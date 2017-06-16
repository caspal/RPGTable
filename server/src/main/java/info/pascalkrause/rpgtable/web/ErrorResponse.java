package info.pascalkrause.rpgtable.web;

import info.pascalkrause.rpgtable.utils.Utils;

public class ErrorResponse {

    public static ErrorResponse newUnexpectedError() {
        return new ErrorResponse(500, "[500-UNER]", "Unexpected Error", "An unexpected error occured");
    }

    public static ErrorResponse newImageBodyIsEmptyError() {
        return new ErrorResponse(400, "[400-IMAGE-BOISEM]", "Body Is Empty", "There is no image in the body");
    }

    public static ErrorResponse newImageResourceAlreadyExistError(String name) {
        return new ErrorResponse(409, "[409-IMAGE-IMALEX]", "Image Already Exists",
                "An image with name " + name + " already exist");
    }

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