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
package ai.startree.thirdeye.detectionpipeline.operator;

import static ai.startree.thirdeye.spi.Constants.K_DATASET_MANAGER;
import static ai.startree.thirdeye.spi.Constants.K_DATA_SOURCE_CACHE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.detectionpipeline.OperatorContext;
import ai.startree.thirdeye.detectionpipeline.components.GenericDataFetcher;
import ai.startree.thirdeye.detectionpipeline.spec.DataFetcherSpec;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.detection.BaseComponent;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataFetcherOperatorTest {

  public static final String TABLE_NAME = "myTable";
  private DataSourceCache dataSourceCache;
  private DatasetConfigManager datasetDao;
  private String dataSourceName;

  @BeforeMethod
  public void setUp() {
    dataSourceName = "pinot-cluster-1";

    dataSourceCache = mock(DataSourceCache.class);
    datasetDao = mock(DatasetConfigManager.class);
    when(datasetDao.findByDataset(anyString())).thenReturn(new DatasetConfigDTO().setDataset
        (TABLE_NAME));
    final ThirdEyeDataSource thirdEyeDataSource = mock(ThirdEyeDataSource.class);
    when(dataSourceCache.getDataSource(dataSourceName))
        .thenReturn(thirdEyeDataSource);
  }

  @Test
  public void testNewInstance() {
    final DataFetcherOperator dataFetcherOperator = new DataFetcherOperator();
    final PlanNodeBean planNodeBean = new PlanNodeBean()
        .setParams(TemplatableMap.fromValueMap(ImmutableMap.of("component.dataSource",
            dataSourceName)))
        .setOutputs(ImmutableList.of());
    final Map<String, Object> properties = ImmutableMap.of(K_DATA_SOURCE_CACHE,
        dataSourceCache, K_DATASET_MANAGER, datasetDao);
    final long startTime = System.currentTimeMillis();
    final long endTime = startTime + 1000L;
    final Interval detectionInterval = new Interval(startTime, endTime, DateTimeZone.UTC);
    final OperatorContext context = new OperatorContext()
        .setDetectionInterval(detectionInterval)
        .setPlanNode(planNodeBean)
        .setProperties(properties);
    dataFetcherOperator.init(context);
    assertThat(dataFetcherOperator.getDetectionInterval()).isEqualTo(detectionInterval);
  }

  @Test
  public void testInitComponents() {
    Map<String, Object> params = new HashMap<>();

    params.put("component.dataSource", dataSourceName);
    params.put("component.query", "SELECT * FROM " + TABLE_NAME);
    params.put("component.tableName", TABLE_NAME);

    final DataFetcherOperator dataFetcherOperator = new DataFetcherOperator();
    final long startTime = System.currentTimeMillis();
    final long endTime = startTime + 1000L;
    final PlanNodeBean planNodeBean = new PlanNodeBean()
        .setOutputs(ImmutableList.of())
        .setInputs(ImmutableList.of())
        .setParams(TemplatableMap.fromValueMap(params));

    final Map<String, Object> properties = ImmutableMap.of(K_DATA_SOURCE_CACHE,
        dataSourceCache, K_DATASET_MANAGER, datasetDao);
    final Interval detectionInterval = new Interval(startTime, endTime, DateTimeZone.UTC);
    final OperatorContext context = new OperatorContext()
        .setDetectionInterval(detectionInterval)
        .setPlanNode(planNodeBean)
        .setProperties(properties);
    dataFetcherOperator.init(context);

    assertThat(dataFetcherOperator.getDetectionInterval()).isEqualTo(detectionInterval);

    final BaseComponent<DataFetcherSpec> pinotDataFetcher = dataFetcherOperator.getDataFetcher();

    Assert.assertTrue(pinotDataFetcher instanceof GenericDataFetcher);
    Assert.assertEquals(((GenericDataFetcher) pinotDataFetcher).getQuery(),
        "SELECT * FROM " + TABLE_NAME);
    Assert.assertEquals(((GenericDataFetcher) pinotDataFetcher).getTableName(), TABLE_NAME);
  }
}
