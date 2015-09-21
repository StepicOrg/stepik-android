package org.stepic.droid.concurrency;

public class AsyncResultWrapper<T> {
    T result;
    Throwable exception;

    public Throwable getException() {
        return exception;
    }

    public T getResult() {
        return result;
    }

    public AsyncResultWrapper (T result) {
        this.result = result;
    }

    public AsyncResultWrapper (Throwable exception) {
        this.exception = exception;
    }
}
