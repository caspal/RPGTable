package info.pascalkrause.rpgtable.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.common.hash.Hashing;

import info.pascalkrause.rpgtable.api.ImageAPI;
import info.pascalkrause.rpgtable.error.BasicError;
import info.pascalkrause.rpgtable.error.ErrorType;
import info.pascalkrause.rpgtable.utils.SimpleAsyncResult;
import info.pascalkrause.rpgtable.utils.Utils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;

public class NonPersistentImageStore implements ImageStore, ImageAPI {

    private final FileSystem fs;
    private final String storageDir;

    private static Map<String, String> files = new HashMap<>();
    private static Map<String, Image> images = new HashMap<>();

    public NonPersistentImageStore(FileSystem fs, String WORKSPACE_DIR) {
        storageDir = WORKSPACE_DIR + "/uploads/images/";
        this.fs = fs;
        fs.mkdirsBlocking(storageDir);
    }

    @Override
    public void list(Handler<AsyncResult<Collection<Image>>> handler) {
        handler.handle(new SimpleAsyncResult<Collection<Image>>(images.values()));
    }

    @Override
    public void create(String name, Buffer buffer, Handler<AsyncResult<Image>> handler) {
        if (images.containsKey(name)) {
            handler.handle(new SimpleAsyncResult<Image>(new BasicError(ErrorType.RESOURCE_ALREADY_EXIST)));
            return;
        }

        String sha256 = Hashing.sha256().hashBytes(buffer.getBytes()).toString();
        if (files.containsKey(sha256)) {
            Image image = new Image(name, sha256, buffer.length(), files.get(sha256));
            images.put(name, image);
            handler.handle(new SimpleAsyncResult<Image>(image));
            return;
        }

        final String path = storageDir + UUID.randomUUID();
        fs.writeFile(path, buffer, res -> {
            if (res.failed()) {
                handler.handle(new SimpleAsyncResult<Image>(res.cause()));
                return;
            }
            files.put(sha256, path);
            Image image = new Image(name, sha256, buffer.length(), files.get(sha256));
            images.put(name, image);
            handler.handle(new SimpleAsyncResult<Image>(image));
        });
    }

    @Override
    public void get(String nameOrId, Handler<AsyncResult<Image>> handler) {
        Optional<Image> image = null;
        if (Utils.isUUIDv4(nameOrId)) {
            image = images.values().parallelStream().filter(i -> i.getId().equals(nameOrId)).findFirst();
        } else {
            image = Optional.ofNullable(images.get(nameOrId));
        }
        if (image.isPresent()) {
            handler.handle(new SimpleAsyncResult<Image>(image.get()));
        } else {
            handler.handle(new SimpleAsyncResult<Image>(new BasicError(ErrorType.RESOURCE_NOT_FOUND)));
        }
    }

    @Override
    public void delete(String nameOrId, Handler<AsyncResult<Void>> handler) {
        get(nameOrId, res -> {
            if (res.failed()) {
                handler.handle(new SimpleAsyncResult<Void>(res.cause()));
                return;
            }
            final Image deleted = images.remove(res.result().getName());
            Optional<Image> sameHash = images.values().parallelStream()
                    .filter(i -> deleted.getHash().equals(i.getHash())).findFirst();
            if (sameHash.isPresent()) {
                handler.handle(new SimpleAsyncResult<Void>((Void) null));
                return;
            }
            Future<Void> deleteFuture = Future.future();
            deleteFuture.setHandler(v -> {
                files.remove(deleted.getHash());
                handler.handle(v);
            });
            fs.delete(files.get(deleted.getHash()), deleteFuture.completer());
        });
    }

    @Override
    public void getContent(String nameOrId, Handler<AsyncResult<Buffer>> handler) {
        get(nameOrId, res -> {
            if (res.failed()) {
                handler.handle(new SimpleAsyncResult<Buffer>(res.cause()));
                return;
            }
            String path = files.get(res.result().getHash());
            fs.readFile(path, handler);
        });
    }
}