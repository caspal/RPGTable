package info.pascalkrause.rpgtable.web;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import info.pascalkrause.rpgtable.TestUtils;
import info.pascalkrause.rpgtable.data.Image;
import info.pascalkrause.rpgtable.utils.SimpleAsyncResult;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

public class ImageRouter_LIST_Test extends ImageRouterTest {

    @Test
    public void testEmpty(TestContext context) {
        SimpleAsyncResult<Collection<Image>> sar = new SimpleAsyncResult<Collection<Image>>(Collections.emptyList());
        doAnswer(TestUtils.createAsyncResultAnswer(sar)).when(imageApiMock).list(any());

        Async listImages = context.async();
        getClient().getNow("/v1/images", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                assertThat(response.statusCode()).isEqualTo(200);
                TestUtils.isJsonResponseValid(response, body, Collections.emptyList());
                listImages.complete();
            });
        });
    }

    @Test
    public void testNonEmpty(TestContext context) {
        List<Image> expectedImages = ImmutableList.of(new Image("I1", "someHash", 123, "somepath"),
                new Image("I2", "someHash", 123, "somepath"));
        SimpleAsyncResult<Collection<Image>> sar = new SimpleAsyncResult<Collection<Image>>(expectedImages);
        doAnswer(TestUtils.createAsyncResultAnswer(sar)).when(imageApiMock).list(any());

        Async listImages = context.async();
        getClient().getNow("/v1/images", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                assertThat(response.statusCode()).isEqualTo(200);
                TestUtils.isJsonResponseValid(response, body, expectedImages);
                listImages.complete();
            });
        });
    }

    @Test
    public void testThrowError(TestContext context) {
        SimpleAsyncResult<Collection<Image>> sar = new SimpleAsyncResult<Collection<Image>>(new Error());
        doAnswer(TestUtils.createAsyncResultAnswer(sar)).when(imageApiMock).list(any());

        Async listImages = context.async();
        getClient().getNow("/v1/images", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                TestUtils.isErrorResponseCorrect(response, body, ErrorResponse.newUnexpectedError());
                listImages.complete();
            });
        });
    }
}