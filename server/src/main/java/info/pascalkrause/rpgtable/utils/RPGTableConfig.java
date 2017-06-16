package info.pascalkrause.rpgtable.utils;

import java.io.File;
import java.io.StringWriter;
import java.util.Objects;

import com.google.common.io.Files;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class RPGTableConfig {

    public final static String WORKSPACE_DIR_KEY = "workspace.dir";
    public final String WORKSPACE_DIR;
    public final static String HTTP_PORT_KEY = "http.port";
    public final static int HTTP_PORT_DEFAULT = 8080;
    public final int HTTP_PORT;
    public final static String ENV_TEST_KEY = "env.test";
    public final static boolean ENV_TEST_DEFAULT = false;;
    public final boolean ENV_TEST;

    private RPGTableConfig(Boolean ENV_TEST, Integer HTTP_PORT, String WORKSPACE_DIR) {
        this.ENV_TEST = Objects.nonNull(ENV_TEST) ? ENV_TEST : ENV_TEST_DEFAULT;
        this.HTTP_PORT = Objects.nonNull(HTTP_PORT) ? HTTP_PORT : HTTP_PORT_DEFAULT;
        this.WORKSPACE_DIR = Objects.nonNull(WORKSPACE_DIR) ? WORKSPACE_DIR
                : System.getProperty(WORKSPACE_DIR_KEY, createTempWorkspaceDir());
    }

    public static RPGTableConfig create(Vertx vertx) {
        JsonObject config = vertx.getOrCreateContext().config();
        return new RPGTableConfig(config.getBoolean(ENV_TEST_KEY), config.getInteger(HTTP_PORT_KEY),
                config.getString(WORKSPACE_DIR_KEY));
    }

    public static RPGTableConfig create(Boolean ENV_TEST, Integer HTTP_PORT, String WORKSPACE_DIR) {
        return new RPGTableConfig(ENV_TEST, HTTP_PORT, WORKSPACE_DIR);
    }

    public JsonObject toJsonObject() {
        JsonObject c = new JsonObject();
        c.put(ENV_TEST_KEY, ENV_TEST);
        c.put(HTTP_PORT_KEY, HTTP_PORT);
        c.put(WORKSPACE_DIR_KEY, WORKSPACE_DIR);
        return c;
    }

    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        sw.append("RPGTable configuration:");
        sw.append("\n").append(ENV_TEST_KEY + ": " + ENV_TEST);
        sw.append("\n").append(HTTP_PORT_KEY + ": " + HTTP_PORT);
        sw.append("\n").append(WORKSPACE_DIR_KEY + ": " + WORKSPACE_DIR);
        return sw.toString();
    }

    private static String createTempWorkspaceDir() {
        File dir = Files.createTempDir();
        return dir.getAbsolutePath();
    }
}