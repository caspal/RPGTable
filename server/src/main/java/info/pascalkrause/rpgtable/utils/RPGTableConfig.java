package info.pascalkrause.rpgtable.utils;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class RPGTableConfig {

    public static class RPGTableConfigOptions {
        public final static boolean ENV_TEST_DEFAULT = false;
        public final static int BODY_SIZE_LIMIT_DEFAULT = 1024 * 1024 * 5; // 5 mebibyte
        public final static int HTTP_PORT_DEFAULT = 8080;
        public final static int MONGO_PORT_DEFAULT = 27017;
        public final static String MONGO_HOST_DEFAULT = "127.0.0.1";

        private final JsonObject config;

        private RPGTableConfigOptions() {
            config = new JsonObject();
            config.put(ENV_TEST_KEY, ENV_TEST_DEFAULT);
            config.put(BODY_SIZE_LIMIT_BYTES_KEY, BODY_SIZE_LIMIT_DEFAULT);
            config.put(HTTP_PORT_KEY, HTTP_PORT_DEFAULT);
            config.put(MONGO_PORT_KEY, MONGO_PORT_DEFAULT);
            config.put(MONGO_HOST_KEY, MONGO_HOST_DEFAULT);
            config.put(WORKSPACE_DIR_KEY, System.getProperty(WORKSPACE_DIR_KEY));
        }

        private static RPGTableConfigOptions defaults() {
            return new RPGTableConfigOptions();
        }

        private static RPGTableConfigOptions fromVertx(Vertx vertx) {
            RPGTableConfigOptions opts = RPGTableConfigOptions.defaults();
            Map<String, Object> vertxConfMap = vertx.getOrCreateContext().config().getMap();
            Map<String, Object> optsMap = opts.config.getMap();

            KEYS.parallelStream().filter(key -> vertxConfMap.containsKey(key))
                    .forEach(key -> optsMap.put(key, vertxConfMap.get(key)));
            return opts;
        }

        public RPGTableConfigOptions setEnvTest(boolean envTest) {
            config.put(ENV_TEST_KEY, envTest);
            return this;
        }

        public RPGTableConfigOptions setHttpPort(int httpPort) {
            config.put(HTTP_PORT_KEY, httpPort);
            return this;
        }

        public RPGTableConfigOptions setWorkspaceDir(String workspaceDir) {
            config.put(WORKSPACE_DIR_KEY, workspaceDir);
            return this;
        }
    }

    public final static String MONGO_PORT_KEY = "mongo.port";
    public final int MONGO_PORT;
    
    public final static String MONGO_HOST_KEY = "mongo.host";
    public final String MONGO_HOST;
    
    public final static String WORKSPACE_DIR_KEY = "workspace.dir";
    public final String WORKSPACE_DIR;

    public final static String HTTP_PORT_KEY = "http.port";
    public final int HTTP_PORT;

    public final static String BODY_SIZE_LIMIT_BYTES_KEY = "http.request.bodySizeLimitBytes";
    public final int BODY_SIZE_LIMIT_BYTES;

    public final static String ENV_TEST_KEY = "env.test";
    public final boolean ENV_TEST;

    private final static Set<String> KEYS = ImmutableSet.of(WORKSPACE_DIR_KEY, HTTP_PORT_KEY, ENV_TEST_KEY,
            BODY_SIZE_LIMIT_BYTES_KEY, MONGO_HOST_KEY, MONGO_PORT_KEY);
    private final RPGTableConfigOptions options;

    private RPGTableConfig(RPGTableConfigOptions options) {
        this.options = options;
        this.ENV_TEST = options.config.getBoolean(ENV_TEST_KEY);
        this.HTTP_PORT = options.config.getInteger(HTTP_PORT_KEY);
        this.WORKSPACE_DIR = options.config.getString(WORKSPACE_DIR_KEY);
        this.BODY_SIZE_LIMIT_BYTES = options.config.getInteger(BODY_SIZE_LIMIT_BYTES_KEY);
        this.MONGO_HOST = options.config.getString(MONGO_HOST_KEY);
        this.MONGO_PORT = options.config.getInteger(MONGO_PORT_KEY);
    }

    public static RPGTableConfig getOrCreate(Vertx vertx) {
        String key = "RPGTableConfigInstance";
        if (Objects.isNull(vertx.getOrCreateContext().get(key))) {
            vertx.getOrCreateContext().put(key, new RPGTableConfig(RPGTableConfigOptions.fromVertx(vertx)));
        }
        return vertx.getOrCreateContext().get(key);
    }

    public JsonObject toJsonObject() {
        return options.config.copy();
    }

    @Override
    public String toString() {
        return new StringBuilder("RPGTable configuration:\n").append(toJsonObject().encodePrettily()).toString();
    }
}