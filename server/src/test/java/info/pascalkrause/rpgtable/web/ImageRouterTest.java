package info.pascalkrause.rpgtable.web;

import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import info.pascalkrause.rpgtable.TestUtils;
import info.pascalkrause.rpgtable.WebRouterVerticle;
import info.pascalkrause.rpgtable.api.ImageAPI;
import info.pascalkrause.rpgtable.utils.RPGTableConfig;
import info.pascalkrause.rpgtable.utils.RPGTableConfig.RPGTableConfigOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public abstract class ImageRouterTest {

    private static Vertx vertx;
    private static HttpClient client;
    protected static ImageAPI imageApiMock;
    protected static RPGTableConfig config;
    
    public HttpClient getClient() {
        return client;
    }

    @BeforeClass
    public static void setUp(TestContext context) throws IOException {
        imageApiMock = mock(ImageAPI.class);
        vertx = Vertx.vertx();

        config = new RPGTableConfig(RPGTableConfigOptions.defaults().setEnvTest(true)
                .setHttpPort(TestUtils.getFreePort()).setWorkspaceDir(null).setBodySizeLimitBytes(100));
        DeploymentOptions options = new DeploymentOptions().setConfig(config.toJsonObject());
        vertx.deployVerticle(new WebRouterVerticle(imageApiMock), options, context.asyncAssertSuccess());

        HttpClientOptions opts = new HttpClientOptions().setDefaultPort(config.HTTP_PORT).setDefaultHost("localhost");
        client = vertx.createHttpClient(opts);
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }
}