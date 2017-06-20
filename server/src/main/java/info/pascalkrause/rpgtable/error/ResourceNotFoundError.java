package info.pascalkrause.rpgtable.error;

@SuppressWarnings("serial")
public class ResourceNotFoundError extends BasicError{

    public ResourceNotFoundError(String type, String nameOrId) {
        super(ErrorType.RESOURCE_NOT_FOUND, "A resource of type " + type + " with name or id " + nameOrId + " does not exist");
    }
}