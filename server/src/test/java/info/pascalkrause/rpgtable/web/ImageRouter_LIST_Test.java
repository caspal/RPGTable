package info.pascalkrause.rpgtable.web;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

import com.google.common.net.MediaType;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

public class ImageRouter_LIST_Test extends ImageRouterTest {
    
    @Test
    public void testEmptyList(TestContext context) {
        final Async listImages = context.async();
        getClient().getNow("/v1/images", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                assertThat(response.statusCode()).isEqualTo(200);
                assertThat(response.headers()).hasSize(2);
                assertThat(response.headers().get(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.JSON_UTF_8.toString());
                assertThat(response.headers().get(HttpHeaders.CONTENT_LENGTH)).isEqualTo("3");
                assertThat(body.toJsonArray()).isEmpty();
                listImages.complete();
            });
        });
    }
    
    @Test
    public void testNonEmptyList(TestContext context) {
        final Async createImage = context.async();
        
        final Async async = context.async();
        getClient().getNow("/v1/images", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                assertThat(response.statusCode()).isEqualTo(200);
                assertThat(response.headers()).hasSize(2);
                assertThat(response.headers().get(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.JSON_UTF_8.toString());
                assertThat(response.headers().get(HttpHeaders.CONTENT_LENGTH)).isEqualTo("3");
                assertThat(body.toJsonArray()).isEmpty();
                async.complete();
            });
        });
    }
}