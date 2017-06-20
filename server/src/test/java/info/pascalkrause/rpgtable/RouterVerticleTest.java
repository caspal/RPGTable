package info.pascalkrause.rpgtable;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import info.pascalkrause.rpgtable.utils.RPGTableConfig;
import info.pascalkrause.rpgtable.utils.RPGTableConfig.RPGTableConfigOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * This is our JUnit test for our verticle. The test uses vertx-unit, so we declare a custom runner.
 */
@RunWith(VertxUnitRunner.class)
public class RouterVerticleTest {

    private Vertx vertx;
    private RPGTableConfig config;

    /**
     * Before executing our test, let's deploy our verticle.
     * <p/>
     * This method instantiates a new Vertx and deploy the verticle. Then, it waits in the verticle has successfully
     * completed its start sequence (thanks to `context.asyncAssertSuccess`).
     *
     * @param context
     *            the test context.
     * @throws IOException
     */
    @Before
    public void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();
        config = new RPGTableConfig(RPGTableConfigOptions.defaults().setEnvTest(true)
                .setHttpPort(TestUtils.getFreePort()).setWorkspaceDir(TestUtils.createTempWorkspaceDir()));
        DeploymentOptions options = new DeploymentOptions().setConfig(config.toJsonObject());

        // We pass the options as the second parameter of the deployVerticle method.
        vertx.deployVerticle(WebRouterVerticle.class.getName(), options, context.asyncAssertSuccess());
    }

    /**
     * This method, called after our test, just cleanup everything by closing the vert.x instance
     *
     * @param context
     *            the test context
     */
    @After
    public void tearDown(TestContext context) {
        vertx.fileSystem().deleteRecursiveBlocking(config.WORKSPACE_DIR, true);
        vertx.close(context.asyncAssertSuccess());
    }

    /**
     * Let's ensure that our application behaves correctly.
     *
     * @param context
     *            the test context
     */
    @Test
    public void serveIndexHTMLTest(TestContext context) {
        // This test is asynchronous, so get an async handler to inform the test
        // when we are done.
        final Async async = context.async();

        // We create a HTTP client and query our application. When we get the response we check it contains
        // the 'Welcome' message. Then, we call the `complete` method on the async handler to declare
        // this async (and here the test) done. Notice that the assertions are made on the 'context'
        // object and are not Junit assert. This ways it manage the async aspect of the test the right way.
        vertx.createHttpClient().getNow(config.HTTP_PORT, "localhost", "/", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                assertThat(response.statusCode()).isEqualTo(200);
                assertThat(response.getHeader(HttpHeaders.CONTENT_TYPE).equals(HttpHeaders.TEXT_HTML));
                assertThat(body.toString()).contains("Welcome");
                async.complete();
            });
        });
    }
}
