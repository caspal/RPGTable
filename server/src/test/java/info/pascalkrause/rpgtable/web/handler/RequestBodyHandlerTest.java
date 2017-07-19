package info.pascalkrause.rpgtable.web.handler;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.SecureRandom;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableSet;
import com.google.common.net.MediaType;

import info.pascalkrause.rpgtable.TestUtils;
import info.pascalkrause.rpgtable.error.EmptyRequestBodyError;
import info.pascalkrause.rpgtable.error.InvalidContentTypeError;
import info.pascalkrause.rpgtable.error.RequestBodyTooLargeError;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;

@RunWith(VertxUnitRunner.class)
public class RequestBodyHandlerTest {

    private Vertx vertx;
    private Integer port;
    private Router router;
    private Handler<Throwable> expectionHandlerMock;

    private Verticle verticle = new AbstractVerticle() {
        @SuppressWarnings("unchecked")
        @Override
        public void start(Future<Void> fut) throws IOException {
            expectionHandlerMock = mock(Handler.class);
            router = Router.router(vertx);
            router.exceptionHandler(expectionHandlerMock);
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
        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();
        DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("http.port", port));
        vertx.deployVerticle(verticle, options, context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void rejectEmptyBodyTest(TestContext context) {
        final Async testComplete = context.async();
        router.route("/").handler(new RequestBodyHandler(0, null, true));
        vertx.createHttpClient().post(port, "localhost", "/", response -> {
            TestUtils.failTestOnException(context,
                    v -> verify(expectionHandlerMock).handle(argThat(e -> e instanceof EmptyRequestBodyError)));
            testComplete.complete();
        }).end();
    }

    @Test
    public void rejectTooLargeBodyTest(TestContext context) {
        int bodySizeLimit = 5;
        Buffer payload = Buffer.buffer(SecureRandom.getSeed(bodySizeLimit + 1));
        final Async testComplete = context.async();
        router.route("/").handler(new RequestBodyHandler(3, null, true));
        vertx.createHttpClient().post(port, "localhost", "/", response -> {
            TestUtils.failTestOnException(context,
                    v -> verify(expectionHandlerMock).handle(argThat(e -> e instanceof RequestBodyTooLargeError)));
            testComplete.complete();
        }).putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(payload.length())).write(payload).end();
    }

    @Test
    public void rejectInvalidContentTypeTest(TestContext context) {
        Buffer payload = Buffer.buffer("<html>This is not a picture</html>");
        final Async testComplete = context.async();
        router.route("/").handler(new RequestBodyHandler(0, ImmutableSet.of(MediaType.GIF), true));
        vertx.createHttpClient().post(port, "localhost", "/", response -> {
            TestUtils.failTestOnException(context,
                    v -> verify(expectionHandlerMock).handle(argThat(e -> e instanceof InvalidContentTypeError)));
            testComplete.complete();
        }).putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(payload.length())).write(payload).end();
    }

    @Test
    public void letPassValidRequest(TestContext context) {
        Buffer payload = Buffer.buffer("<html>This is not a picture</html>");
        final Async testComplete = context.async();
        router.route("/").handler(new RequestBodyHandler(0, null, true));
        router.route("/").handler(rc -> rc.response().setStatusCode(1337).end());
        vertx.createHttpClient().post(port, "localhost", "/", response -> {
            TestUtils.failTestOnException(context, v -> assertThat(response.statusCode()).isEqualTo(1337));
            testComplete.complete();
        }).putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(payload.length())).write(payload).end();
    }
}