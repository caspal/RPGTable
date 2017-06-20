package info.pascalkrause.rpgtable.web;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

import org.junit.Test;

import info.pascalkrause.rpgtable.TestUtils;
import info.pascalkrause.rpgtable.error.BasicError;
import info.pascalkrause.rpgtable.error.ErrorType;
import info.pascalkrause.rpgtable.utils.SimpleAsyncResult;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

public class ImageRouter_DELETE_Test extends ImageRouterTest {

    @Test
    public void test(TestContext context) {
        String name = "TestName";
        SimpleAsyncResult<Void> sar = new SimpleAsyncResult<Void>((Void) null);
        doAnswer(TestUtils.createAsyncResultAnswer(sar)).when(imageApiMock).delete(eq(name), any());

        Async deleteImage = context.async();
        getClient().delete("/v1/images/" + name, response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                assertThat(response.statusCode()).isEqualTo(204);
                assertThat(response.headers()).hasSize(1);
                TestUtils.hasEmptyBody(response);
                deleteImage.complete();
            });
        }).end();
    }

    @Test
    public void testResourceNotFound(TestContext context) {
        SimpleAsyncResult<Void> sar = new SimpleAsyncResult<Void>(new BasicError(ErrorType.RESOURCE_NOT_FOUND));
        doAnswer(TestUtils.createAsyncResultAnswer(sar)).when(imageApiMock).delete(any(), any());

        Async deleteImage = context.async();
        getClient().delete("/v1/images/abc", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                assertThat(response.statusCode()).isEqualTo(404);
                assertThat(response.headers()).hasSize(1);
                TestUtils.hasEmptyBody(response);
                deleteImage.complete();
            });
        }).end();
    }

    @Test
    public void testThrowError(TestContext context) {
        SimpleAsyncResult<Void> sar = new SimpleAsyncResult<Void>(new Error());
        doAnswer(TestUtils.createAsyncResultAnswer(sar)).when(imageApiMock).delete(any(), any());

        Async deleteImage = context.async();
        getClient().delete("/v1/images/abc", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                TestUtils.isErrorResponseCorrect(response, body, ErrorResponse.newUnexpectedError());
                deleteImage.complete();
            });
        }).end();
    }
}