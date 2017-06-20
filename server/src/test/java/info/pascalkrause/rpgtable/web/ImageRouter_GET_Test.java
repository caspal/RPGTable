package info.pascalkrause.rpgtable.web;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

import org.junit.Test;

import info.pascalkrause.rpgtable.TestUtils;
import info.pascalkrause.rpgtable.data.Image;
import info.pascalkrause.rpgtable.error.ResourceNotFoundError;
import info.pascalkrause.rpgtable.error.UnexpectedError;
import info.pascalkrause.rpgtable.utils.SimpleAsyncResult;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

public class ImageRouter_GET_Test extends ImageRouterTest {

    @Test
    public void testContent(TestContext context) {
        Image image = new Image("TestName", "somehash", 123, "somePath");
        Buffer expected = Buffer.buffer("Some Content");
        SimpleAsyncResult<Buffer> sar = new SimpleAsyncResult<Buffer>(expected);
        doAnswer(TestUtils.createAsyncResultAnswer(sar)).when(imageApiMock).getResource(eq(image.getName()), any());

        Async getContent = context.async();
        getClient().getNow("/v1/images/" + image.getName() + "/binary", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                assertThat(response.statusCode()).isEqualTo(200);
                assertThat(body).isEqualTo(expected);
                getContent.complete();
            });
        });
    }

    @Test
    public void test(TestContext context) {
        Image expected = new Image("TestName", "somehash", 123, "somePath");
        SimpleAsyncResult<Image> sar = new SimpleAsyncResult<Image>(expected);
        doAnswer(TestUtils.createAsyncResultAnswer(sar)).when(imageApiMock).get(eq(expected.getName()), any());

        Async getImage = context.async();
        getClient().getNow("/v1/images/" + expected.getName(), response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                assertThat(response.statusCode()).isEqualTo(200);
                TestUtils.isJsonResponseValid(response, body, expected);
                getImage.complete();
            });
        });
    }

    @Test
    public void testResourceNotFound(TestContext context) {
        SimpleAsyncResult<Image> sar = new SimpleAsyncResult<Image>(new ResourceNotFoundError("Image", "doesntMatter"));
        doAnswer(TestUtils.createAsyncResultAnswer(sar)).when(imageApiMock).get(any(), any());

        Async getImage = context.async();
        getClient().getNow("/v1/images/abc", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                assertThat(response.statusCode()).isEqualTo(404);
                assertThat(response.headers()).hasSize(1);
                TestUtils.hasEmptyBody(response);
                getImage.complete();
            });
        });
    }

    @Test
    public void testThrowError(TestContext context) {
        SimpleAsyncResult<Image> sar = new SimpleAsyncResult<Image>(new Error());
        doAnswer(TestUtils.createAsyncResultAnswer(sar)).when(imageApiMock).get(any(), any());

        Async getImage = context.async();
        getClient().getNow("/v1/images/abc", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                TestUtils.isErrorResponseCorrect(response, body, new UnexpectedError(null));
                getImage.complete();
            });
        });
    }
}