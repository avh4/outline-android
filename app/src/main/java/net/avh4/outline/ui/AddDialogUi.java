package net.avh4.outline.ui;

import net.avh4.outline.DataStore;
import net.avh4.outline.Generator;
import net.avh4.outline.OutlineNodeId;

public class AddDialogUi {
    private final DataStore dataStore;
    private final OutlineNodeId parent;
    private final Generator<OutlineNodeId> idGenerator;

    public AddDialogUi(DataStore dataStore, OutlineNodeId parent, Generator<OutlineNodeId> idGenerator) {
        this.dataStore = dataStore;
        this.parent = parent;
        this.idGenerator = idGenerator;
    }

    public void submit(String text) {
        OutlineNodeId itemId = idGenerator.next();
        dataStore.addItem(parent, itemId, text);
    }
}
