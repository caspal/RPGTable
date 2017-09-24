package info.pascalkrause.rpgtable.web;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

import com.google.common.net.MediaType;

import info.pascalkrause.rpgtable.TestUtils;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.Router;

public class WebAppRoutesTest extends WebVerticleBaseTest {
    
    @Override
    protected Router initializeRouter(Vertx vertx) {
        return WebAppRoutes.createRouter(vertx);
    }
    
    @Test
    public void serveIndexHTMLTest(TestContext context) {
        final Async testComplete = context.async();
        client.getNow("/", response -> {
            response.bodyHandler(body -> {
                TestUtils.failTestOnException(context, v -> {
                    assertThat(response.statusCode()).isEqualTo(200);
                    assertThat(body.toString()).contains("<title>RPGTable</title>");
                    MediaType mt = MediaType.parse(response.getHeader(HttpHeaders.CONTENT_TYPE));
                    assertThat(mt).isEqualTo(MediaType.HTML_UTF_8);
                    testComplete.complete();
                });
            });
        });
    }
}
