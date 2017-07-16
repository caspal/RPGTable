package info.pascalkrause.rpgtable.data;

import java.io.IOException;

import com.google.common.hash.Hashing;

import info.pascalkrause.rpgtable.TestUtils;
import info.pascalkrause.rpgtable.utils.MediaTypeDetector;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.TestSuite;

public abstract class AbstractMongoImageStoreTest {

    private final TestSuite suite;

    public AbstractMongoImageStoreTest(String suiteName, MongoImageStore imageStore, MongoClient mc) {
        suite = TestSuite.create(suiteName);
        addTestCases(suite, imageStore, mc);
    }

    public abstract void addTestCases(TestSuite suite, MongoImageStore testClass, MongoClient mc);

    public TestSuite getSuite() {
        return suite;
    }

    protected Image getTestImage1() {
        return loadImage("Image1", "images/TestImage.gif");
    }

    protected Image getTestImage2() {
        return loadImage("Image2", "images/TestImage.png");
    }

    private Image loadImage(String name, String path) {
        try {
            byte[] imageContent = TestUtils.readFile(path).getBytes();
            String sha256 = Hashing.sha256().hashBytes(imageContent).toString();
            String type = MediaTypeDetector.detectBlocking(Buffer.buffer(imageContent)).toString();
            return new Image(name, sha256, imageContent.length, imageContent, type);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
