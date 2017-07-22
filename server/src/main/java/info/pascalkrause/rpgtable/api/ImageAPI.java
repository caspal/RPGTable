package info.pascalkrause.rpgtable.api;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.net.MediaType;

import info.pascalkrause.rpgtable.data.Image;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;

public interface ImageAPI {

    public static Set<MediaType> getSupportedImageTypes() {
        return ImmutableSet.of(MediaType.GIF, MediaType.JPEG, MediaType.PNG);
    }

    public void list(Handler<AsyncResult<List<Image>>> handler);

    public void create(String name, Buffer buffer, Handler<AsyncResult<Image>> handler);

    public void get(String nameOrId, Handler<AsyncResult<Image>> handler);

    public void delete(String nameOrId, Handler<AsyncResult<Void>> handler);
}