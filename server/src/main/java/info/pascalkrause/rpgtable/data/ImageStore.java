package info.pascalkrause.rpgtable.data;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;

public interface ImageStore {

    public void list(Handler<AsyncResult<List<Image>>> handler);

    public void create(String name, Buffer buffer, String mediaType, Handler<AsyncResult<Image>> handler);

    public void get(String nameOrId, Handler<AsyncResult<Image>> handler);

    public void delete(String nameOrId, Handler<AsyncResult<Void>> handler);
}