package info.pascalkrause.rpgtable;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import info.pascalkrause.rpgtable.utils.RPGTableConfig;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * This is our JUnit test for our verticle. The test uses vertx-unit, so we declare a custom runner.
 */
@RunWith(VertxUnitRunner.class)
public abstract class AbstractWebRouterVerticleTest {

    private Vertx vertx;
    private HttpClient client;

    public HttpClient getClient() {
        return client;
    }
    
    private String tempWorkspaceDir = TestUtils.createTempWorkspaceDir(); 

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

        RPGTableConfig config = RPGTableConfig.create(false, TestUtils.getFreePort(), tempWorkspaceDir);
        DeploymentOptions options = new DeploymentOptions().setConfig(config.toJsonObject());

        vertx.deployVerticle(WebRouterVerticle.class.getName(), options, context.asyncAssertSuccess());

        // Init client
        client = vertx
                .createHttpClient(new HttpClientOptions().setDefaultPort(config.HTTP_PORT).setDefaultHost("localhost"));
    }

    /**
     * This method, called after our test, just cleanup everything by closing the vert.x instance
     *
     * @param context
     *            the test context
     */
    @After
    public void tearDown(TestContext context) {
        vertx.fileSystem().deleteRecursiveBlocking(tempWorkspaceDir, true);
        vertx.close(context.asyncAssertSuccess());
    }
}