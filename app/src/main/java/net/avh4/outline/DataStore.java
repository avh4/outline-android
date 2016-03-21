package net.avh4.outline;

import org.pcollections.PVector;
import org.pcollections.TreePVector;

class DataStore {

    private PVector<String> items = TreePVector.empty();
    private Listener listener = null;

    void addItem(String input) {
        items = items.plus(input);
        listener.onItemsChanged(items);
    }

    void deleteItem(int position) {
        items = items.minus(position);
        listener.onItemsChanged(items);
    }

    void setListener(Listener listener) {
        this.listener = listener;
        listener.onItemsChanged(items);
    }

    interface Listener {
        void onItemsChanged(PVector<String> items);
    }
}
