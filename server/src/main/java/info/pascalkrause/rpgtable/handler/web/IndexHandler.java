package info.pascalkrause.rpgtable.handler.web;

import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

public class IndexHandler implements Handler<RoutingContext> {
    private static final String WEB_ROOT_FOLDER = "webroot/";
    private static final String INDEX_HTML = WEB_ROOT_FOLDER + "index.html";

    private final Vertx vertx;

    private final Handler<Future<String>> loadIndexHtml = (future) -> {
        try {
            String body = Resources.toString(Resources.getResource(INDEX_HTML), Charsets.UTF_8);
            future.complete(body);
        } catch (IOException e) {
            future.fail(e);
        }
    };

    public IndexHandler(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void handle(RoutingContext ctx) {
        vertx.executeBlocking(loadIndexHtml, result -> {
            if (result.failed()) {
                result.cause().printStackTrace();
                ctx.response().setStatusCode(500).end();
            } else {
                ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_HTML).end(result.result());
            }
        });
    }
}