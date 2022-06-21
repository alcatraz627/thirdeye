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
package ai.startree.thirdeye.detection.anomalydetection.context;

import ai.startree.thirdeye.spi.detection.dimension.DimensionMap;
import java.util.Objects;
import org.apache.commons.lang3.ObjectUtils;

public class TimeSeriesKey {

  private String metricName = "";
  private DimensionMap dimensionMap = new DimensionMap();

  public String getMetricName() {
    return metricName;
  }

  public void setMetricName(String metricName) {
    this.metricName = metricName;
  }

  public DimensionMap getDimensionMap() {
    return dimensionMap;
  }

  public void setDimensionMap(DimensionMap dimensionMap) {
    this.dimensionMap = dimensionMap;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof TimeSeriesKey) {
      TimeSeriesKey other = (TimeSeriesKey) o;
      return ObjectUtils.equals(metricName, other.metricName)
          && ObjectUtils.equals(dimensionMap, other.dimensionMap);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(metricName, dimensionMap);
  }
}
