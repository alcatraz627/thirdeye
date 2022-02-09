package ai.startree.thirdeye;

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.auth.AuthConfiguration;
import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.datalayer.TestDatabase;
import ai.startree.thirdeye.datasource.ThirdEyeCacheRegistry;
import ai.startree.thirdeye.resources.RootResource;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.testng.annotations.Test;

public class ThirdEyeServerModuleTest {

  @Test
  public void testRootResourceInjection() throws Exception {
    TestDatabase db = new TestDatabase();
    final DataSource dataSource = db.createDataSource(db.testDatabaseConfiguration());

    final ThirdEyeServerConfiguration configuration = new ThirdEyeServerConfiguration()
        .setAuthConfiguration(new AuthConfiguration())
        .setConfigPath("../config");

    final Injector injector = Guice.createInjector(new ThirdEyeServerModule(
        configuration,
        dataSource,
        new MetricRegistry()));

    injector
        .getInstance(ThirdEyeCacheRegistry.class)
        .initializeCaches();

    assertThat(injector.getInstance(RootResource.class)).isNotNull();
  }
}