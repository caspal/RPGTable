package info.pascalkrause.rpgtable.web;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;

import static org.mockito.Mockito.*;

import info.pascalkrause.rpgtable.TestUtils;
import info.pascalkrause.rpgtable.WebRouterVerticle;
import info.pascalkrause.rpgtable.api.ImageAPI;
import info.pascalkrause.rpgtable.utils.RPGTableConfig;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.ext.unit.TestContext;

public abstract class ImageRouterTest {

    private Vertx vertx;
    private HttpClient client;
    private ImageAPI imageApiMock;

    public HttpClient getClient() {
        return client;
    }
    
    public ImageAPI getImageApiMock() {
        return imageApiMock;
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
        
        RPGTableConfig config = RPGTableConfig.create(false, TestUtils.getFreePort(), null);
        DeploymentOptions options = new DeploymentOptions().setConfig(config.toJsonObject());

        imageApiMock = mock(ImageAPI.class);
        
        vertx.deployVerticle(new WebRouterVerticle(imageApiMock), options, context.asyncAssertSuccess());

        // Init client
        HttpClientOptions opts = new HttpClientOptions().setDefaultPort(config.HTTP_PORT).setDefaultHost("localhost");
        client = vertx.createHttpClient(opts);
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