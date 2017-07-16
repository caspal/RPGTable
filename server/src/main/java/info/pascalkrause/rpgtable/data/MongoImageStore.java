package info.pascalkrause.rpgtable.data;

import java.util.List;
import java.util.function.Function;

import com.google.common.annotations.VisibleForTesting;
import com.mongodb.MongoWriteException;

import info.pascalkrause.rpgtable.error.ResourceAlreadyExistError;
import info.pascalkrause.rpgtable.error.ResourceNotFoundError;
import info.pascalkrause.rpgtable.error.UnexpectedError;
import info.pascalkrause.rpgtable.utils.Utils;
import info.pascalkrause.vertx.mongodata.MongoCollectionFactory;
import info.pascalkrause.vertx.mongodata.SimpleAsyncResult;
import info.pascalkrause.vertx.mongodata.collection.Index;
import info.pascalkrause.vertx.mongodata.collection.MongoCollection;
import info.pascalkrause.vertx.mongodata.datasource.MongoDataSource;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public class MongoImageStore implements ImageStore {

    private final String collectionName;
    private final MongoCollection<Image> imageCollection;

    @VisibleForTesting
    static Function<Image, JsonObject> encode = image -> {
        JsonObject imageAsJson = new JsonObject();
        imageAsJson.put("_id", image.getId());
        imageAsJson.put("name", image.getName());
        imageAsJson.put("hash", image.getHash());
        imageAsJson.put("contentLength", image.getContentLength());
        imageAsJson.put("content", new JsonObject().put("$binary", image.getContent()));
        imageAsJson.put("mediaType", image.getMediaType());
        return imageAsJson;
    };

    @VisibleForTesting
    static Function<JsonObject, Image> decode = imageAsJson -> {
        Image image = new Image();
        image.id = imageAsJson.getString("_id");
        image.name = imageAsJson.getString("name");
        image.hash = imageAsJson.getString("hash");
        image.contentLength = imageAsJson.getInteger("contentLength");
        image.content = imageAsJson.getJsonObject("content").getBinary("$binary");
        image.mediaType = imageAsJson.getString("mediaType");
        return image;
    };

    public MongoImageStore init(Handler<AsyncResult<MongoImageStore>> completed) {
        imageCollection.createIndex(new Index("idxNameUnique", "name", true), v -> {
            if (v.succeeded()) {
                completed.handle(new SimpleAsyncResult<MongoImageStore>(this));
            } else {
                completed.handle(new SimpleAsyncResult<MongoImageStore>(v.cause()));
            }
        });
        return this;
    }

    public static void createInstance(String collectionName, MongoDataSource mds,
            Handler<AsyncResult<MongoImageStore>> completed) {
        new MongoImageStore(collectionName, mds, completed);
    }

    private MongoImageStore(String collectionName, MongoDataSource mds,
            Handler<AsyncResult<MongoImageStore>> completed) {
        this.collectionName = collectionName;
        this.imageCollection = MongoCollectionFactory.using(mds).build(collectionName, encode, decode);
        init(completed);

    }

    public String getCollectionName() {
        return collectionName;
    }

    @Override
    public void list(Handler<AsyncResult<List<Image>>> handler) {
        imageCollection.findAll(res -> {
            if (res.failed()) {
                handler.handle(new SimpleAsyncResult<List<Image>>(new UnexpectedError(res.cause())));
                return;
            }
            handler.handle(new SimpleAsyncResult<List<Image>>(res.result()));
        });
    }

    @Override
    public void save(Image image, Handler<AsyncResult<Void>> handler) {
        imageCollection.upsert(image, res -> {
            if (res.failed()) {
                if (res.cause() instanceof MongoWriteException) {
                    handler.handle(new SimpleAsyncResult<Void>(
                            new ResourceAlreadyExistError("Image", image.getName(), res.cause())));
                } else {
                    handler.handle(new SimpleAsyncResult<Void>(new UnexpectedError(res.cause())));
                }
                return;
            }
            handler.handle(new SimpleAsyncResult<Void>((Void) null));
        });
    }

    private static JsonObject buildQuery(String nameOrId) {
        JsonObject query = new JsonObject();
        if (Utils.isUUIDv4(nameOrId)) {
            query.put("_id", nameOrId);
        } else {
            query.put("name", nameOrId);
        }
        return query;
    }

    @Override
    public void get(String nameOrId, Handler<AsyncResult<Image>> handler) {
        imageCollection.find(buildQuery(nameOrId), res -> {
            if (res.failed()) {
                handler.handle(new SimpleAsyncResult<Image>(new UnexpectedError(res.cause())));
                return;
            }
            if (res.result().isEmpty()) {
                handler.handle(new SimpleAsyncResult<Image>(new ResourceNotFoundError("Image", nameOrId)));
            } else {
                handler.handle(new SimpleAsyncResult<Image>(res.result().get(0)));
            }
        });
    }

    @Override
    public void delete(String nameOrId, Handler<AsyncResult<Void>> handler) {
        imageCollection.remove(buildQuery(nameOrId), res -> {
            if (res.failed()) {
                handler.handle(new SimpleAsyncResult<Void>(new UnexpectedError(res.cause())));
                return;
            }
            if (res.result() == 0) {
                handler.handle(new SimpleAsyncResult<Void>(new ResourceNotFoundError("Image", nameOrId)));
            } else {
                handler.handle(new SimpleAsyncResult<Void>((Void) null));
            }
        });
    }
}
