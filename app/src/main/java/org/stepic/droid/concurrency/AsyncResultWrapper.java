package org.stepic.droid.concurrency;

public class AsyncResultWrapper<T> {
    T result;
    Exception exception;

    public Exception getException() {
        return exception;
    }

    public T getResult() {
        return result;
    }

    public AsyncResultWrapper (T result) {
        this.result = result;
    }

    public AsyncResultWrapper (Exception exception) {
        this.exception = exception;
    }
}
