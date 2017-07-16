package info.pascalkrause.rpgtable.utils;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class Utils {

    public static String getUTCTimestamp() {
        ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
        return utc.toInstant().toString();
    }
}