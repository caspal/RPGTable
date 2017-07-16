package info.pascalkrause.rpgtable.utils;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.regex.Pattern;

public class Utils {

    private static Pattern uuidv4 = Pattern
            .compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    public static String getUTCTimestamp() {
        ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
        return utc.toInstant().toString();
    }

    public static boolean isUUIDv4(String s) {
        if (uuidv4.matcher(s).matches()) {
            return true;
        }
        return false;
    }
}