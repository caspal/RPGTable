package info.pascalkrause.rpgtable.api;

import java.util.List;

import com.google.common.hash.Hashing;
import com.google.common.net.MediaType;

import info.pascalkrause.rpgtable.data.Image;
import info.pascalkrause.rpgtable.data.MongoImageStore;
import info.pascalkrause.vertx.mongodata.SimpleAsyncResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;

public class ImageApiImpl implements ImageAPI {

    private final MongoImageStore mis;

    public ImageApiImpl(MongoImageStore mis) {
        this.mis = mis;
    }

    @Override
    public void list(Handler<AsyncResult<List<Image>>> handler) {
        mis.list(handler);
    }

    @Override
    public void create(String name, Buffer buffer, MediaType type, Handler<AsyncResult<Image>> handler) {
        byte[] content = buffer.getBytes();
        String sha256 = Hashing.sha256().hashBytes(content).toString();
        Image newImage = new Image(name, sha256, content.length, content, type.toString());
        mis.save(newImage, res -> {
            if (res.failed()) {
                handler.handle(new SimpleAsyncResult<Image>(res.cause()));
                return;
            }
            handler.handle(new SimpleAsyncResult<Image>(newImage));
        });
    }

    @Override
    public void get(String nameOrId, Handler<AsyncResult<Image>> handler) {
        mis.get(nameOrId, handler);
    }

    @Override
    public void delete(String nameOrId, Handler<AsyncResult<Void>> handler) {
        mis.delete(nameOrId, handler);
    }
}
