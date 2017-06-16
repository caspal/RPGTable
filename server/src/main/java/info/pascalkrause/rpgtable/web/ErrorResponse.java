package info.pascalkrause.rpgtable.web;

import com.fasterxml.jackson.annotation.JsonIgnore;

import info.pascalkrause.rpgtable.utils.Utils;

public class ErrorResponse {

    public final static ErrorResponse UNEXPECTED_ERROR = new ErrorResponse(500, "[500-UNER]", "Unexpected Error",
            "An unexpected error occured");

    @JsonIgnore
    private int statusCode;
    private String id;
    private String description;
    private String message;
    private String timestamp;

    public ErrorResponse(int statusCode, String id, String description, String message) {
        this(statusCode, id, description, message, Utils.getUTCTimestamp());
    }

    public ErrorResponse(int statusCode, String id, String description, String message, String timestamp) {
        super();
        this.statusCode = statusCode;
        this.id = id;
        this.description = description;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }
}