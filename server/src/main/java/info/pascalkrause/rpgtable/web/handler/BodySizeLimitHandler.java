package info.pascalkrause.rpgtable.web.handler;

import java.util.Objects;

import info.pascalkrause.rpgtable.error.RequestBodyTooLargeError;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

public class BodySizeLimitHandler implements Handler<RoutingContext> {

    private final int BODY_SIZE_LIMIT_BYTES;

    public BodySizeLimitHandler(int bodySizeLimitBytes) {
        BODY_SIZE_LIMIT_BYTES = bodySizeLimitBytes;
    }

    @Override
    public void handle(RoutingContext rc) {
        String s = rc.request().getHeader(HttpHeaders.CONTENT_LENGTH);
        if (Objects.nonNull(s)) {
            int contentLength = Integer.parseInt(s);
            if (contentLength > BODY_SIZE_LIMIT_BYTES) {
                rc.fail(new RequestBodyTooLargeError(contentLength, BODY_SIZE_LIMIT_BYTES));
            }
        }
        rc.next();
    }
}