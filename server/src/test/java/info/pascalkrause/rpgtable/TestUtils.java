package info.pascalkrause.rpgtable;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import com.google.common.io.Files;

public class TestUtils {

    public static int getFreePort() throws IOException {
        ServerSocket socket = new ServerSocket(0);
        int port = socket.getLocalPort();
        socket.close();
        return port;
    }

    public static String createTempWorkspaceDir() {
        File dir = Files.createTempDir();
        return dir.getAbsolutePath();
    }
}