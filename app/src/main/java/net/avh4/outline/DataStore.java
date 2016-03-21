package net.avh4.outline;

import android.content.Context;
import com.fasterxml.jackson.core.JsonGenerator;
import net.avh4.F1;
import net.avh4.json.JsonHelper;
import org.pcollections.PVector;
import org.pcollections.TreePVector;
import rx.Observable;
import rx.subjects.ReplaySubject;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

class DataStore {

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private PVector<String> items;
    private ReplaySubject<PVector<String>> outlineSubject = ReplaySubject.createWithSize(1);
    private EventStore eventStore;

    DataStore() {
        items = TreePVector.empty();
    }

    void initialize(Context context) throws IOException {
        if (initialized.getAndSet(true)) {
            return;
        }
        eventStore = new EventStore(context);
        eventStore.iterate(new F1<Event>() {
            @Override
            public void call(Event event) {
                items = event.execute(items);
            }
        });
        outlineSubject.onNext(items);
    }

    private void processEvent(Event e) {
        if (!initialized.get()) {
            throw new IllegalStateException("Not initialized");
        }
        eventStore.record(e);
        items = e.execute(items);
        outlineSubject.onNext(items);
    }

    void addItem(String input) {
        Event e = new Add(input);
        processEvent(e);
    }

    void deleteItem(int position) {
        Event e = new Delete(position);
        processEvent(e);
    }

    Observable<PVector<String>> getOutline() {
        return outlineSubject;
    }

    interface Event {
        PVector<String> execute(PVector<String> items);

        void toJson(JsonGenerator generator) throws IOException;
    }

    static class Add implements Event {
        static final JsonHelper.ValueCallback<Add> fromJson = new JsonHelper.ValueCallback<Add>() {
            @Override
            public Add call(JsonHelper.ValueContext context) throws IOException {
                String value = context.getString();
                return new Add(value);
            }
        };
        private final String input;

        Add(String input) {
            this.input = input;
        }

        @Override
        public PVector<String> execute(PVector<String> items) {
            return items.plus(input);
        }

        @Override
        public void toJson(JsonGenerator generator) throws IOException {
            generator.writeString(input);
        }
    }

    static class Delete implements Event {
        static final JsonHelper.ValueCallback<Delete> fromJson = new JsonHelper.ValueCallback<Delete>() {
            @Override
            public Delete call(JsonHelper.ValueContext context) throws IOException {
                int value = context.getInt();
                return new Delete(value);
            }
        };
        private final int position;

        Delete(int position) {
            this.position = position;
        }

        @Override
        public PVector<String> execute(PVector<String> items) {
            return items.minus(position);
        }

        @Override
        public void toJson(JsonGenerator generator) throws IOException {
            generator.writeNumber(position);
        }
    }
}
