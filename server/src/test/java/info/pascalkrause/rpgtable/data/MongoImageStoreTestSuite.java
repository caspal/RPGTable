package info.pascalkrause.rpgtable.data;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableList;

import de.flapdoodle.embed.mongo.MongodExecutable;
import info.pascalkrause.rpgtable.TestUtils;
import info.pascalkrause.vertx.mongodata.datasource.MongoClientDataSource;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class MongoImageStoreTestSuite {
    private static Vertx vertx;

    private static JsonObject mongoConfig;
    private static MongodExecutable mongodExecutable;
    private static MongoImageStore testClass;
    private static MongoClient mc;

    private void startMongoDB() throws IOException {
        mongodExecutable = TestUtils.prepareMongo(mongoConfig);
        mongodExecutable.start();
        System.out.println(new StringBuilder("Embedded MongoDB Started with:\n")
                .append(Json.encodePrettily(mongoConfig)).toString());
    }

    @Before
    public void tearUp(TestContext testContext) throws IOException {
        Async tearUpCompleted = testContext.async();
        mongoConfig = TestUtils.getDefaultMongoConfig();
        startMongoDB();
        vertx = Vertx.vertx();
        mc = MongoClient.createShared(vertx, mongoConfig);
        MongoImageStore.createInstance("test_images", new MongoClientDataSource(mc), mis -> {
            testClass = mis.result();
            tearUpCompleted.complete();
        });
    }

    @After
    public void tearDown(TestContext testContext) {
        mongodExecutable.stop();
        vertx.close(testContext.asyncAssertSuccess());
    }

    private List<AbstractMongoImageStoreTest> initializeSuites(String suiteName) {
        List<AbstractMongoImageStoreTest> suites = ImmutableList.<AbstractMongoImageStoreTest>builder()
                .add(new MongoImageStoreListTest(suiteName, testClass, mc))
                .add(new MongoImageStoreGetTest(suiteName, testClass, mc))
                .add(new MongoImageStoreDeleteTest(suiteName, testClass, mc))
                .add(new MongoImageStoreCreateTest(suiteName, testClass, mc)).build();
        return suites;
    }

    @Test
    public void test(TestContext testContext) {
        Async testComplete = testContext.async();
        initializeSuites("TestSuite MongoImageStore").forEach(suite -> {
            suite.getSuite().run(vertx, TestUtils.getTestOptions(vertx)).awaitSuccess(TimeUnit.SECONDS.toMillis(2));
        });
        testComplete.complete();
    }
}
