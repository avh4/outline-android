package net.avh4.outline;

import android.content.Context;
import com.fasterxml.jackson.core.JsonGenerator;
import net.avh4.F1;
import net.avh4.json.JsonHelper;
import rx.Observable;
import rx.subjects.ReplaySubject;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

class DataStore {

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final ReplaySubject<Outline> outlineSubject;
    private final IdGenerator idGenerator;
    private Outline outline;
    private EventStore eventStore;

    DataStore(String deviceId) {
        outline = Outline.empty();
        outlineSubject = ReplaySubject.createWithSize(1);
        idGenerator = new IdGenerator(deviceId);
    }

    void initialize(Context context) throws IOException {
        if (initialized.getAndSet(true)) {
            return;
        }
        eventStore = new EventStore(context);
        eventStore.iterate(new F1<Event>() {
            @Override
            public void call(Event event) {
                outline = event.execute(outline);
            }
        });
        outlineSubject.onNext(outline);
    }

    private void processEvent(Event e) {
        if (!initialized.get()) {
            throw new IllegalStateException("Not initialized");
        }
        eventStore.record(e);
        outline = e.execute(outline);
        outlineSubject.onNext(outline);
    }

    void addItem(OutlineNodeId parent, String input) {
        OutlineNodeId id = idGenerator.next();
        Event e = new Add(parent, id, input);
        processEvent(e);
    }

    void deleteItem(OutlineNodeId node) {
        Event e = new Delete(node);
        processEvent(e);
    }

    Observable<Outline> getOutline() {
        return outlineSubject;
    }

    interface Event {
        Outline execute(Outline outline);

        void toJson(JsonGenerator generator) throws IOException;
    }

    static class Add implements Event {
        static final JsonHelper.ValueCallback<Add> fromJson = new JsonHelper.ValueCallback<Add>() {
            @Override
            public Add call(JsonHelper.ValueContext context) throws IOException {
                return context.getObject(new JsonHelper.ObjectCallback<Add>() {
                    @Override
                    public Add call(JsonHelper.ObjectContext context) throws IOException {
                        String parent = context.getString("parent");
                        String id = context.getString("id");
                        String value = context.getString("value");
                        return new Add(new OutlineNodeId(parent), new OutlineNodeId(id), value);
                    }
                });
            }
        };
        private final OutlineNodeId parent;
        private final OutlineNodeId id;
        private final String value;

        Add(OutlineNodeId parent, OutlineNodeId id, String value) {
            this.parent = parent;
            this.id = id;
            this.value = value;
        }

        @Override
        public Outline execute(Outline outline) {
            return outline.addChild(parent, id, value);
        }

        @Override
        public void toJson(JsonGenerator generator) throws IOException {
            generator.writeStartObject();
            generator.writeStringField("parent", parent.toString());
            generator.writeStringField("id", id.toString());
            generator.writeStringField("value", value);
            generator.writeEndObject();
        }
    }

    static class Delete implements Event {
        static final JsonHelper.ValueCallback<Delete> fromJson = new JsonHelper.ValueCallback<Delete>() {
            @Override
            public Delete call(JsonHelper.ValueContext context) throws IOException {
                String node = context.getString();
                return new Delete(new OutlineNodeId(node));
            }
        };
        private final OutlineNodeId node;

        Delete(OutlineNodeId node) {
            this.node = node;
        }

        @Override
        public Outline execute(Outline outline) {
            return outline.deleteNode(node);
        }

        @Override
        public void toJson(JsonGenerator generator) throws IOException {
            node.toJson(generator);
        }
    }
}
