package net.avh4.outline.domain;

import net.avh4.outline.*;
import net.avh4.outline.features.importing.ImportAction;
import net.avh4.outline.ui.AddDialogUi;
import rx.functions.Action1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Java6Assertions.assertThat;
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
        OutlineView outlineView = inspectOutlineView();
        ArrayList<String> seen = new ArrayList<>();

        for (int i = 0; i < outlineView.getNumberOfChildren(); i++) {
            OutlineNode child = outlineView.getChild(i);
            String childText = child.getText();
            seen.add(childText);
            if (childText.equals(itemName)) {
                return child;
            }
        }

        assertThat(seen).contains(itemName);
        return null;
    }

    public void enter(String itemName) {
        OutlineNode node = assertSeesItem(itemName);
        mainUi.enter(node.getId());
    }

    public void goUp() {
        mainUi.back();
    }
}
