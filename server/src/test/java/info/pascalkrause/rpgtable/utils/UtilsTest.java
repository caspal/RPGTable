package info.pascalkrause.rpgtable.utils;

import static com.google.common.truth.Truth.assertThat;

import java.util.UUID;

import org.junit.Test;

public class UtilsTest {

    @Test
    public void isUUIDv4Test() {
        String validUUIDv4 = UUID.randomUUID().toString();
        String invalidUUIDv4 = "invalidUUIDv4";
        assertThat(Utils.isUUIDv4(validUUIDv4)).isTrue();
        assertThat(Utils.isUUIDv4(invalidUUIDv4)).isFalse();
    }
}