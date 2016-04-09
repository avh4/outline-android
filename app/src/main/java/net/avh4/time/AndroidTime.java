package net.avh4.time;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

import java.util.concurrent.TimeUnit;

public class AndroidTime implements Time {
    @Override
    public Observable<Long> everyMinute() {
        return Observable.interval(0, 1, TimeUnit.MINUTES).map(new Func1<Object, Long>() {
            @Override
            public Long call(Object o) {
                return nowMillis();
            }
        })
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public long nowMillis() {
        return System.currentTimeMillis();
    }
}
