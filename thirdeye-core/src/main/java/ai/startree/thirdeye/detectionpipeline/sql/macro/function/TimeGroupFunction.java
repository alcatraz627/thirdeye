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
package ai.startree.thirdeye.detectionpipeline.sql.macro.function;

import static ai.startree.thirdeye.spi.datasource.macro.MacroMetadataKeys.GRANULARITY;
import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.macro.MacroFunction;
import ai.startree.thirdeye.spi.datasource.macro.MacroFunctionContext;
import java.util.List;
import java.util.Objects;
import org.joda.time.Period;

public class TimeGroupFunction implements MacroFunction {

  @Override
  public String name() {
    return "__timeGroup";
  }

  @Override
  public String expandMacro(final List<String> macroParams, final MacroFunctionContext context) {
    //parse params
    checkArgument(macroParams.size() == 3,
        "timeGroup macro requires 3 parameters. Eg: __timeGroup(timeColumn, 'timeFormat', 'granularity')");
    final String timeColumn = macroParams.get(0);
    final String timeColumnFormat = context.getLiteralUnquoter().apply(macroParams.get(1));
    final String granularityText = context.getLiteralUnquoter().apply(macroParams.get(2));
    Period granularity = isoPeriod(granularityText);
    final String timezone = context.getDetectionInterval().getChronology().getZone().toString();

    //write granularity to metadata
    context.getProperties().put(GRANULARITY.toString(), granularityText);
    if (isAutoTimeConfiguration(timeColumn)) {
      final DatasetConfigDTO datasetConfigDTO = context.getDatasetConfigDTO();
      Objects.requireNonNull(datasetConfigDTO, "Cannot use AUTO mode for macro. dataset table name is not defined.");
      final String quotedTimeColumn = context.getIdentifierQuoter().apply(datasetConfigDTO.getTimeColumn());
      return context.getSqlExpressionBuilder()
          .getTimeGroupExpression(quotedTimeColumn,
              datasetConfigDTO.getTimeFormat(),
              granularity,
              datasetConfigDTO.getTimeUnit().toString(),
              timezone);
    }

    //generate SQL expression
    return context.getSqlExpressionBuilder()
        .getTimeGroupExpression(timeColumn, timeColumnFormat, granularity, timezone);
  }
}
