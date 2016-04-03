package net.avh4.outline.domain;

import net.avh4.outline.*;
import net.avh4.outline.features.importing.ImportAction;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;

public class TestApp {
    private static final AtomicInteger appId = new AtomicInteger();
    private final EventStore eventStore = mock(EventStore.class);
    private final DataStore dataStore = new DataStore(eventStore);
    private final MainUi mainUi = new MainUi(dataStore);
    private final Filesystem filesystem;
    private Generator<OutlineNodeId> idGenerator;
    private AppAction.OnError errorHandler = new AppAction.OnError() {
        @Override
        public void onError(Throwable err) {
            throw new RuntimeException(err);
        }
    };

    public TestApp(Filesystem filesystem) {
        this.filesystem = filesystem;
        String deviceId = "TestApp-" + appId.incrementAndGet();
        idGenerator = new IdGenerator(deviceId);

        try {
            dataStore.initialize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Outline inspectOutline() {
        return dataStore.getOutline().toBlocking().first();
    }

    public void importFile(String filename) {
        new ImportAction(dataStore, idGenerator, filesystem, filename).run(errorHandler);
    }

    public OutlineView inspectOutlineView() {
        return mainUi.getOutlineView().toBlocking().first();
    }
}
