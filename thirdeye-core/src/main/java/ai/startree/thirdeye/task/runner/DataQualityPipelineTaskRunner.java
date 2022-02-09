/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package ai.startree.thirdeye.task.runner;

import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.task.TaskInfo;
import ai.startree.thirdeye.task.TaskContext;
import ai.startree.thirdeye.task.TaskResult;
import ai.startree.thirdeye.task.TaskRunner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ths class is responsible for running the data quality tasks
 * It is not implemented in v2.
 */
@Singleton
public class DataQualityPipelineTaskRunner implements TaskRunner {

  private static final Logger LOG = LoggerFactory.getLogger(DataQualityPipelineTaskRunner.class);

  private final AlertManager alertManager;

  @Inject
  public DataQualityPipelineTaskRunner(final AlertManager detectionConfigManager) {
    this.alertManager = detectionConfigManager;
  }

  @Override
  public List<TaskResult> execute(TaskInfo taskInfo, TaskContext taskContext) throws Exception {
    throw new UnsupportedOperationException("DataQuality Pipeline execution not implemented in v2.");
  }
}