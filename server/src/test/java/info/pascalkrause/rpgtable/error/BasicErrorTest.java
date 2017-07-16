package info.pascalkrause.rpgtable.error;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

import info.pascalkrause.rpgtable.error.BasicError.ErrorType;
import info.pascalkrause.rpgtable.utils.Utils;
import io.vertx.core.json.JsonObject;

public class BasicErrorTest {

    @Test
    public void testGetResponse() {
        String timestamp = Utils.getUTCTimestamp();
        String message = "This is a test";
        BasicError err = new BasicError(ErrorType.UNEXPECTED_ERROR, message, null, timestamp);

        JsonObject expected = new JsonObject();
        expected.put("id", "[500-UNER]");
        expected.put("description", "Unexpected Error");
        expected.put("message", message);
        expected.put("timestamp", timestamp);

        assertThat(err.getResponse()).isEqualTo(expected);
    }
}
