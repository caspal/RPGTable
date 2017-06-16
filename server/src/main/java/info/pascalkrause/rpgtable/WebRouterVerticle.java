package info.pascalkrause.rpgtable;

import java.io.IOException;
import java.net.URISyntaxException;

import info.pascalkrause.rpgtable.api.ImageAPI;
import info.pascalkrause.rpgtable.handler.web.IndexHandler;
import info.pascalkrause.rpgtable.utils.RPGTableConfig;
import info.pascalkrause.rpgtable.web.ImageRouter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;

public class WebRouterVerticle extends AbstractVerticle {

    private ImageRouter imageRouter;

    private Router createRouter() {
        Router router = Router.router(vertx);
        router.route("/").handler(new IndexHandler(getVertx()));
        router.mountSubRouter("/v1/images", imageRouter.createRoutes(vertx));
        return router;
    }

    @Override
    public void start(Future<Void> fut) throws IOException, URISyntaxException {
        imageRouter = new ImageRouter(new ImageAPI(vertx));
        Router router = createRouter();
        RPGTableConfig config = RPGTableConfig.create(vertx);
        if (!config.ENV_TEST) {
            System.out.println(config.toString());
        }
        
        // Create the HTTP server and pass the "accept" method to the request
        // handler.
        int port = config.HTTP_PORT;
        vertx.createHttpServer().requestHandler(router::accept).listen(
                // Retrieve the port from the configuration,
                // default to 8080.
                port, result -> {
                    if (result.succeeded()) {
                        fut.complete();
                    } else {
                        fut.fail(result.cause());
                    }
                });
    }
}