package org.apache.pinot.thirdeye.detection.v2.operator;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.apache.pinot.thirdeye.detection.v2.components.filler.TimeIndexFiller;
import org.apache.pinot.thirdeye.detection.v2.spec.TimeIndexFillerSpec;
import org.apache.pinot.thirdeye.spi.detection.AbstractSpec;
import org.apache.pinot.thirdeye.spi.detection.DetectionUtils;
import org.apache.pinot.thirdeye.spi.detection.IndexFiller;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeIndexFillerOperator extends DetectionPipelineOperator {

  private static final Logger LOG = LoggerFactory.getLogger(TimeIndexFillerOperator.class);
  private static final String OPERATOR_NAME = "TimeIndexFillerOperator";

  private IndexFiller<? extends AbstractSpec> timeIndexFiller;

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
    this.timeIndexFiller = createTimeIndexFiller(planNode.getParams());

    checkArgument(inputMap.size() == 1,
        OPERATOR_NAME + " must have exactly 1 input node.");
    checkArgument(outputKeyMap.size() == 1,
        OPERATOR_NAME + " must have exactly 1 output node.");
  }

  @Override
  public void execute() throws Exception {
    final Map<String, DataTable> timeSeriesMap = DetectionUtils.getTimeSeriesMap(inputMap);
    checkArgument(timeSeriesMap.size() == 1,
        OPERATOR_NAME + " must have exactly 1 DataTable in input");
    final Interval interval = new Interval(startTime, endTime);
    final DataTable dataTable = timeIndexFiller.fillIndex(interval,
        timeSeriesMap.values().iterator().next());
    resultMap.put(outputKeyMap.values().iterator().next(), dataTable);
  }

  @Override
  public String getOperatorName() {
    return OPERATOR_NAME;
  }

  private IndexFiller<? extends AbstractSpec> createTimeIndexFiller(
      final Map<String, Object> params) {
    final Map<String, Object> componentSpec = getComponentSpec(params);
    final TimeIndexFillerSpec spec = requireNonNull(
        AbstractSpec.fromProperties(componentSpec, TimeIndexFillerSpec.class),
        "Unable to construct TimeIndexFillerSpec");

    final IndexFiller<TimeIndexFillerSpec> timeIndexFiller = new TimeIndexFiller();
    timeIndexFiller.init(spec);

    return timeIndexFiller;
  }
}
