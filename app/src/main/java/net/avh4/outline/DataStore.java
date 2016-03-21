package net.avh4.outline;

import android.content.Context;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import net.avh4.F1;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

class DataStore {

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private PVector<String> items;
    private Listener listener = null;
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
    }

    private void processEvent(Event e) {
        if (!initialized.get()) {
            throw new IllegalStateException("Not initialized");
        }
        eventStore.record(e);
        items = e.execute(items);
        listener.onItemsChanged(items);
    }

    void addItem(String input) {
        Event e = new Add(input);
        processEvent(e);
    }

    void deleteItem(int position) {
        Event e = new Delete(position);
        processEvent(e);
    }

    void setListener(Listener listener) {
        this.listener = listener;
        listener.onItemsChanged(items);
    }

    interface Listener {
        void onItemsChanged(PVector<String> items);
    }

    interface Event {
        PVector<String> execute(PVector<String> items);

        void toJson(JsonGenerator generator) throws IOException;
    }

    static class Add implements Event {
        private final String input;

        Add(String input) {
            this.input = input;
        }

        static Add fromJson(JsonParser parser) throws IOException {
            if (parser.nextToken() != JsonToken.VALUE_STRING) {
                throw new IOException("Add: expected string value, but got " + parser.getCurrentToken());
            }
            String value = parser.getText();
            return new Add(value);
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
        private final int position;

        Delete(int position) {
            this.position = position;
        }

        static Delete fromJson(JsonParser parser) throws IOException {
            if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) {
                throw new IOException("Delete: expected integer value");
            }
            int value = parser.getIntValue();
            return new Delete(value);
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
