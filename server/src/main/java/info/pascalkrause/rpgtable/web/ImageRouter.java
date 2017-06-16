package info.pascalkrause.rpgtable.web;

import java.util.Objects;
import java.util.UUID;

import com.google.common.net.MediaType;

import info.pascalkrause.rpgtable.api.ImageAPI;
import info.pascalkrause.rpgtable.error.BasicError;
import info.pascalkrause.rpgtable.error.ErrorType;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class ImageRouter {
    private static final String URI_PARAM_NAME_OR_ID = "nameOrId";
    private static final String SLUG = "SLUG";
    private final ImageAPI imageApi;

    public ImageRouter(ImageAPI imageApi) {
        this.imageApi = imageApi;
    }

    public Router createRoutes(Vertx vertx) {
        Router router = Router.router(vertx);
        router.route().failureHandler(this::handleFailure);

        router.get("/").handler(this::list);
        router.post("/").handler(this::create).consumes(MediaType.OCTET_STREAM.toString());
        router.get("/:" + URI_PARAM_NAME_OR_ID).handler(this::get);
        router.delete("/:" + URI_PARAM_NAME_OR_ID).handler(this::delete);
        return router;
    }

    private static void sendErrorResponse(ErrorResponse resp, RoutingContext rc) {
        sendJsonResponse(resp.getStatusCode(), resp, rc.response());
    }

    private static void sendJsonResponse(int statuscode, Object body, HttpServerResponse response) {
        response.setStatusCode(statuscode).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
                .end(Json.encodePrettily(body));
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
        if (!(rc.failure() instanceof BasicError)) {
            // In case that Vertx produces an error
            rc.failure().printStackTrace(System.err);
            sendErrorResponse(ErrorResponse.newUnexpectedError(), rc);
            return;
        }
        BasicError e = (BasicError) rc.failure();
        switch (e.getType()) {
        case IMAGE_CREATE_EMPTY_BODY:
            sendErrorResponse(ErrorResponse.newImageBodyIsEmptyError(), rc);
            break;
        case RESOURCE_ALREADY_EXIST:
            sendErrorResponse(ErrorResponse.newImageResourceAlreadyExistError(extractNameOrId(rc)), rc);
            break;
        case RESOURCE_NOT_FOUND:
            rc.response().setStatusCode(404).end();
            break;
        default:
            rc.failure().printStackTrace(System.err);
            sendErrorResponse(ErrorResponse.newUnexpectedError(), rc);
            break;
        }
    }

    private void list(RoutingContext rc) {
        imageApi.list(res -> {
            if (res.failed()) {
                rc.fail(res.cause());
            } else {
                sendJsonResponse(200, res.result(), rc.response());
            }
        });
    }

    private void create(RoutingContext rc) {
        final String name = extractNameOrId(rc);
        rc.request().bodyHandler(buffer -> {
            if (buffer.length() == 0) {
                rc.fail(new BasicError(ErrorType.IMAGE_CREATE_EMPTY_BODY));
                return;
            }
            imageApi.create(name, buffer, res -> {
                if (res.failed()) {
                    rc.fail(res.cause());
                } else {
                    sendJsonResponse(201, res.result(), rc.response());
                }
            });
        });
    }

    private void get(RoutingContext rc) {
        final String nameOrId = extractNameOrId(rc);
        imageApi.get(nameOrId, res -> {
            if (res.failed()) {
                rc.fail(res.cause());
            } else {
                sendJsonResponse(200, res.result(), rc.response());
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
}