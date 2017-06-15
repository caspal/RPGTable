package info.pascalkrause.rpgtable;

import java.io.IOException;

import info.pascalkrause.rpgtable.handler.IndexHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;

public class WebRouterVerticle extends AbstractVerticle {
    @Override
    public void start(Future<Void> fut) throws IOException {
        Router router = Router.router(vertx);
        router.route("/").handler(new IndexHandler(getVertx()));

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