package info.pascalkrause.rpgtable;

import java.io.IOException;

import info.pascalkrause.rpgtable.api.ImageAPI;
import info.pascalkrause.rpgtable.api.ImageApiImpl;
import info.pascalkrause.rpgtable.data.MongoImageStore;
import info.pascalkrause.rpgtable.handler.IndexHandler;
import info.pascalkrause.rpgtable.utils.RPGTableConfig;
import info.pascalkrause.rpgtable.web.ImageRoutes;
import info.pascalkrause.vertx.mongodata.datasource.MongoClientDataSource;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;

public class WebRouterVerticle extends AbstractVerticle {

    private RPGTableConfig config;
    private MongoClient mc;

    private Router createRoutes(ImageAPI imageApi) {
        Router router = Router.router(vertx);
        router.route("/").handler(new IndexHandler(vertx));
        router.mountSubRouter("/v1/images", new ImageRoutes(imageApi).createRoutes(vertx));
        return router;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        config = RPGTableConfig.getOrCreate(vertx);
        if (!config.ENV_TEST) {
            System.out.println(config.toString());
        }
        JsonObject mongoConfig = new JsonObject();
        mongoConfig.put("host", config.MONGO_HOST);
        mongoConfig.put("port", config.MONGO_PORT);
        mongoConfig.put("db_name", "RPGTable");
        mc = MongoClient.createNonShared(vertx, mongoConfig);
    }

    @Override
    public void start(Future<Void> fut) throws IOException {
        Future<HttpServer> httpServerCompleted = Future.future();
        Future<MongoImageStore> imageStoreComplete = Future.future();
        MongoImageStore.createInstance("images", new MongoClientDataSource(mc), imageStoreComplete.completer());
        imageStoreComplete.compose(mis -> {
            Router router = createRoutes(new ImageApiImpl(mis));
            vertx.createHttpServer().requestHandler(router::accept).listen(config.HTTP_PORT,
                    httpServerCompleted.completer());
            httpServerCompleted.setHandler(res -> {
                if (res.succeeded()) {
                    fut.complete();
                } else {
                    fut.fail(res.cause());
                }
            });
        }, httpServerCompleted);
    }
}