package info.pascalkrause.rpgtable.data;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;

import com.google.common.collect.ImmutableList;

import info.pascalkrause.rpgtable.TestUtils;
import info.pascalkrause.rpgtable.data.Image;
import info.pascalkrause.rpgtable.data.MongoImageStore;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestSuite;

public class MongoImageStoreListTest extends AbstractMongoImageStoreTest {

    public MongoImageStoreListTest(String suitePrefix, MongoImageStore imageStore, MongoClient mc) {
        super(suitePrefix + "test_method_list", imageStore, mc);
    }

    @Override
    public void addTestCases(TestSuite suite, MongoImageStore testClass, MongoClient mc) {
        suite.beforeEach(c -> {
            Async beforeEachComplete = c.async();
            mc.dropCollection(testClass.getCollectionName(), res -> {
                beforeEachComplete.complete();
            });
        });
        suite.test("Empty list Test", c -> {
            Async testComplete = c.async();
            testClass.list(res -> {
                TestUtils.runTruthTests(c, v -> assertThat(res.result()).isEmpty());
                testComplete.complete();
            });
        });
        suite.test("Non-empty list Test", c -> {
            Async testComplete = c.async();
            Image i1 = getTestImage1();
            Image i2 = getTestImage2();
            List<Image> expected = ImmutableList.of(i1, i2);
            mc.save(testClass.getCollectionName(), MongoImageStore.encode.apply(i1), resInsert1 -> {
                mc.save(testClass.getCollectionName(), MongoImageStore.encode.apply(i2), resInsert2 -> {
                    testClass.list(listResult -> {
                        TestUtils.runTruthTests(c,
                                v -> assertThat(listResult.result()).containsExactlyElementsIn(expected));
                        testComplete.complete();
                    });
                });
            });
        });
    }
}
