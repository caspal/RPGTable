package info.pascalkrause.rpgtable;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import info.pascalkrause.rpgtable.utils.RPGTableConfig;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * This is our JUnit test for our verticle. The test uses vertx-unit, so we declare a custom runner.
 */
@RunWith(VertxUnitRunner.class)
public class WebRouterVerticleTest {

    private Vertx vertx;
    private RPGTableConfig config;

    public RPGTableConfig getConfig() {
        return config;
    }
    
    public Vertx getVertx() {
        return vertx;
    }
    
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

        // Let's configure the verticle to listen on the 'test' port (randomly picked).
        // We create deployment options and set the _configuration_ json object:
        ServerSocket socket = new ServerSocket(0);
        int port = socket.getLocalPort();
        socket.close();

        config  = RPGTableConfig.create(false, port, null);
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
        vertx.close(context.asyncAssertSuccess());
    }
}