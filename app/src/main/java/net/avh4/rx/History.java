package net.avh4.rx;

import org.pcollections.ConsPStack;
import org.pcollections.PStack;
import rx.Observable;
import rx.functions.Func1;
import rx.subjects.ReplaySubject;

public class History<T> {
    private final ReplaySubject<PStack<T>> output = ReplaySubject.createWithSize(1);
    private PStack<T> stack = ConsPStack.empty();

    public void push(T next) {
        stack = stack.plus(next);
        output.onNext(stack);
    }

    public void pop() {
        stack = stack.minus(0);
        output.onNext(stack);
    }

    public Observable<T> getCurrent() {
        return output.map(new Func1<PStack<T>, T>() {
            @Override
            public T call(PStack<T> ts) {
                return ts.get(0);
            }
        });
    }

    public Observable<T> getParent() {
        return output.map(new Func1<PStack<T>, T>() {
            @Override
            public T call(PStack<T> ts) {
                return ts.get(1);
            }
        });
    }
}
