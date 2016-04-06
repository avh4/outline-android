package net.avh4.outline;

import org.pcollections.PVector;

public class OutlineView {
    private final Outline outline;
    private final OutlineNode parent;
    private final OutlineNode current;
    private final PVector<OutlineNodeId> children;

    OutlineView(Outline outline, OutlineNodeId current, OutlineNodeId parent) {
        this.outline = outline;
        this.parent = parent == null ? null : outline.getNode(parent);
        this.current = outline.getNode(current);
        children = this.current.getChildren();
    }

    public int getNumberOfChildren() {
        return children.size();
    }

    public OutlineNode getChild(int position) {
        OutlineNodeId childId = children.get(position);
        return outline.getNode(childId);
    }

    OutlineNode getNode() {
        return current;
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

    public OutlineNode getParent() {
        return parent;
    }
}
