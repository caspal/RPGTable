package info.pascalkrause.rpgtable;

import java.io.IOException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.runtime.Network;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.TestOptions;
import io.vertx.ext.unit.report.ReportOptions;

public class TestUtils {

    public static void failTestOnException(TestContext context, Handler<Void> testCode) {
        try {
            testCode.handle(null);
        } catch (Throwable t) {
            context.fail(t);
        }
    }

    public static Buffer readFile(String ressourcePath) throws IOException {
        return Buffer.buffer(Resources.asByteSource(Resources.getResource(ressourcePath)).read());
    }

    public static MongodExecutable prepareMongo(JsonObject mongoConfig) throws UnknownHostException, IOException {
        Logger logger = LoggerFactory.getLogger(TestUtils.class);
        String bindIp = mongoConfig.getString("host");
        int port = mongoConfig.getInteger("port");
        IMongodConfig mongodConfig = new MongodConfigBuilder().version(Version.Main.PRODUCTION)
                .net(new Net(bindIp, port, false)).build();

        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder().defaultsWithLogger(Command.MongoD, logger)
                .processOutput(ProcessOutput.getDefaultInstanceSilent()).build();

        MongodStarter starter = MongodStarter.getInstance(runtimeConfig);
        return starter.prepare(mongodConfig);
    }

    public static JsonObject getDefaultMongoConfig() throws IOException {
        JsonObject mongoConfig = new JsonObject();
        mongoConfig.put("host", "127.0.0.1");
        mongoConfig.put("port", Network.getFreeServerPort());
        mongoConfig.put("db_name", "test");
        return mongoConfig;
    }

    public static TestOptions getTestOptions(Vertx vertx) {
        String reportsPath = "./testreports";
        if (!vertx.fileSystem().existsBlocking(reportsPath)) {
            vertx.fileSystem().mkdirBlocking(reportsPath);
        }
        TestOptions opts = new TestOptions().addReporter(new ReportOptions().setTo("console"));
        opts.addReporter(new ReportOptions().setTo("file:./testreports/").setFormat("junit"));
        opts.addReporter(new ReportOptions().setTo("file:./testreports/").setFormat("simple"));
        return opts;
    }
}
