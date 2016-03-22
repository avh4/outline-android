package net.avh4.outline;

import org.pcollections.PVector;

class OutlineView {
    private final Outline outline;
    private final OutlineNode focusNode;
    private final PVector<OutlineNodeId> children;

    OutlineView(Outline outline, OutlineNodeId focus) {
        this.outline = outline;
        focusNode = outline.getNode(focus);
        children = focusNode.getChildren();
    }

    int getNumberOfChildren() {
        return children.size();
    }

    OutlineNode getChild(int position) {
        OutlineNodeId childId = children.get(position);
        return outline.getNode(childId);
    }

    OutlineNode getNode() {
        return focusNode;
    }
}
