package net.avh4.outline;

import android.content.Context;
import net.avh4.Event;
import net.avh4.F1;
import rx.Observable;
import rx.subjects.ReplaySubject;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

class DataStore {

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final ReplaySubject<Outline> outlineSubject;
    private Outline outline;
    private EventStore eventStore;

    DataStore() {
        outline = Outline.empty();
        outlineSubject = ReplaySubject.createWithSize(1);
    }

    void initialize(Context context) throws IOException {
        if (initialized.getAndSet(true)) {
            return;
        }
        eventStore = new EventStore(context);
        eventStore.iterate(new F1<Event<Outline>>() {
            @Override
            public void call(Event<Outline> event) {
                outline = event.execute(outline);
            }
        });
        outlineSubject.onNext(outline);
    }

    private void processEvent(Event<Outline> e) {
        if (!initialized.get()) {
            throw new IllegalStateException("Not initialized");
        }
        eventStore.record(e);
        outline = e.execute(outline);
        outlineSubject.onNext(outline);
    }

    void addItem(OutlineNodeId parent, OutlineNodeId itemId, String text) {
        Event<Outline> e = new Add(parent, itemId, text);
        processEvent(e);
    }

    void deleteItem(OutlineNodeId node) {
        Event<Outline> e = new Delete(node);
        processEvent(e);
    }

    Observable<Outline> getOutline() {
        return outlineSubject;
    }

}
