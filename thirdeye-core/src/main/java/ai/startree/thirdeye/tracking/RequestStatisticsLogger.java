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
package ai.startree.thirdeye.tracking;

import ai.startree.thirdeye.spi.detection.TimeGranularity;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestStatisticsLogger implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(RequestStatisticsLogger.class);
  private static final RequestLog requestLog = new RequestLog(1000000);

  private final ScheduledExecutorService scheduledExecutorService;
  private final TimeGranularity runFrequency;

  public RequestStatisticsLogger(TimeGranularity runFrequency) {
    this.runFrequency = runFrequency;
    this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
  }

  public static RequestLog getRequestLog() {
    return requestLog;
  }

  @Override
  public void run() {
    try {
      long timestamp = System.nanoTime();
      RequestStatistics stats = getRequestLog().getStatistics(timestamp);
      getRequestLog().truncate(timestamp);

      RequestStatisticsFormatter formatter = new RequestStatisticsFormatter();
      LOG.info("Recent request performance statistics:\n{}", formatter.format(stats));
    } catch (Exception e) {
      LOG.error("Could not generate statistics", e);
    }
  }

  public void start() {
    LOG.info("starting logger");
    this.scheduledExecutorService.scheduleWithFixedDelay(this,
        this.runFrequency.getSize(), this.runFrequency.getSize(), this.runFrequency.getUnit());
  }

  public void shutdown() {
    LOG.info("stopping logger");
    this.scheduledExecutorService.shutdown();
  }
}
