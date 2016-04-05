package net.avh4.outline;

import org.pcollections.PVector;

public class OutlineView {
    private final Outline outline;
    private final OutlineNode focusNode;
    private final PVector<OutlineNodeId> children;

    OutlineView(Outline outline, OutlineNodeId focus) {
        this.outline = outline;
        focusNode = outline.getNode(focus);
        children = focusNode.getChildren();
    }

    public int getNumberOfChildren() {
        return children.size();
    }

    public OutlineNode getChild(int position) {
        OutlineNodeId childId = children.get(position);
        return outline.getNode(childId);
    }

    OutlineNode getNode() {
        return focusNode;
    }

    int getDisplayCount(OutlineNode item) {
        int count = 0;
        for (OutlineNodeId childId : item.getChildren()) {
            OutlineNode child = outline.getNode(childId);
            if (!child.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    public Outline getOutline() {
        return outline;
    }
}
