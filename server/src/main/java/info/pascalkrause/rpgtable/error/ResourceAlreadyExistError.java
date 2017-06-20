package info.pascalkrause.rpgtable.error;

@SuppressWarnings("serial")
public class ResourceAlreadyExistError extends BasicError{

    public ResourceAlreadyExistError(String type, String name) {
        super(ErrorType.RESOURCE_ALREADY_EXIST, "A resource of type " + type + " with name " + name + " already exist");
    }
}