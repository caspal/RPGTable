package info.pascalkrause.rpgtable.web.handler;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.security.SecureRandom;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.net.MediaType;

import info.pascalkrause.rpgtable.TestUtils;
import info.pascalkrause.rpgtable.error.EmptyRequestBodyError;
import info.pascalkrause.rpgtable.error.InvalidContentTypeError;
import info.pascalkrause.rpgtable.error.RequestBodyTooLargeError;
import info.pascalkrause.rpgtable.web.WebVerticleBaseTest;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.Router;

public class RequestBodyHandlerTest extends WebVerticleBaseTest {

    private Handler<Throwable> expectionHandlerMock;

    @SuppressWarnings("unchecked")
    @Override
    protected Router initializeRouter(Vertx vertx) {
        Router router = super.initializeRouter(vertx);
        expectionHandlerMock = mock(Handler.class);
        router.exceptionHandler(expectionHandlerMock);
        return router;
    }

    @Test
    public void rejectEmptyBodyTest(TestContext context) {
        final Async testComplete = context.async();
        router.route("/").handler(new RequestBodyHandler(true, 0));
        client.post("/", response -> {
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
        router.route("/").handler(new RequestBodyHandler(bodySizeLimit, true));
        client.post("/", response -> {
            TestUtils.failTestOnException(context,
                    v -> verify(expectionHandlerMock).handle(argThat(e -> e instanceof RequestBodyTooLargeError)));
            testComplete.complete();
        }).putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(payload.length())).write(payload).end();
    }

    @Test
    public void rejectInvalidContentTypeTest(TestContext context) {
        Buffer payload = Buffer.buffer("<html>This is not a picture</html>");
        final Async testComplete = context.async();
        router.route("/").handler(new RequestBodyHandler(0, ImmutableSet.of(MediaType.GIF)));
        client.post("/", response -> {
            TestUtils.failTestOnException(context,
                    v -> verify(expectionHandlerMock).handle(argThat(e -> e instanceof InvalidContentTypeError)));
            testComplete.complete();
        }).putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(payload.length())).write(payload).end();
    }

    @Test
    public void letPassValidRequest(TestContext context) {
        Buffer payload = Buffer.buffer("<html>This is not a picture</html>");
        final Async testComplete = context.async();
        router.route("/").handler(new RequestBodyHandler(false, 0));
        router.route("/").handler(rc -> rc.response().setStatusCode(1337).end());
        client.post("/", response -> {
            TestUtils.failTestOnException(context, v -> assertThat(response.statusCode()).isEqualTo(1337));
            testComplete.complete();
        }).putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(payload.length())).write(payload).end();
    }
}