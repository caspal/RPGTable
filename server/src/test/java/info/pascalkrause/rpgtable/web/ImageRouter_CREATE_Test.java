package info.pascalkrause.rpgtable.web;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

import org.junit.Test;

import com.google.common.net.MediaType;

import info.pascalkrause.rpgtable.TestUtils;
import info.pascalkrause.rpgtable.data.Image;
import info.pascalkrause.rpgtable.error.BasicError;
import info.pascalkrause.rpgtable.error.ErrorType;
import info.pascalkrause.rpgtable.utils.SimpleAsyncResult;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

public class ImageRouter_CREATE_Test extends ImageRouterTest {

    @Test
    public void testWithName(TestContext context) {
        Image expected = new Image("TestName", "somehash", 123, "somePath");
        SimpleAsyncResult<Image> sar = new SimpleAsyncResult<Image>(expected);
        doAnswer(TestUtils.createAsyncResultAnswer(sar)).when(imageApiMock).create(eq(expected.getName()), any(),
                any());

        Buffer paylaod = Buffer.buffer("someContent");
        Async createImage = context.async();
        getClient().post("/v1/images", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                assertThat(response.statusCode()).isEqualTo(201);
                TestUtils.isJsonResponseValid(response, body, expected);
                createImage.complete();
            });
        }).putHeader("SLUG", expected.getName()).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.OCTET_STREAM.toString())
                .putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(paylaod.length())).write(paylaod).end();
    }

    @Test
    public void testWithoutName(TestContext context) {
        String key = "ImageRouterCreateTestWithoutName-Image";
        doAnswer(TestUtils.createAsyncResultAnswer(i -> {
            Image expexted = new Image(i.getArgument(0).toString(), "somehash", 123, "somePath");
            context.put(key, expexted);
            return new SimpleAsyncResult<Image>(expexted);
        })).when(imageApiMock).create(any(), any(), any());

        Buffer paylaod = Buffer.buffer("someContent");
        Async createImage = context.async();
        getClient().post("/v1/images", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                TestUtils.isJsonResponseValid(response, body, context.get(key));
                createImage.complete();
            });
        }).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.OCTET_STREAM.toString())
                .putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(paylaod.length())).write(paylaod).end();
    }

    @Test
    public void testThrowResourceAlreadyExistError(TestContext context) {
        String name = "TestName";
        SimpleAsyncResult<Image> sar = new SimpleAsyncResult<Image>(new BasicError(ErrorType.RESOURCE_ALREADY_EXIST));
        doAnswer(TestUtils.createAsyncResultAnswer(sar)).when(imageApiMock).create(any(), any(), any());

        Buffer paylaod = Buffer.buffer("someContent");
        Async createImage = context.async();
        getClient().post("/v1/images", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                TestUtils.isErrorResponseCorrect(response, body, ErrorResponse.newImageResourceAlreadyExistError(name));
                createImage.complete();
            });
        }).putHeader("SLUG", name).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.OCTET_STREAM.toString())
                .putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(paylaod.length())).write(paylaod).end();
    }

    @Test
    public void testThrowEmptyBodyError(TestContext context) {
        Async createImage = context.async();
        getClient().post("/v1/images", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                TestUtils.isErrorResponseCorrect(response, body, ErrorResponse.newImageBodyIsEmptyError());
                createImage.complete();
            });
        }).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.OCTET_STREAM.toString()).end();
    }

    @Test
    public void testThrowUnexpectedError(TestContext context) {
        SimpleAsyncResult<Image> sar = new SimpleAsyncResult<Image>(new Error());
        doAnswer(TestUtils.createAsyncResultAnswer(sar)).when(imageApiMock).create(any(), any(), any());

        Buffer paylaod = Buffer.buffer("someContent");
        Async createImage = context.async();
        getClient().post("/v1/images", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                TestUtils.isErrorResponseCorrect(response, body, ErrorResponse.newUnexpectedError());
                createImage.complete();
            });
        }).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.OCTET_STREAM.toString())
                .putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(paylaod.length())).write(paylaod).end();
    }
}