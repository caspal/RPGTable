package info.pascalkrause.rpgtable.error;

@SuppressWarnings("serial")
public class EmptyRequestBodyError extends BasicError {

    public EmptyRequestBodyError() {
        super(ErrorType.EMPTY_REQUEST_BODY, "This request requires a non empty request body");
    }
}