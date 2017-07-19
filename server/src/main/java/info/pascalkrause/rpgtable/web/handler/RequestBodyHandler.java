package info.pascalkrause.rpgtable.web.handler;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.net.MediaType;
import com.google.common.primitives.Ints;

import info.pascalkrause.rpgtable.error.EmptyRequestBodyError;
import info.pascalkrause.rpgtable.error.InvalidContentTypeError;
import info.pascalkrause.rpgtable.error.RequestBodyTooLargeError;
import info.pascalkrause.rpgtable.utils.MediaTypeDetector;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

public class RequestBodyHandler implements Handler<RoutingContext> {

    private final int BODY_SIZE_LIMIT_BYTES;
    private final Set<MediaType> ALLOWED_CONTENT_TYPES;
    private final boolean REJECT_EMPTY_BODY;

    /**
     *
     * @param maxRequestBodySizeBytes
     *            The maximum bytes for a request body, or 0 to allow any size.
     * @param allowedTypes
     *            The allowed types. Pass null to allow everything.
     * @param rejectEmptyBody
     *            If true, the handler will response with a @EmptyRequestBodyError, in case of an empty body.
     *
     */
    public RequestBodyHandler(int maxRequestBodySizeBytes, Set<MediaType> allowedTypes, boolean rejectEmptyBody) {
        BODY_SIZE_LIMIT_BYTES = maxRequestBodySizeBytes;
        ALLOWED_CONTENT_TYPES = Objects.isNull(allowedTypes) ? Collections.emptySet()
                : ImmutableSet.copyOf(allowedTypes);
        REJECT_EMPTY_BODY = rejectEmptyBody;
    }

    @Override
    public void handle(RoutingContext rc) {
        String contentLengthValue = rc.request().getHeader(HttpHeaders.CONTENT_LENGTH);
        int contentLength = Objects.isNull(contentLengthValue) ? 0 : Ints.tryParse(contentLengthValue);
        if (REJECT_EMPTY_BODY && contentLength == 0) {
            rc.fail(new EmptyRequestBodyError());
            return;
        }
        if (BODY_SIZE_LIMIT_BYTES > 0 && contentLength > BODY_SIZE_LIMIT_BYTES) {
            rc.fail(new RequestBodyTooLargeError(contentLength, BODY_SIZE_LIMIT_BYTES));
            return;

        }
        rc.request().bodyHandler(buffer -> {
            rc.setBody(buffer);
            if (ALLOWED_CONTENT_TYPES.isEmpty()) {
                rc.next();
                return;
            }
            MediaTypeDetector.getInstance(rc.vertx()).detect(rc.getBody(), type -> {
                if (type.failed()) {
                    rc.fail(type.cause());
                    return;
                }
                if (!ALLOWED_CONTENT_TYPES.contains(type.result())) {
                    rc.fail(new InvalidContentTypeError(type.result(), ALLOWED_CONTENT_TYPES));
                    return;
                }
                rc.next();
            });
        });
    }
}