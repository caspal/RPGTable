package info.pascalkrause.rpgtable.error;

@SuppressWarnings("serial")
public class UnexpectedError extends BasicError {

    public UnexpectedError(Throwable cause) {
        super(ErrorType.UNEXPECTED_ERROR, "An unexpected error occured", cause);
    }
}