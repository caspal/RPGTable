package info.pascalkrause.rpgtable;

import static com.google.common.truth.Truth.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.common.net.MediaType;

import info.pascalkrause.rpgtable.error.BasicError;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class TestUtils {

    public static int getFreePort() throws IOException {
        ServerSocket socket = new ServerSocket(0);
        int port = socket.getLocalPort();
        socket.close();
        return port;
    }

    public static Buffer readFile(String ressourcePath) throws IOException {
        return Buffer.buffer(Resources.asByteSource(Resources.getResource(ressourcePath)).read());
    }

    public static String createTempWorkspaceDir() {
        File dir = Files.createTempDir();
        return dir.getAbsolutePath();
    }

    public static <E> Answer<Void> createAsyncResultAnswer(Function<InvocationOnMock, AsyncResult<E>> resultbuilder) {
        Answer<Void> a = (invocation) -> {
            List<Object> args = ImmutableList.copyOf(invocation.getArguments());
            Optional<Object> handler = args.stream().filter(o -> o instanceof Handler).findFirst();
            if (handler.isPresent() && handler.get() instanceof Handler) {
                @SuppressWarnings("unchecked")
                Handler<AsyncResult<E>> h = (Handler<AsyncResult<E>>) handler.get();
                h.handle(resultbuilder.apply(invocation));
            } else {
                throw new RuntimeException("No Handler was found");
            }
            return null;
        };
        return a;
    }

    public static <E> Answer<Void> createAsyncResultAnswer(AsyncResult<E> result) {
        return createAsyncResultAnswer(i -> result);
    }

    private static void checkHeaders(HttpClientResponse response, int expectedContentLength) {
        assertThat(response.headers().size()).isEqualTo(2);
        assertThat(response.headers().get(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.JSON_UTF_8.toString());
        int contentLength = Integer.parseInt(response.headers().get(HttpHeaders.CONTENT_LENGTH));
        assertThat(contentLength).isEqualTo(expectedContentLength);
    }

    public static void isJsonResponseValid(HttpClientResponse response, Buffer body, Object expected) {
        Buffer json = Buffer.buffer(Json.encodePrettily(expected));
        checkHeaders(response, json.length());
        if (expected instanceof Collection<?>) {
            assertThat(body.toJsonArray()).isEqualTo(json.toJsonArray());
        } else {
            assertThat(body.toJsonObject()).isEqualTo(json.toJsonObject());
        }
    }

    public static void hasEmptyBody(HttpClientResponse response) {
        int contentLength = Integer.parseInt(response.headers().get(HttpHeaders.CONTENT_LENGTH));
        assertThat(contentLength).isEqualTo(0);
    }

    public static void isErrorResponseCorrect(HttpClientResponse response, Buffer body, BasicError expected) {

        assertThat(response.statusCode()).isEqualTo(expected.getType().getStatusCode());
        assertThat(response.headers()).hasSize(2);
        int contentLength = Integer.parseInt(response.headers().get(HttpHeaders.CONTENT_LENGTH));
        assertThat(contentLength).isEqualTo(expected.getResponse().encodePrettily().length());
        assertThat(response.headers().get(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.JSON_UTF_8.toString());

        // Remove timestamp because they are never the same in two different error responses
        String timestampKey = "timestamp";
        JsonObject expectedAsJson = expected.getResponse();
        JsonObject responseBodyAsJson = body.toJsonObject();
        expectedAsJson.remove(timestampKey);
        responseBodyAsJson.remove(timestampKey);
        assertThat(responseBodyAsJson).containsExactlyElementsIn(expectedAsJson);
    }
}