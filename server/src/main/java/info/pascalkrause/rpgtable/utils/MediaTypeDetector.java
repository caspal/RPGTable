package info.pascalkrause.rpgtable.utils;

import org.apache.tika.Tika;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.net.MediaType;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

public class MediaTypeDetector {
    private final Tika tika;
    private final Vertx vertx;

    public MediaTypeDetector(Vertx vertx) {
        tika = new Tika();
        this.vertx = vertx;
    }

    public void detect(Buffer buffer, Handler<AsyncResult<MediaType>> handler) {
        vertx.executeBlocking(future -> {
            future.complete(detectBlocking(buffer));
        }, handler);
    }

    @VisibleForTesting
    MediaType detectBlocking(Buffer buffer) {
        // According to the documentation [1] only a few bytes are necessary.
        // [1] https://tika.apache.org/1.15/api/org/apache/tika/Tika.html#detect-byte:A-
        int prefixBytes = 1024 * 3; // 2 kibibyte
        if (buffer.length() < prefixBytes) {
            return MediaType.parse(tika.detect(buffer.getBytes()));
        }
        return MediaType.parse(tika.detect(buffer.getBytes(0, prefixBytes)));
    }
}