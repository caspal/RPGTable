package info.pascalkrause.rpgtable;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

public class ServeIndexHTMLTest extends AbstractWebRouterVerticleTest {

    @Test
    public void serveIndexHTMLTest(TestContext context) {
        final Async async = context.async();

        getClient().getNow("/", response -> {
            response.exceptionHandler(context.exceptionHandler());
            response.bodyHandler(body -> {
                assertThat(response.statusCode()).isEqualTo(200);
                assertThat(response.getHeader(HttpHeaders.CONTENT_TYPE).equals(HttpHeaders.TEXT_HTML));
                assertThat(body.toString()).contains("Welcome");
                async.complete();
            });
        });
    }
}