package info.pascalkrause.rpgtable.web;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

import java.io.IOException;
import java.security.SecureRandom;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.net.MediaType;

import info.pascalkrause.rpgtable.TestUtils;
import info.pascalkrause.rpgtable.api.ImageAPI;
import info.pascalkrause.rpgtable.data.Image;
import info.pascalkrause.rpgtable.error.BasicError;
import info.pascalkrause.rpgtable.error.EmptyRequestBodyError;
import info.pascalkrause.rpgtable.error.InvalidContentTypeError;
import info.pascalkrause.rpgtable.error.RequestBodyTooLargeError;
import info.pascalkrause.rpgtable.error.ResourceAlreadyExistError;
import info.pascalkrause.rpgtable.error.UnexpectedError;
import info.pascalkrause.rpgtable.utils.SimpleAsyncResult;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

public class ImageRouter_CREATE_Test extends ImageRouterTest {

    private static Buffer payload;

    @BeforeClass
    public static void init() throws IOException {
        payload = TestUtils.readFile("images/TestImage.gif");
    }

    @Test
    public void testWithName(TestContext context) {
        Image expected = new Image("TestName", "somehash", 123, "somePath");
        SimpleAsyncResult<Image> sar = new SimpleAsyncResult<Image>(expected);
        doAnswer(TestUtils.createAsyncResultAnswer(sar)).when(imageApiMock).create(eq(expected.getName()), eq(payload),
                any());

        Async createImage = context.async();
        getClient().post("/v1/images", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                assertThat(response.statusCode()).isEqualTo(201);
                TestUtils.isJsonResponseValid(response, body, expected);
                createImage.complete();
            });
        }).putHeader("SLUG", expected.getName()).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.OCTET_STREAM.toString())
                .putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(payload.length())).write(payload).end();
    }

    @Test
    public void testWithoutName(TestContext context) {
        String key = "ImageRouterCreateTestWithoutName-Image";
        doAnswer(TestUtils.createAsyncResultAnswer(i -> {
            Image expexted = new Image(i.getArgument(0).toString(), "somehash", 123, "somePath");
            context.put(key, expexted);
            return new SimpleAsyncResult<Image>(expexted);
        })).when(imageApiMock).create(any(), eq(payload), any());

        Async createImage = context.async();
        getClient().post("/v1/images", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                TestUtils.isJsonResponseValid(response, body, context.get(key));
                createImage.complete();
            });
        }).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.OCTET_STREAM.toString())
                .putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(payload.length())).write(payload).end();
    }

    @Test
    public void testThrowResourceAlreadyExistError(TestContext context) {
        String name = "TestName";
        BasicError expected = new ResourceAlreadyExistError("Image", name);
        SimpleAsyncResult<Image> sar = new SimpleAsyncResult<Image>(expected);
        doAnswer(TestUtils.createAsyncResultAnswer(sar)).when(imageApiMock).create(any(), any(), any());

        Async createImage = context.async();
        getClient().post("/v1/images", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                TestUtils.isErrorResponseCorrect(response, body, expected);
                createImage.complete();
            });
        }).putHeader("SLUG", name).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.OCTET_STREAM.toString())
                .putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(payload.length())).write(payload).end();
    }

    @Test
    public void testThrowEmptyBodyError(TestContext context) {
        Async createImage = context.async();
        getClient().post("/v1/images", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                TestUtils.isErrorResponseCorrect(response, body, new EmptyRequestBodyError());
                createImage.complete();
            });
        }).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.OCTET_STREAM.toString()).end();
    }

    @Test
    public void testThrowRequestBodyTooLargeError(TestContext context) {
        Buffer payload = Buffer.buffer(SecureRandom.getSeed(config.BODY_SIZE_LIMIT_BYTES + 1));

        Async createImage = context.async();
        getClient().post("/v1/images", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                TestUtils.isErrorResponseCorrect(response, body,
                        new RequestBodyTooLargeError(payload.length(), config.BODY_SIZE_LIMIT_BYTES));
                createImage.complete();
            });
        }).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.OCTET_STREAM.toString())
                .putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(payload.length())).write(payload).end();
    }

    @Test
    public void testThrowInvalidContentTypeError(TestContext context) {
        Buffer payload = Buffer.buffer("<html>This is not a picture</html>");

        Async createImage = context.async();
        getClient().post("/v1/images", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                TestUtils.isErrorResponseCorrect(response, body, new InvalidContentTypeError(
                        MediaType.HTML_UTF_8.withoutParameters(), ImageAPI.getSupportedImageTypes()));
                createImage.complete();
            });
        }).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.OCTET_STREAM.toString())
                .putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(payload.length())).write(payload).end();
    }

    @Test
    public void testThrowUnexpectedError(TestContext context) {
        SimpleAsyncResult<Image> sar = new SimpleAsyncResult<Image>(new Error());
        doAnswer(TestUtils.createAsyncResultAnswer(sar)).when(imageApiMock).create(any(), any(), any());

        Async createImage = context.async();
        getClient().post("/v1/images", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                TestUtils.isErrorResponseCorrect(response, body, new UnexpectedError(null));
                createImage.complete();
            });
        }).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.OCTET_STREAM.toString())
                .putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(payload.length())).write(payload).end();
    }
}