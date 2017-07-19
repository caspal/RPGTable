package info.pascalkrause.rpgtable.error;

import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.net.MediaType;

@SuppressWarnings("serial")
public class InvalidContentTypeError extends BasicError {

    public InvalidContentTypeError(MediaType detected, Set<MediaType> allowedContentTypes) {
        super(ErrorType.INVALID_CONTENT_TYPE, "The detected content type for this request is " + detected
                + " (Allowed: " + Joiner.on(", ").join(allowedContentTypes) + ")");
    }
}