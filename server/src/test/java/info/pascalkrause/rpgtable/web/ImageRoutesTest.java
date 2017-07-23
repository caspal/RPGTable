package info.pascalkrause.rpgtable.web;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.google.common.net.MediaType;

import info.pascalkrause.rpgtable.TestUtils;
import info.pascalkrause.rpgtable.api.ImageAPI;
import info.pascalkrause.rpgtable.data.Image;
import info.pascalkrause.rpgtable.error.BasicError;
import info.pascalkrause.rpgtable.error.ResourceNotFoundError;
import info.pascalkrause.rpgtable.error.UnexpectedError;
import info.pascalkrause.vertx.mongodata.SimpleAsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.Router;

public class ImageRoutesTest extends WebVerticleBaseTest {

    private ImageRoutes imageRoutes;
    private ImageAPI imageApiMock;

    @Override
    protected Router initializeRouter(Vertx vertx) {
        imageApiMock = mock(ImageAPI.class);
        imageRoutes = new ImageRoutes(imageApiMock);
        router = imageRoutes.createRoutes(vertx);
        return router;
    }

    @Test
    public void listImageRouteTest(TestContext context) {
        final Async testComplete = context.async();
        SimpleAsyncResult<List<Image>> sar = new SimpleAsyncResult<List<Image>>(Collections.emptyList());
        doAnswer(TestUtils.createAsyncResultAnswer(sar)).when(imageApiMock).list(any());
        client.getNow("/", response -> {
            response.bodyHandler(body -> {
                TestUtils.failTestOnException(context, v -> {
                    assertThat(body.toJsonArray()).hasSize(0);
                    assertThat(response.statusCode()).isEqualTo(200);
                    assertThat(response.headers().size()).isEqualTo(2);
                    assertThat(response.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.JSON_UTF_8.toString());
                    testComplete.complete();
                });
            });
        });
    }

    @Test
    public void createImageRouteTest(TestContext context) throws IOException, InterruptedException {
        final Async testComplete = context.async();
        Buffer payload = TestUtils.readFile("images/TestImage.gif");
        Image expected = new Image("TestImage", "hash", payload.length(), payload.getBytes(), MediaType.GIF.toString());
        SimpleAsyncResult<Image> sar = new SimpleAsyncResult<Image>(expected);
        doAnswer(TestUtils.createAsyncResultAnswer(sar)).when(imageApiMock).create(eq(expected.getName()), eq(payload),
                eq(MediaType.GIF), any());
        client.post("/", response -> {
            response.bodyHandler(body -> {
                TestUtils.failTestOnException(context, v -> {
                    assertThat(body.toString()).isEqualTo(Json.encodePrettily(expected));
                    assertThat(response.statusCode()).isEqualTo(201);
                    assertThat(response.headers().size()).isEqualTo(2);
                    assertThat(response.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.JSON_UTF_8.toString());
                    testComplete.complete();
                });
            });
        }).putHeader("SLUG", expected.getName()).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.OCTET_STREAM.toString())
                .putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(payload.length())).write(payload).end();
    }

    @Test
    public void getImageRouteTest(TestContext context) throws IOException {
        final Async testComplete = context.async();
        Buffer payload = TestUtils.readFile("images/TestImage.gif");
        Image expected = new Image("TestImage", "hash", payload.length(), payload.getBytes(), MediaType.GIF.toString());
        SimpleAsyncResult<Image> sar = new SimpleAsyncResult<Image>(expected);
        doAnswer(TestUtils.createAsyncResultAnswer(sar)).when(imageApiMock).get(eq(expected.getName()), any());
        client.getNow("/" + expected.getName(), response -> {
            response.bodyHandler(body -> {
                TestUtils.failTestOnException(context, v -> {
                    assertThat(body.toString()).isEqualTo(Json.encodePrettily(expected));
                    assertThat(response.statusCode()).isEqualTo(200);
                    assertThat(response.headers().size()).isEqualTo(2);
                    assertThat(response.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.JSON_UTF_8.toString());
                    testComplete.complete();
                });
            });
        });
    }

    @Test
    public void deleteImageRouteTest(TestContext context) {
        final Async testComplete = context.async();
        String imageName = "imageToDelete";
        SimpleAsyncResult<Void> sar = new SimpleAsyncResult<Void>((Void) null);
        doAnswer(TestUtils.createAsyncResultAnswer(sar)).when(imageApiMock).delete(eq(imageName), any());
        client.delete("/" + imageName, response -> {
            TestUtils.failTestOnException(context, v -> {
                assertThat(response.statusCode()).isEqualTo(204);
                assertThat(response.headers()).hasSize(1);
                assertThat(response.getHeader(HttpHeaders.CONTENT_LENGTH)).isEqualTo("0");
                testComplete.complete();
            });
        }).end();
    }

    @Test
    public void errorHandlingImageRouteTest(TestContext context) throws IOException {
        final Async testComplete = context.async();
        String imageName = "NotExisting";
        BasicError expected = new UnexpectedError(null);
        SimpleAsyncResult<Image> sar = new SimpleAsyncResult<Image>(expected);
        doAnswer(TestUtils.createAsyncResultAnswer(sar)).when(imageApiMock).get(eq(imageName), any());
        client.getNow("/" + imageName, response -> {
            response.bodyHandler(body -> {
                TestUtils.failTestOnException(context, v -> {
                    assertThat(body.toJsonObject()).isEqualTo(expected.getResponse());
                    assertThat(response.statusCode()).isEqualTo(500);
                    assertThat(response.headers().size()).isEqualTo(2);
                    assertThat(response.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.JSON_UTF_8.toString());
                    testComplete.complete();
                });
            });
        });
    }

    @Test
    public void resourceNotFoundImageRouteTest(TestContext context) {
        final Async testComplete = context.async();
        String imageName = "NotExisting";
        SimpleAsyncResult<Image> sar = new SimpleAsyncResult<Image>(new ResourceNotFoundError("Image", imageName));
        doAnswer(TestUtils.createAsyncResultAnswer(sar)).when(imageApiMock).get(eq(imageName), any());
        client.getNow("/" + imageName, response -> {
            TestUtils.failTestOnException(context, v -> {
                assertThat(response.statusCode()).isEqualTo(404);
                assertThat(response.headers()).hasSize(1);
                assertThat(response.getHeader(HttpHeaders.CONTENT_LENGTH)).isEqualTo("0");
                testComplete.complete();
            });
        });
    }
}
