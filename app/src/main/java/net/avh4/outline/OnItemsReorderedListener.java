package net.avh4.outline;

import org.pcollections.PVector;

public interface OnItemsReorderedListener {
    void onItemsReordered(OutlineView current, PVector<Integer> newOrder);
}
