package net.avh4.outline;

import net.avh4.Event;
import net.avh4.outline.events.Add;
import net.avh4.outline.events.Delete;
import rx.Observable;
import rx.functions.Action1;
import rx.subjects.ReplaySubject;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataStore {

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final ReplaySubject<Outline> outlineSubject;
    private final EventStore eventStore;
    private Outline outline;

    public DataStore(EventStore eventStore) {
        outline = Outline.empty();
        outlineSubject = ReplaySubject.createWithSize(1);
        outlineSubject.onNext(outline);
        this.eventStore = eventStore;
    }

    public void initialize() throws IOException {
        if (initialized.getAndSet(true)) {
            return;
        }
        eventStore.iterate(new Action1<Event<Outline>>() {
            @Override
            public void call(Event<Outline> event) {
                outline = event.execute(outline);
            }
        });
        outlineSubject.onNext(outline);
    }

    public void processEvent(Event<Outline> e) {
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

    public Observable<Outline> getOutline() {
        return outlineSubject;
    }
}
