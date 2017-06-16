package info.pascalkrause.rpgtable.api;

import java.util.Collection;

import info.pascalkrause.rpgtable.data.Image;
import info.pascalkrause.rpgtable.data.ImageStore;
import info.pascalkrause.rpgtable.data.NonPersistentImageStore;
import info.pascalkrause.rpgtable.utils.RPGTableConfig;
import info.pascalkrause.rpgtable.utils.SimpleAsyncResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

public class ImageAPI {

    private final ImageStore store;

    public ImageAPI(Vertx vertx) {
        String storageDir = RPGTableConfig.create(vertx).WORKSPACE_DIR + "/uploads/images/";
        store = new NonPersistentImageStore(vertx.fileSystem(), storageDir);
    }

    public void list(Handler<AsyncResult<Collection<Image>>> handler) {
        handler.handle(new SimpleAsyncResult<Collection<Image>>(store.list()));
    }

    public void create(String name, Buffer buffer, Handler<AsyncResult<Image>> handler) {
        store.create(name, buffer, res -> {handler.handle(res);});
    }

    public void get(String nameOrId, Handler<AsyncResult<Image>> handler) {
        store.get(nameOrId, res -> {handler.handle(res);});
    }
    
    public void delete(String nameOrId, Handler<AsyncResult<Void>> handler) {
        store.delete(nameOrId, res -> {handler.handle(res);});
    }
}