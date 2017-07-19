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

    public static final String PARSED_TYPE_KEY = "parsedType";

    /**
     * Retrieve the {@link com.google.common.net.MediaType MediaType} from the request body, if body was parsed.
     *
     * @param rc The {@link io.vertx.ext.web.RoutingContext RoutingContext} which should contain the parsed body type.
     * @return The {@link com.google.common.net.MediaType MediaType} of the request body, or null if the body wasn't
     * parsed or was empty.
     */
    public static MediaType getParsedBodyType(RoutingContext rc) {
        return rc.get(PARSED_TYPE_KEY);
    }

    private final int BODY_SIZE_LIMIT_BYTES;
    private final Set<MediaType> ALLOWED_CONTENT_TYPES;
    private final boolean REJECT_EMPTY_BODY;
    private final boolean PARSE_TYPE;

    /**
     * This RequestBodyHandler will not parse the {@link com.google.common.net.MediaType MediaType} of the request body,
     * so every {@link com.google.common.net.MediaType MediaType} is allowed.
     *
     * @param rejectEmptyBody If true, the handler will response with a @EmptyRequestBodyError, in case of an empty
     * @param maxRequestBodySizeBytes The maximum bytes for a request body, or 0 to allow any size. body.
     */
    public RequestBodyHandler(boolean rejectEmptyBody, int maxRequestBodySizeBytes) {
        this(maxRequestBodySizeBytes, null, rejectEmptyBody, false);
    }

    /**
     * This RequestBodyHandler will parse the {@link com.google.common.net.MediaType MediaType} of the request body, and
     * allows every {@link com.google.common.net.MediaType MediaType}. Parsing {@link com.google.common.net.MediaType
     * MediaType} requires a non empty body. Due to this fact empty bodies will be rejected.
     *
     * @param maxRequestBodySizeBytes The maximum bytes for a request body, or 0 to allow any size.
     * @param parseType If true, the handler will parse the type of the body and put it to the
     * {@link io.vertx.ext.web.RoutingContext RoutingContext} map.
     */
    public RequestBodyHandler(int maxRequestBodySizeBytes, boolean parseType) {
        this(maxRequestBodySizeBytes, null, true, parseType);
    }

    /**
     * This RequestBodyHandler will parse the {@link com.google.common.net.MediaType MediaType} of the request body, and
     * let pass only requests with a valid {@link com.google.common.net.MediaType MediaType}. Parsing
     * {@link com.google.common.net.MediaType MediaType} requires a non empty body. Due to this fact empty bodies will
     * be rejected.
     *
     * @param maxRequestBodySizeBytes The maximum bytes for a request body, or 0 to allow any size.
     * @param parseType If true, the handler will parse the type of the body and put it to the @RoutingContext map.
     */
    public RequestBodyHandler(int maxRequestBodySizeBytes, Set<MediaType> allowedTypes) {
        this(maxRequestBodySizeBytes, allowedTypes, true, true);
    }

    private RequestBodyHandler(int maxRequestBodySizeBytes, Set<MediaType> allowedTypes, boolean rejectEmptyBody,
            boolean parseType) {
        BODY_SIZE_LIMIT_BYTES = maxRequestBodySizeBytes;
        ALLOWED_CONTENT_TYPES = Objects.isNull(allowedTypes) ? Collections.emptySet()
                : ImmutableSet.copyOf(allowedTypes);
        REJECT_EMPTY_BODY = rejectEmptyBody;
        PARSE_TYPE = parseType;
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
        if (!PARSE_TYPE) {
            rc.next();
            return;
        }
        rc.request().bodyHandler(buffer -> {
            rc.setBody(buffer);
            new MediaTypeDetector(rc.vertx()).detect(rc.getBody(), type -> {
                if (type.failed()) {
                    rc.fail(type.cause());
                    return;
                }
                rc.put(PARSED_TYPE_KEY, type.result());
                if (ALLOWED_CONTENT_TYPES.isEmpty()) {
                    rc.next();
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