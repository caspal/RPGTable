package info.pascalkrause.rpgtable.data;

import static com.google.common.truth.Truth.assertThat;

import info.pascalkrause.rpgtable.TestUtils;
import info.pascalkrause.rpgtable.error.ResourceNotFoundError;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestSuite;

public class MongoImageStoreDeleteTest extends AbstractMongoImageStoreTest {

    public MongoImageStoreDeleteTest(String suitePrefix, MongoImageStore imageStore, MongoClient mc) {
        super(suitePrefix + "test_method_delete", imageStore, mc);
    }

    @Override
    public void addTestCases(TestSuite suite, MongoImageStore testClass, MongoClient mc) {
        suite.beforeEach(c -> {
            Async beforeEachComplete = c.async();
            mc.dropCollection(testClass.getCollectionName(), res -> {
                beforeEachComplete.complete();
            });
        });
        suite.test("Delete with id Test", c -> {
            Async testComplete = c.async();
            Image i1 = getTestImage1();
            mc.save(testClass.getCollectionName(), MongoImageStore.encode.apply(i1), resInsert1 -> {
                testClass.delete(i1.id, deleteResult -> {
                    TestUtils.runTruthTests(c, v -> assertThat(deleteResult.succeeded()).isTrue());
                    mc.find(testClass.getCollectionName(), new JsonObject(), findRes -> {
                        TestUtils.runTruthTests(c, v -> assertThat(findRes.result()).isEmpty());
                        testComplete.complete();
                    });
                });
            });
        });
        suite.test("Delete with name Test", c -> {
            Async testComplete = c.async();
            Image i2 = getTestImage1();
            mc.save(testClass.getCollectionName(), MongoImageStore.encode.apply(i2), resInsert1 -> {
                testClass.delete(i2.name, deleteResult -> {
                    TestUtils.runTruthTests(c, v -> assertThat(deleteResult.succeeded()).isTrue());
                    mc.find(testClass.getCollectionName(), new JsonObject(), findRes -> {
                        TestUtils.runTruthTests(c, v -> assertThat(findRes.result()).isEmpty());
                        testComplete.complete();
                    });
                });
            });
        });
        suite.test("Throw Resource Not Found Test", c -> {
            Async testComplete = c.async();
            testClass.delete("nonPresentItemName", res -> {
                TestUtils.runTruthTests(c, v -> assertThat(res.cause()).isInstanceOf(ResourceNotFoundError.class));
                testComplete.complete();
            });
        });
    }
}
