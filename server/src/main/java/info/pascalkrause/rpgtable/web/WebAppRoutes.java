package info.pascalkrause.rpgtable.web;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class WebAppRoutes {

    public static Router createRouter(Vertx vertx) {
        Router router = Router.router(vertx);
        router.route("/").handler(StaticHandler.create());
        return router;
    }
}
