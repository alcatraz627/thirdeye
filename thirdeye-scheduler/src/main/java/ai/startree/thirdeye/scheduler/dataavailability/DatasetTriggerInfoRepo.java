/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.scheduler.dataavailability;

import ai.startree.thirdeye.rootcause.entity.MetricEntity;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.util.ThirdEyeUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is to refresh a list of active dataset and its latest timestamp in memory so that
 * it can be used for event-driven scheduling.
 */
@Singleton
public class DatasetTriggerInfoRepo {

  private static final Logger LOG = LoggerFactory.getLogger(DatasetTriggerInfoRepo.class);

  private final Map<String, Long> datasetRefreshTimeMap;
  private final ScheduledThreadPoolExecutor executorService;
  private final AlertManager detectionConfigDAO;
  private final DatasetConfigManager datasetConfigManager;
  private final MetricConfigManager metricConfigManager;
  private final int refreshFreqInMin = 1;
  private Set<String> dataSourceWhitelist = new HashSet<>();

  @Inject
  private DatasetTriggerInfoRepo(final AlertManager detectionConfigManager,
      final DatasetConfigManager datasetConfigManager,
      final MetricConfigManager metricConfigManager) {
    this.detectionConfigDAO = detectionConfigManager;
    this.datasetConfigManager = datasetConfigManager;
    this.metricConfigManager = metricConfigManager;

    this.datasetRefreshTimeMap = new ConcurrentHashMap<>();
    this.executorService = new ScheduledThreadPoolExecutor(1, r -> {
      Thread t = Executors.defaultThreadFactory().newThread(r);
      t.setDaemon(true);
      return t;
    });
    this.executorService.scheduleAtFixedRate(
        this::updateFreshTimeMap, refreshFreqInMin, refreshFreqInMin, TimeUnit.MINUTES);
  }

  public void init(Collection<String> dataSourceWhitelist) {
    this.dataSourceWhitelist = new HashSet<>(dataSourceWhitelist);
    this.updateFreshTimeMap(); // initial refresh
  }

  public boolean isDatasetActive(String dataset) {
    return datasetRefreshTimeMap.containsKey(dataset);
  }

  public long getLastUpdateTimestamp(String dataset) {
    return datasetRefreshTimeMap.getOrDefault(dataset, 0L);
  }

  public void setLastUpdateTimestamp(String dataset, long timestamp) {
    datasetRefreshTimeMap.put(dataset, timestamp);
  }

  public void close() {
    executorService.shutdown();
  }

  private void updateFreshTimeMap() {
    Set<Long> visitedMetrics = new HashSet<>(); // reduce the number of DB read
    List<AlertDTO> detectionConfigs = detectionConfigDAO.findAllActive();
    LOG.info(String.format("Found %d active detection configs", detectionConfigs.size()));
    for (AlertDTO detectionConfig : detectionConfigs) {
      Set<String> metricUrns = ThirdEyeUtils
          .extractMetricUrnsFromProperties(detectionConfig.getProperties());
      for (String urn : metricUrns) {
        MetricEntity me = MetricEntity.fromURN(urn);
        if (visitedMetrics.contains(me.getId())) {
          // the metric is already visited before, so skipping.
          continue;
        }
        List<DatasetConfigDTO> datasetConfigs = ThirdEyeUtils.getDatasetConfigsFromMetricUrn(urn,
            datasetConfigManager,
            metricConfigManager);
        for (DatasetConfigDTO datasetConfig : datasetConfigs) {
          String datasetName = datasetConfig.getDataset();
          if (!datasetRefreshTimeMap.containsKey(datasetName)
              && dataSourceWhitelist.contains(datasetConfig.getDataSource())) {
            datasetRefreshTimeMap.put(datasetName, datasetConfig.getLastRefreshTime());
          }
        }
        visitedMetrics.add(me.getId());
      }
    }
    LOG.info("Finished updating the list of dataset with size: " + datasetRefreshTimeMap.size());
  }
}
