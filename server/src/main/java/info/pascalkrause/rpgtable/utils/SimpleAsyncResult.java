package info.pascalkrause.rpgtable.utils;

import java.util.Objects;

import io.vertx.core.AsyncResult;

public class SimpleAsyncResult<E> implements AsyncResult<E> {

    private final Throwable cause;
    private final E result;

    public SimpleAsyncResult(Throwable cause) {
        this.cause = cause;
        this.result = null;
    }
    
    public SimpleAsyncResult(E result) {
        this.cause = null;
        this.result = result;
    }

    @Override
    public E result() {
        return result;
    }

    @Override
    public Throwable cause() {
        return cause;
    }

    @Override
    public boolean succeeded() {
        return !failed();
    }

    @Override
    public boolean failed() {
        return Objects.nonNull(cause);
    }
}
