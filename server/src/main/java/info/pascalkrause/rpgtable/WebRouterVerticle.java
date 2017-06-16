package info.pascalkrause.rpgtable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

import com.google.common.annotations.VisibleForTesting;

import info.pascalkrause.rpgtable.api.ImageAPI;
import info.pascalkrause.rpgtable.data.NonPersistentImageStore;
import info.pascalkrause.rpgtable.handler.web.IndexHandler;
import info.pascalkrause.rpgtable.utils.RPGTableConfig;
import info.pascalkrause.rpgtable.web.ImageRouter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class WebRouterVerticle extends AbstractVerticle {

    private RPGTableConfig config;
    private Router router;
    private ImageAPI imageApi;

    public WebRouterVerticle() {
    };

    @VisibleForTesting
    public WebRouterVerticle(ImageAPI imageApi) {
        this.imageApi = imageApi;
    }

    private void createRoutes() {
        router.route("/").handler(new IndexHandler(getVertx()));
        router.mountSubRouter("/v1/images", new ImageRouter(imageApi).createRoutes(vertx));
    }

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        config = RPGTableConfig.create(vertx);
        if (!config.ENV_TEST) {
            System.out.println(config.toString());
        }
        if (Objects.isNull(imageApi)) {
            if (Objects.isNull(config.WORKSPACE_DIR)) {
                System.err.println("No value for setting " + RPGTableConfig.WORKSPACE_DIR_KEY + " found");
                System.exit(1);
            }
            imageApi = new NonPersistentImageStore(vertx.fileSystem(), config.WORKSPACE_DIR);
        }
        router = Router.router(vertx);
        createRoutes();
    }

    @Override
    public void start(Future<Void> fut) throws IOException, URISyntaxException {
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