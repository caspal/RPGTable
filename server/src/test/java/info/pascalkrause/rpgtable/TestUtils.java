package info.pascalkrause.rpgtable;

import java.io.IOException;

import com.google.common.io.Resources;

import io.vertx.core.buffer.Buffer;

public class TestUtils {

    public static Buffer readFile(String ressourcePath) throws IOException {
        return Buffer.buffer(Resources.asByteSource(Resources.getResource(ressourcePath)).read());
    }
}