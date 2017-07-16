package info.pascalkrause.rpgtable.utils;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class Utils {

    public static String getUTCTimestamp() {
        ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
        return utc.toInstant().toString();
    }
    
    public static boolean isUUIDv4(String s) {
        if (s.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
            return true;
        }
        return false;
    }
}