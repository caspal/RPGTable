package info.pascalkrause.rpgtable.data;

import java.util.List;
import java.util.function.Function;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.hash.Hashing;

import info.pascalkrause.rpgtable.error.ResourceNotFoundError;
import info.pascalkrause.rpgtable.error.UnexpectedError;
import info.pascalkrause.rpgtable.utils.Utils;
import info.pascalkrause.vertx.mongodata.MongoCollectionFactory;
import info.pascalkrause.vertx.mongodata.SimpleAsyncResult;
import info.pascalkrause.vertx.mongodata.collection.MongoCollection;
import info.pascalkrause.vertx.mongodata.datasource.MongoClientDataSource;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
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
        return image;
    };
    
    public MongoImageStore(String collectionName, MongoClientDataSource mds) {
        this.collectionName = collectionName;
        this.imageCollection = MongoCollectionFactory.using(mds).build(collectionName, encode, decode);
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
    public void create(String name, Buffer buffer, Handler<AsyncResult<Image>> handler) {
        byte[] content = buffer.getBytes();
        String sha256 = Hashing.sha256().hashBytes(content).toString();
        Image newImage = new Image(name, sha256, content.length, content);
        imageCollection.upsert(newImage, res -> {
            if (res.failed()) {
                handler.handle(new SimpleAsyncResult<Image>(new UnexpectedError(res.cause())));
                return;
            }
            handler.handle(new SimpleAsyncResult<Image>(newImage));
        });
    }

    private JsonObject buildQuery(String nameOrId) {
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
        get(nameOrId, getResult -> {
            if (getResult.failed()) {
                handler.handle(new SimpleAsyncResult<Void>(new UnexpectedError(getResult.cause())));
                return;
            }
            imageCollection.remove(getResult.result().getId(), deleteResult -> {
                if (deleteResult.failed()) {
                    handler.handle(new SimpleAsyncResult<Void>(new UnexpectedError(deleteResult.cause())));
                    return;
                }
                handler.handle(new SimpleAsyncResult<Void>((Void) null));
            });
        });
    }

    @Override
    public void getResource(String nameOrId, Handler<AsyncResult<Buffer>> handler) {
        get(nameOrId, res -> {
            if (res.failed()) {
                handler.handle(new SimpleAsyncResult<Buffer>(new UnexpectedError(res.cause())));
                return;
            }
            handler.handle(new SimpleAsyncResult<Buffer>(Buffer.buffer(res.result().getContent())));
        });
    }
}
