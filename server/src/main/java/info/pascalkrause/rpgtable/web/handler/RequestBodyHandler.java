package info.pascalkrause.rpgtable.web.handler;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.net.MediaType;

import info.pascalkrause.rpgtable.error.RequestBodyTooLargeError;
import info.pascalkrause.rpgtable.utils.MediaTypeDetector;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

public class RequestBodyHandler implements Handler<RoutingContext> {

    private final int BODY_SIZE_LIMIT_BYTES;
    private final Set<MediaType> ALLOWED_CONTENT_TYPES;

    /**
     * Allows all content types
     * 
     * @param maxRequestBodySizeBytes
     */
    public RequestBodyHandler(int maxRequestBodySizeBytes) {
        this(maxRequestBodySizeBytes, Collections.emptySet());
    }

    public RequestBodyHandler(int maxRequestBodySizeBytes, Set<MediaType> allowedContentTypes) {
        BODY_SIZE_LIMIT_BYTES = maxRequestBodySizeBytes;
        ALLOWED_CONTENT_TYPES = ImmutableSet.copyOf(allowedContentTypes);
    }

    @Override
    public void handle(RoutingContext rc) {
        String lengthHeader = rc.request().getHeader(HttpHeaders.CONTENT_LENGTH);
        if (Objects.nonNull(lengthHeader)) {
            int contentLength = Integer.parseInt(lengthHeader);
            if (contentLength > BODY_SIZE_LIMIT_BYTES) {
                rc.fail(new RequestBodyTooLargeError(contentLength, BODY_SIZE_LIMIT_BYTES));
                return;
            }
        }
        rc.request().bodyHandler(buffer -> {
            rc.setBody(buffer);
            if (ALLOWED_CONTENT_TYPES.isEmpty()) {
                rc.next();
                return;
            }
            MediaTypeDetector.getInstance(rc.vertx()).detect(buffer, type -> {
                if (type.failed()) {
                    rc.fail(type.cause());
                    return;
                }
                if(!ALLOWED_CONTENT_TYPES.contains(type.result())) {
                    rc.fail(type.cause());
                    return;
                }
                rc.next();
            });
        });
    }
}