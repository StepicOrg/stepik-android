package org.stepic.droid.concurrency.tasks;

public class AsyncResultWrapper<T> {
    private T result;
    private Throwable exception;

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
