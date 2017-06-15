package info.pascalkrause.rpgtable;

import com.google.common.io.Resources;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;

public class WebRouterVerticle extends AbstractVerticle {

    private static final String WEB_ROOT_FOLDER = "webroot/";

    @Override
    public void start(Future<Void> fut) {
        Router router = Router.router(vertx);
        String pathIndexHtml = Resources.getResource(WEB_ROOT_FOLDER + "index.html").getFile();
        router.route("/").handler(ctx -> {
            ctx.response().sendFile(pathIndexHtml);
        });

        // Create the HTTP server and pass the "accept" method to the request handler.
        vertx.createHttpServer().requestHandler(router::accept).listen(
                // Retrieve the port from the configuration,
                // default to 8080.
                config().getInteger("http.port", 8080), result -> {
                    if (result.succeeded()) {
                        fut.complete();
                    } else {
                        fut.fail(result.cause());
                    }
                });
    }
}