package net.avh4.rx;

import org.pcollections.ConsPStack;
import org.pcollections.PStack;
import rx.Observable;
import rx.subjects.ReplaySubject;

public class PathHistory<T> {
    private final ReplaySubject<HistoryFrame<T>> output = ReplaySubject.createWithSize(1);
    private PStack<T> stack = ConsPStack.empty();

    public void push(T next) {
        T parent = stack.get(0);
        stack = stack.plus(next);

        output.onNext(new HistoryFrame<>(parent, next));
    }

    public boolean pop() {
        if (stack.size() <= 1) return false;

        stack = stack.minus(0);
        T current = stack.get(0);
        T parent = stack.get(1);

        output.onNext(new HistoryFrame<>(parent, current));
        return true;
    }

    public Observable<HistoryFrame<T>> getCurrent() {
        return output;
    }

    public static class HistoryFrame<T> {
        private final T parent;
        private final T current;

        public HistoryFrame(T parent, T current) {
            this.parent = parent;
            this.current = current;
        }

        @Override
        public String toString() {
            return "HistoryFrame{" +
                    "parent=" + parent +
                    ", current=" + current +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            HistoryFrame<?> that = (HistoryFrame<?>) o;

            if (parent != null ? !parent.equals(that.parent) : that.parent != null) return false;
            return current != null ? current.equals(that.current) : that.current == null;

        }

        @Override
        public int hashCode() {
            int result = parent != null ? parent.hashCode() : 0;
            result = 31 * result + (current != null ? current.hashCode() : 0);
            return result;
        }

        public T getParent() {
            return parent;
        }

        public T getCurrent() {
            return current;
        }
    }
}
