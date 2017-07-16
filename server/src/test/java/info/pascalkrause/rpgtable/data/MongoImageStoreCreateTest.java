package info.pascalkrause.rpgtable.data;

import static com.google.common.truth.Truth.assertThat;

import info.pascalkrause.rpgtable.TestUtils;
import info.pascalkrause.rpgtable.error.ResourceAlreadyExistError;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestSuite;

public class MongoImageStoreCreateTest extends AbstractMongoImageStoreTest {

    public MongoImageStoreCreateTest(String suitePrefix, MongoImageStore imageStore, MongoClient mc) {
        super(suitePrefix + "test_method_create", imageStore, mc);
    }

    @Override
    public void addTestCases(TestSuite suite, MongoImageStore testClass, MongoClient mc) {
        suite.beforeEach(c -> {
            Async beforeEachComplete = c.async();
            mc.dropCollection(testClass.getCollectionName(), res -> {
                testClass.init(res2 -> {
                    beforeEachComplete.complete();
                });

            });
        });
        suite.test("Create image Test", c -> {
            Async testComplete = c.async();
            Image i1 = getTestImage1();
            testClass.create(i1.name, Buffer.buffer(i1.content), i1.mediaType, createResult -> {
                Image created = createResult.result();
                JsonObject query = new JsonObject();
                query.put("_id", created.id);
                mc.find(testClass.getCollectionName(), query, findResult -> {
                    TestUtils.failTestOnException(c, v -> assertThat(findResult.result().size()).isEqualTo(1));
                    Image found = MongoImageStore.decode.apply(findResult.result().get(0));
                    TestUtils.failTestOnException(c, v -> assertThat(found).isEqualTo(created));
                    testComplete.complete();
                });
            });
        });
        suite.test("Throw Unique Constraint Exception", c -> {
            Async testComplete = c.async();
            Image i1 = getTestImage1();
            testClass.create(i1.name, Buffer.buffer(i1.content), i1.mediaType, createResult -> {
                testClass.create(i1.name, Buffer.buffer(i1.content), i1.mediaType, createResult2 -> {
                    TestUtils.failTestOnException(c,
                            v -> assertThat(createResult2.cause()).isInstanceOf(ResourceAlreadyExistError.class));
                    testComplete.complete();
                });
            });
        });
    }
}
