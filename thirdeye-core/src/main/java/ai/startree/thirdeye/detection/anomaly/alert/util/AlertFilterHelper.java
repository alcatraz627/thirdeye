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
package ai.startree.thirdeye.detection.anomaly.alert.util;

import ai.startree.thirdeye.detection.detector.email.filter.AlertFilter;
import ai.startree.thirdeye.detection.detector.email.filter.AlertFilterFactory;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFunctionDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertFilterHelper {

  private static final Logger LOG = LoggerFactory.getLogger(AlertFilterHelper.class);

  /**
   * Each function has a filtration rule which let alert module decide if an anomaly should be
   * included in the alert email. This method applies respective filtration rule on list of
   * anomalies.
   */
  public static List<MergedAnomalyResultDTO> applyFiltrationRule(
      List<MergedAnomalyResultDTO> results, AlertFilterFactory alertFilterFactory) {
    if (results.size() == 0) {
      return results;
    }
    // Function ID to Alert Filter
    Map<Long, AlertFilter> functionAlertFilter = new HashMap<>();

    List<MergedAnomalyResultDTO> qualifiedAnomalies = new ArrayList<>();
    for (MergedAnomalyResultDTO result : results) {
      // Lazy initiates alert filter for anomalies of the same anomaly function
      AnomalyFunctionDTO anomalyFunctionSpec = result.getAnomalyFunction();
      long functionId = anomalyFunctionSpec.getId();
      AlertFilter alertFilter = functionAlertFilter.get(functionId);
      if (alertFilter == null) {
        // Get filtration rule from anomaly function configuration
        alertFilter = alertFilterFactory.fromSpec(anomalyFunctionSpec.getAlertFilter());
        functionAlertFilter.put(functionId, alertFilter);
        LOG.info("Using filter {} for anomaly function {} (dataset: {}, topic metric: {})",
            alertFilter,
            functionId, anomalyFunctionSpec.getCollection(), anomalyFunctionSpec.getTopicMetric());
      }
      if (alertFilter.isQualified(result)) {
        qualifiedAnomalies.add(result);
      }
    }
    LOG.info(
        "Found [{}] anomalies qualified to alert after applying filtration rule on [{}] anomalies",
        qualifiedAnomalies.size(), results.size());
    return qualifiedAnomalies;
  }
}

