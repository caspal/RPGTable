package info.pascalkrause.rpgtable.error;

@SuppressWarnings("serial")
public class RequestBodyTooLargeError extends BasicError {

    public RequestBodyTooLargeError(int actualSize, int maxSize) {
        super(ErrorType.REQUEST_BODY_TOO_LARGE, "The request body is larger than " + maxSize + " bytes (Limit: " + maxSize + " bytes)");
    }
}