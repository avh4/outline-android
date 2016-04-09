package net.avh4.observables.test;

import rx.Observer;

public class Capture<T> implements Observer<T> {
    private boolean sawValue = false;
    private T value;
    private Throwable error;

    @Override
    public void onCompleted() {
    }

    @Override
    public void onError(Throwable e) {
        this.error = e;
    }

    @Override
    public void onNext(T t) {
        this.sawValue = true;
        this.value = t;
    }

    public T getValue() {
        if (error != null) {
            throw new RuntimeException("Previously observed an error", error);
        } else if (!sawValue) {
            throw new RuntimeException("No observed values");
        } else {
            return value;
        }
    }
}
