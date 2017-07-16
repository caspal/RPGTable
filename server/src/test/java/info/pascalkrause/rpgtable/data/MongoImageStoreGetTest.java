package info.pascalkrause.rpgtable.data;

import static com.google.common.truth.Truth.assertThat;

import info.pascalkrause.rpgtable.TestUtils;
import info.pascalkrause.rpgtable.error.ResourceNotFoundError;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestSuite;

public class MongoImageStoreGetTest extends AbstractMongoImageStoreTest {

    public MongoImageStoreGetTest(String suitePrefix, MongoImageStore imageStore, MongoClient mc) {
        super(suitePrefix + "test_method_get", imageStore, mc);
    }

    @Override
    public void addTestCases(TestSuite suite, MongoImageStore testClass, MongoClient mc) {
        suite.beforeEach(c -> {
            Async beforeEachComplete = c.async();
            mc.dropCollection(testClass.getCollectionName(), res -> {
                beforeEachComplete.complete();
            });
        });
        suite.test("Get with id Test", c -> {
            Async testComplete = c.async();
            Image i1 = getTestImage1();
            mc.save(testClass.getCollectionName(), MongoImageStore.encode.apply(i1), resInsert1 -> {
                testClass.get(i1.id, getResult -> {
                    TestUtils.failTestOnException(c, v -> assertThat(getResult.result()).isEqualTo(i1));
                    testComplete.complete();
                });
            });
        });
        suite.test("Get with name Test", c -> {
            Async testComplete = c.async();
            Image i2 = getTestImage2();
            mc.save(testClass.getCollectionName(), MongoImageStore.encode.apply(i2), resInsert1 -> {
                testClass.get(i2.name, getResult -> {
                    TestUtils.failTestOnException(c, v -> assertThat(getResult.result()).isEqualTo(i2));
                    testComplete.complete();
                });
            });
        });
        suite.test("Throw Resource Not Found Test", c -> {
            Async testComplete = c.async();
            testClass.get("nonPresentItemName", res -> {
                TestUtils.failTestOnException(c, v -> assertThat(res.cause()).isInstanceOf(ResourceNotFoundError.class));
                testComplete.complete();
            });
        });
    }
}
