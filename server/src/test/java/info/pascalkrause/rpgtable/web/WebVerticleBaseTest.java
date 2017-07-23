package info.pascalkrause.rpgtable.web;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;

@RunWith(VertxUnitRunner.class)
public abstract class WebVerticleBaseTest {

    protected Vertx vertx;
    private Integer port;
    protected Router router;
    protected static HttpClient client;

    protected Router initializeRouter(Vertx vertx) {
        return Router.router(vertx);
    }

    private Verticle verticle = new AbstractVerticle() {
        @Override
        public void start(Future<Void> fut) throws IOException {
            router = initializeRouter(vertx);
            vertx.createHttpServer().requestHandler(router::accept).listen(port, result -> {
                if (result.succeeded()) {
                    fut.complete();
                } else {
                    fut.fail(result.cause());
                }
            });
        }
    };

    @Before
    public void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();
        Vertx.vertx(new VertxOptions().setInternalBlockingPoolSize(1));
        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();
        DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("http.port", port));
        vertx.deployVerticle(verticle, options, context.asyncAssertSuccess());

        HttpClientOptions opts = new HttpClientOptions().setDefaultPort(port).setDefaultHost("localhost");
        client = vertx.createHttpClient(opts);
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }
}