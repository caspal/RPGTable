package info.pascalkrause.rpgtable.utils;

import static com.google.common.truth.Truth.assertThat;

import java.util.UUID;

import org.junit.Test;

public class UtilsTest {

    @Test
    public void testIsUUIDv4() {
        String valid = UUID.randomUUID().toString();
        String invalid = "No-UUID-String";
        assertThat(Utils.isUUIDv4(valid)).isTrue();
        assertThat(Utils.isUUIDv4(invalid)).isFalse();
    }
}
