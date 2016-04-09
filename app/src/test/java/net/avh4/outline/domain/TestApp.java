package net.avh4.outline.domain;

import net.avh4.observables.test.Capture;
import net.avh4.outline.*;
import net.avh4.outline.features.importing.ImportAction;
import net.avh4.outline.ui.AddDialogUi;
import net.avh4.time.Time;
import rx.functions.Action1;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class TestApp {
    private static final AtomicInteger appId = new AtomicInteger();
    private final EventStore eventStore = mock(EventStore.class);
    private final DataStore dataStore = new DataStore(eventStore);
    private final MainUi mainUi;
    private final Filesystem filesystem;
    private final Capture<OutlineView> outlineView = new Capture<>();
    private Generator<OutlineNodeId> idGenerator;
    private AppAction.OnError errorHandler = new AppAction.OnError() {
        @Override
        public void onError(Throwable err) {
            throw new RuntimeException(err);
        }
    };

    public TestApp(Filesystem filesystem, Time time) {
        this.filesystem = filesystem;
        String deviceId = "TestApp-" + appId.incrementAndGet();
        idGenerator = new IdGenerator(deviceId);
        mainUi = new MainUi(dataStore, time);

        mainUi.getOutlineView().subscribe(outlineView);

        try {
            dataStore.initialize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Outline inspectOutline() {
        return outlineView.getValue().getOutline();
    }

    public void importFile(String filename) {
        new ImportAction(dataStore, idGenerator, filesystem, filename).run(errorHandler);
    }

    public OutlineView inspectOutlineView() {
        return outlineView.getValue();
    }

    public void addItem(final String text) {
        mainUi.showAddDialog().subscribe(new Action1<AddDialogUi>() {
            @Override
            public void call(AddDialogUi addDialogUi) {
                addDialogUi.submit(text);
            }
        });
    }

    public void completeItem(String text) {
        OutlineNode target = assertSeesItem(text);
        mainUi.completeAction(target.getId()).run(errorHandler);
    }

    public OutlineNode assertSeesItem(String itemName) {
        OutlineNode item = seeItem(itemName);
        assertThat(item).isNotNull();
        return item;
    }

    public void enter(String itemName) {
        OutlineNode node = assertSeesItem(itemName);
        mainUi.enter(node.getId());
    }

    public void goUp() {
        mainUi.back();
    }

    public OutlineNode seeItem(String itemName) {
        OutlineView outlineView = inspectOutlineView();

        for (int i = 0; i < outlineView.getNumberOfChildren(); i++) {
            OutlineNode child = outlineView.getChild(i);
            String childText = child.getText();
            if (childText.equals(itemName)) {
                return child;
            }
        }

        return null;
    }
}
