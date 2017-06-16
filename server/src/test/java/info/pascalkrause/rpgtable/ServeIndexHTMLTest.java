package info.pascalkrause.rpgtable;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

public class ServeIndexHTMLTest extends AbstractWebRouterVerticleTest {

    /**
     * Let's ensure that our application behaves correctly.
     *
     * @param context
     *            the test context
     */
    @Test
    public void serveIndexHTMLTest(TestContext context) {
        // This test is asynchronous, so get an async handler to inform the test
        // when we are done.
        final Async async = context.async();

        // We create a HTTP client and query our application. When we get the response we check it contains
        // the 'Welcome' message. Then, we call the `complete` method on the async handler to declare
        // this async (and here the test) done. Notice that the assertions are made on the 'context'
        // object and are not Junit assert. This ways it manage the async aspect of the test the right way.
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