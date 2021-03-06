package info.pascalkrause.rpgtable.web;

import java.util.Objects;
import java.util.UUID;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.net.MediaType;

import info.pascalkrause.rpgtable.api.ImageAPI;
import info.pascalkrause.rpgtable.error.BasicError;
import info.pascalkrause.rpgtable.error.UnexpectedError;
import info.pascalkrause.rpgtable.utils.RPGTableConfig;
import info.pascalkrause.rpgtable.web.handler.RequestBodyHandler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class ImageRoutes {
    private static final String URI_PARAM_NAME_OR_ID = "nameOrId";
    private static final String SLUG = "SLUG";
    private final ImageAPI imageApi;
    private RequestBodyHandler requestBodyHandler;

    public ImageRoutes(ImageAPI imageApi) {
        this.imageApi = imageApi;
    }

    @VisibleForTesting
    ImageRoutes(ImageAPI imageApi, RequestBodyHandler requestBodyHandler) {
        this.imageApi = imageApi;
        this.requestBodyHandler = requestBodyHandler;
    }

    public Router createRouter(Vertx vertx) {
        if (Objects.isNull(requestBodyHandler)) {
            requestBodyHandler = new RequestBodyHandler(RPGTableConfig.getOrCreate(vertx).BODY_SIZE_LIMIT_BYTES,
                    ImageAPI.getSupportedImageTypes());
        }
        Router router = Router.router(vertx);
        router.route().failureHandler(this::handleFailure);
        router.get("/").handler(this::list);
        router.post("/").handler(requestBodyHandler).consumes(MediaType.OCTET_STREAM.toString());
        router.post("/").handler(this::create).consumes(MediaType.OCTET_STREAM.toString());
        router.get("/:" + URI_PARAM_NAME_OR_ID).handler(this::get);
        router.delete("/:" + URI_PARAM_NAME_OR_ID).handler(this::delete);

        router.get("/:" + URI_PARAM_NAME_OR_ID + "/binary").handler(this::binary);
        return router;
    }

    private static HttpServerResponse addJSONHeader(HttpServerResponse response) {
        return response.putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString());
    }

    private String extractNameOrId(RoutingContext rc) {
        if (HttpMethod.POST.equals(rc.request().method())) {
            String name = rc.request().getHeader(SLUG);
            name = Objects.isNull(name) ? "image-" + UUID.randomUUID() : name;
            return name;
        } else {
            return rc.request().getParam(URI_PARAM_NAME_OR_ID);
        }
    }

    private void handleFailure(RoutingContext rc) {
        BasicError e = null;
        if ((rc.failure() instanceof BasicError)) {
            e = (BasicError) rc.failure();
        } else {
            // In case that Vertx produces an error
            e = new UnexpectedError(rc.failure());
        }
        switch (e.getType()) {
        case UNEXPECTED_ERROR:
            if (!RPGTableConfig.getOrCreate(rc.vertx()).ENV_TEST) {
                rc.failure().printStackTrace(System.err);
            }
            addJSONHeader(rc.response()).setStatusCode(e.getType().getStatusCode())
                    .end(e.getResponse().encodePrettily());
            break;
        case RESOURCE_NOT_FOUND:
            rc.response().setStatusCode(e.getType().getStatusCode()).end();
            break;
        default:
            addJSONHeader(rc.response()).setStatusCode(e.getType().getStatusCode())
                    .end(e.getResponse().encodePrettily());
            break;
        }
    }

    private void list(RoutingContext rc) {
        imageApi.list(res -> {
            if (res.failed()) {
                rc.fail(res.cause());
                return;
            }
            addJSONHeader(rc.response()).setStatusCode(200).end(Json.encodePrettily(res.result()));
        });
    }

    private void create(RoutingContext rc) {
        final String name = extractNameOrId(rc);
        imageApi.create(name, rc.getBody(), RequestBodyHandler.getParsedBodyType(rc), res -> {
            if (res.failed()) {
                rc.fail(res.cause());
            } else {
                addJSONHeader(rc.response()).setStatusCode(201).end(Json.encodePrettily(res.result()));
            }
        });
    }

    private void get(RoutingContext rc) {
        final String nameOrId = extractNameOrId(rc);
        imageApi.get(nameOrId, res -> {
            if (res.failed()) {
                rc.fail(res.cause());
            } else {
                addJSONHeader(rc.response()).setStatusCode(200).end(Json.encodePrettily(res.result()));
            }
        });
    }

    private void delete(RoutingContext rc) {
        final String nameOrId = extractNameOrId(rc);
        imageApi.delete(nameOrId, res -> {
            if (res.failed()) {
                rc.fail(res.cause());
            } else {
                rc.response().setStatusCode(204).end();
            }
        });
    }

    private void binary(RoutingContext rc) {
        final String nameOrId = extractNameOrId(rc);
        imageApi.get(nameOrId, res -> {
            if (res.failed()) {
                rc.fail(res.cause());
            } else {
                Buffer payload = Buffer.buffer(res.result().getContent());
                rc.response().putHeader(HttpHeaders.CONTENT_TYPE, res.result().getMediaType()).setStatusCode(200)
                        .end(payload);
            }
        });
    }
}