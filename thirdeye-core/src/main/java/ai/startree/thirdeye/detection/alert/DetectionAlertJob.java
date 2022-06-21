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
package ai.startree.thirdeye.detection.alert;

import static ai.startree.thirdeye.detection.TaskUtils.createTaskDto;

import ai.startree.thirdeye.detection.TaskUtils;
import ai.startree.thirdeye.scheduler.ThirdEyeAbstractJob;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalySubscriptionGroupNotificationManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalySubscriptionGroupNotificationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.task.TaskType;
import ai.startree.thirdeye.task.DetectionAlertTaskInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Map;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Detection alert job that run by the cron scheduler.
 * This job put notification task into database which can be picked up by works later.
 */
public class DetectionAlertJob extends ThirdEyeAbstractJob {

  private static final Logger LOG = LoggerFactory.getLogger(DetectionAlertJob.class);

  @Override
  public void execute(JobExecutionContext ctx) throws JobExecutionException {
    final SubscriptionGroupManager alertConfigDAO = getInstance(ctx,
        SubscriptionGroupManager.class);
    final MergedAnomalyResultManager anomalyDAO = getInstance(ctx,
        MergedAnomalyResultManager.class);
    final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationDAO =
        getInstance(ctx, AnomalySubscriptionGroupNotificationManager.class);
    final TaskManager taskDAO = getInstance(ctx, TaskManager.class);

    final String jobKey = ctx.getJobDetail().getKey().getName();
    final long detectionAlertConfigId = TaskUtils.getIdFromJobKey(jobKey);
    final SubscriptionGroupDTO configDTO = alertConfigDAO.findById(detectionAlertConfigId);
    if (configDTO == null) {
      LOG.error("Subscription config {} does not exist", detectionAlertConfigId);
    }

    DetectionAlertTaskInfo taskInfo = new DetectionAlertTaskInfo(detectionAlertConfigId);

    // if a task is pending and not time out yet, don't schedule more
    String jobName = String.format("%s_%d", TaskType.NOTIFICATION, detectionAlertConfigId);
    if (taskAlreadyRunning(taskDAO, jobName)) {
      LOG.trace("Skip scheduling subscription task {}. Already queued", jobName);
      return;
    }

    if (configDTO != null && !needNotification(configDTO, anomalyDAO,
        anomalySubscriptionGroupNotificationDAO)) {
      LOG.trace("Skip scheduling subscription task {}. No anomaly to notify.", jobName);
      return;
    }

    try {
      TaskDTO taskDTO = createTaskDto(detectionAlertConfigId, taskInfo, TaskType.NOTIFICATION);
      final long taskId = taskDAO.save(taskDTO);
      LOG.info("Created {} task {} with settings {}", TaskType.NOTIFICATION, taskId, taskDTO);
    } catch (JsonProcessingException e) {
      LOG.error("Exception when converting TaskInfo {} to jsonString", taskInfo, e);
    }
  }

  private boolean taskAlreadyRunning(final TaskManager taskDAO, final String jobName) {
    // check if a notification task for the job is already scheduled and not timed-out
    // todo cyril current implementation does not check the timeout
    List<TaskDTO> scheduledTasks = taskDAO.findByPredicate(Predicate.AND(
        Predicate.EQ("name", jobName),
        Predicate.OR(
            Predicate.EQ("status", TaskStatus.RUNNING.toString()),
            Predicate.EQ("status", TaskStatus.WAITING.toString())
        ))
    );
    return !scheduledTasks.isEmpty();
  }

  /**
   * Check if we need to create a subscription task.
   * If there is no anomaly generated (by looking at anomaly create_time) between last notification
   * time
   * till now (left inclusive, right exclusive) then no need to create this task.
   *
   * Even if an anomaly gets merged (end_time updated) it will not renotify this anomaly as the
   * create_time is not
   * modified.
   * For example, if previous anomaly is from t1 to t2 generated at t3, then the timestamp in
   * vectorLock is t3.
   * If there is a new anomaly from t2 to t4 generated at t5, then we can still get this anomaly as
   * t5 > t3.
   *
   * Also, check if there is any anomaly that needs re-notifying
   *
   * @param configDTO The Subscription Configuration.
   * @return true if it needs notification task. false otherwise.
   */
  private boolean needNotification(SubscriptionGroupDTO configDTO,
      final MergedAnomalyResultManager anomalyDAO,
      final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationManager) {
    Map<Long, Long> vectorClocks = configDTO.getVectorClocks();
    if (vectorClocks == null || vectorClocks.size() == 0) {
      return true;
    }
    for (Map.Entry<Long, Long> e : vectorClocks.entrySet()) {
      long configId = e.getKey();
      long lastNotifiedTime = e.getValue();
      if (anomalyDAO.findByCreatedTimeInRangeAndDetectionConfigId(lastNotifiedTime,
              System.currentTimeMillis(), configId)
          .stream().anyMatch(x -> !x.isChild())) {
        return true;
      }
    }
    // in addition to checking the watermarks, check if any anomalies need to be re-notified by querying the anomaly subscription group notification table
    List<AnomalySubscriptionGroupNotificationDTO> anomalySubscriptionGroupNotifications =
        anomalySubscriptionGroupNotificationManager.findByPredicate(
            Predicate.IN("detectionConfigId", vectorClocks.keySet().toArray()));
    return !anomalySubscriptionGroupNotifications.isEmpty();
  }
}
