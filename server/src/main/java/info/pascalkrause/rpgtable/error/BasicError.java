package info.pascalkrause.rpgtable.error;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Splitter;

import info.pascalkrause.rpgtable.utils.Utils;
import io.vertx.core.json.JsonObject;

@SuppressWarnings("serial")
public class BasicError extends Error {

    public static enum ErrorType {
        EMPTY_REQUEST_BODY(400, "Empty Request Body"),
        RESOURCE_ALREADY_EXIST(409, "Resource Already Exist"),
        RESOURCE_NOT_FOUND(404, "Resource Not Found"),
        REQUEST_BODY_TOO_LARGE(413, "Request Body Too Large"),
        UNEXPECTED_ERROR(500, "Unexpected Error");

        private final int statuscode;
        private final String id;
        private final String description;

        private ErrorType(int statuscode, String description) {
            this.statuscode = statuscode;
            this.description = description;

            Stream<String> descParts = Splitter.on(" ").splitToList(description).stream();
            // Transform "Resource Already Exist" to "REALEX"
            String desc = descParts.map(s -> s.substring(0, 2).toUpperCase()).collect(Collectors.joining(""));
            id = new StringBuilder("[").append(statuscode).append("-").append(desc).append("]").toString();
        }

        public int getStatusCode() {
            return statuscode;
        }

        public String getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    private final ErrorType type;
    private final String message;
    private final String timestamp;

    public BasicError(ErrorType type, String message) {
        this(type, message, null);
    }

    public BasicError(ErrorType type, String message, Throwable cause) {
        super(type.toString(), cause);
        this.type = type;
        this.message = message;
        this.timestamp = Utils.getUTCTimestamp();
    }

    public ErrorType getType() {
        return type;
    }

    public JsonObject getResponse() {
        JsonObject resp = new JsonObject();
        resp.put("id", type.getId());
        resp.put("description", type.getDescription());
        resp.put("message", message);
        resp.put("timestamp", timestamp);
        return resp.copy();
    }
}