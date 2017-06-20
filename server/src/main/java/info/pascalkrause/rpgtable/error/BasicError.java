package info.pascalkrause.rpgtable.error;

@SuppressWarnings("serial")
public class BasicError extends Error {

    private final ErrorType type;

    public BasicError(ErrorType type) {
        this(type, null);
    }

    public BasicError(ErrorType type, Throwable cause) {
        super(type.toString(), cause);
        this.type = type;
    }

    public ErrorType getType() {
        return type;
    }
}