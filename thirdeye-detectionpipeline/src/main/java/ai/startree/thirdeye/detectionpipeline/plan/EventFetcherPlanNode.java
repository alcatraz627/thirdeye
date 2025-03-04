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
package ai.startree.thirdeye.detectionpipeline.plan;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.detectionpipeline.Operator;
import ai.startree.thirdeye.detectionpipeline.PlanNodeContext;
import ai.startree.thirdeye.detectionpipeline.operator.EventFetcherOperator;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import com.google.common.collect.ImmutableMap;
import java.util.Map;

public class EventFetcherPlanNode extends DetectionPipelinePlanNode {

  public static final String EVENT_FETCHER_TYPE = "EventFetcher";
  private EventManager eventManager = null;

  @Override
  public void init(final PlanNodeContext planNodeContext) {
    super.init(planNodeContext);
    this.eventManager = planNodeContext.getApplicationContext().getEventManager();
    checkArgument(eventManager != null,
        "Internal issue. No EventManager passed to " + EVENT_FETCHER_TYPE);
  }

  @Override
  public String getType() {
    return EVENT_FETCHER_TYPE;
  }

  @Override
  public Map<String, Object> getParams() {
    return optional(planNodeBean.getParams()).map(TemplatableMap::valueMap).orElse(null);
  }

  @Override
  public Operator buildOperator() throws Exception {
    final EventFetcherOperator eventFetcherOperator = new EventFetcherOperator();
    eventFetcherOperator.init(createOperatorContext()
        .setDetectionInterval(detectionInterval)
        .setPlanNode(planNodeBean)
        .setProperties(ImmutableMap.of(Constants.K_EVENT_MANAGER, eventManager)));
    return eventFetcherOperator;
  }
}
