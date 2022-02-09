/**
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.startree.thirdeye.datasource.sql;

import ai.startree.thirdeye.datalayer.bao.TestDbEnv;
import ai.startree.thirdeye.datasource.ThirdEyeCacheRegistry;
import ai.startree.thirdeye.datasource.cache.MetricDataset;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.MetricFunction;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import ai.startree.thirdeye.spi.detection.MetricAggFunction;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import ai.startree.thirdeye.spi.detection.TimeSpec;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestSqlUtils {

  private final String dataset = "mysql.db.table";
  private final String metric = "metric";

  private MetricDataset metricDataset;
  private MetricFunction metricFunction;
  private TestDbEnv testDbEnv;

  @BeforeMethod
  public void beforeMethod() throws Exception {
    this.testDbEnv = new TestDbEnv();
    this.metricDataset = new MetricDataset(metric, dataset);

    LoadingCache<String, DatasetConfigDTO> mockDatasetConfigCache = Mockito
        .mock(LoadingCache.class);
    Mockito.when(mockDatasetConfigCache.get(this.dataset)).thenReturn(new DatasetConfigDTO());

    LoadingCache<MetricDataset, MetricConfigDTO> mockMetricConfigCache = Mockito
        .mock(LoadingCache.class);
    Mockito.when(mockMetricConfigCache.get(this.metricDataset)).thenReturn(new MetricConfigDTO());

    TestDbEnv.getInstance(ThirdEyeCacheRegistry.class)
        .registerDatasetConfigCache(mockDatasetConfigCache);
    TestDbEnv.getInstance(ThirdEyeCacheRegistry.class)
        .registerMetricConfigCache(mockMetricConfigCache);

    MetricConfigDTO metricConfigDTO = new MetricConfigDTO();
    metricConfigDTO.setDataset(this.dataset);
    metricConfigDTO.setName(this.metricDataset.getMetricName());
    metricConfigDTO
        .setAlias(this.metricDataset.getDataset() + "::" + this.metricDataset.getMetricName());

    metricFunction = new MetricFunction();
    metricFunction.setDataset(dataset);
    metricFunction.setMetricId(1L);
    metricFunction.setMetricName(metric);
    metricFunction.setFunctionName(MetricAggFunction.SUM);

    TestDbEnv.getInstance().getMetricConfigDAO().save(metricConfigDTO);
  }

  @AfterMethod
  public void afterMethod() {
    try {
      this.testDbEnv.cleanup();
    } catch (Exception ignore) {
    }
  }

  @Test
  public void testSqlWithExplicitLimit() {
    TimeGranularity timeGranularity = new TimeGranularity(1, TimeUnit.DAYS);
    DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        .withZone(DateTimeZone.UTC);
    ThirdEyeRequest request = ThirdEyeRequest.newBuilder()
        .setDataSource(this.dataset)
        .setLimit(100)
        .setGroupBy("country")
        .setStartTimeInclusive(DateTime.parse("2020-05-01", formatter))
        .setEndTimeExclusive(DateTime.parse("2020-05-01", formatter))
        .setGroupByTimeGranularity(timeGranularity)
        .build("");

    String timeFormat = TimeSpec.SINCE_EPOCH_FORMAT;
    TimeSpec timeSpec = new TimeSpec("date", timeGranularity, timeFormat);
    String actualSql = SqlUtils
        .getSql(request, this.metricFunction, HashMultimap.create(), timeSpec, this.dataset,
            TestDbEnv.getInstance().getMetricConfigDAO());
    String expected = "SELECT date, country, SUM(metric) FROM table WHERE  date = 18383 GROUP BY date, country ORDER BY SUM(metric) DESC LIMIT 100";
    Assert.assertEquals(actualSql, expected);
  }

  @Test
  public void testSqlWithoutExplicitLimit() {
    TimeGranularity timeGranularity = new TimeGranularity(1, TimeUnit.DAYS);
    DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        .withZone(DateTimeZone.UTC);
    ThirdEyeRequest request = ThirdEyeRequest.newBuilder()
        .setDataSource(this.dataset)
        .setGroupBy("country")
        .setStartTimeInclusive(DateTime.parse("2020-05-01", formatter))
        .setEndTimeExclusive(DateTime.parse("2020-05-01", formatter))
        .setGroupByTimeGranularity(timeGranularity)
        .build("");

    String timeFormat = TimeSpec.SINCE_EPOCH_FORMAT;
    TimeSpec timeSpec = new TimeSpec("date", timeGranularity, timeFormat);
    String actual = SqlUtils
        .getSql(request, this.metricFunction, HashMultimap.create(), timeSpec, this.dataset,
            TestDbEnv.getInstance().getMetricConfigDAO());
    String expected = "SELECT date, country, SUM(metric) FROM table WHERE  date = 18383 GROUP BY date, country LIMIT 100000";
    Assert.assertEquals(actual, expected);
  }
}