package net.avh4.outline.domain;

import net.avh4.time.Time;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class FakeTime implements Time {
    private final PublishSubject<Long> publishSubject = PublishSubject.create();
    private long nowMillis = System.currentTimeMillis();
    private Observable<Long> everyMinute;

    public FakeTime() {
        everyMinute = Observable.concat(Observable.fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return nowMillis;
            }
        }), publishSubject);
    }

    public void advance(int amount, TimeUnit unit) {
        long advanceMillis = unit.toMillis(amount);
        long newNowMillis = nowMillis + advanceMillis;
        nowMillis = newNowMillis;
        if (advanceMillis >= TimeUnit.MINUTES.toMillis(1)) {
            publishSubject.onNext(newNowMillis);
        }
    }

    @Override
    public Observable<Long> everyMinute() {
        return everyMinute;
    }

    @Override
    public long nowMillis() {
        return nowMillis;
    }
}
