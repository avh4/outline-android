package net.avh4.outline;

import org.pcollections.PVector;

class OutlineView {
    private final Outline outline;
    private final PVector<OutlineNodeId> children;

    OutlineView(Outline outline, OutlineNodeId focus) {
        this.outline = outline;
        children = outline.getNode(focus).getChildren();
    }

    int getNumberOfChildren() {
        return children.size();
    }

    OutlineNode getChild(int position) {
        OutlineNodeId childId = children.get(position);
        return outline.getNode(childId);
    }
}
