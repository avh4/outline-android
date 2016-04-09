package net.avh4.time;

import rx.Observable;

public interface Time {
    Observable<Long> everyMinute();

    long nowMillis();
}
