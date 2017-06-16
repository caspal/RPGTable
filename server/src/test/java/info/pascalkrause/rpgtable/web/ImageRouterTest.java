package info.pascalkrause.rpgtable.web;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

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
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public abstract class ImageRouterTest {

    private Vertx vertx;
    private HttpClient client;
    protected ImageAPI imageApiMock;

    public HttpClient getClient() {
        return client;
    }

    @Before
    public void setUp(TestContext context) throws IOException {
        imageApiMock = mock(ImageAPI.class);
        vertx = Vertx.vertx();

        RPGTableConfig config = RPGTableConfig.create(true, TestUtils.getFreePort(), null);
        DeploymentOptions options = new DeploymentOptions().setConfig(config.toJsonObject());
        vertx.deployVerticle(new WebRouterVerticle(imageApiMock), options, context.asyncAssertSuccess());

        HttpClientOptions opts = new HttpClientOptions().setDefaultPort(config.HTTP_PORT).setDefaultHost("localhost");
        client = vertx.createHttpClient(opts);
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }
}