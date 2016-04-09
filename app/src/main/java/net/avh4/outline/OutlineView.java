package net.avh4.outline;

import org.pcollections.PVector;
import org.pcollections.TreePVector;

public class OutlineView {
    private final Outline outline;
    private final OutlineNode parent;
    private final OutlineNode current;
    private final PVector<OutlineNodeId> children;

    OutlineView(Outline outline, OutlineNodeId current, OutlineNodeId parent, long nowMillis) {
        this.outline = outline;
        this.parent = parent == null ? null : outline.getNode(parent);
        this.current = outline.getNode(current);
        children = getVisibleChildren(nowMillis, this.current.getChildren());
    }

    private PVector<OutlineNodeId> getVisibleChildren(long nowMillis, PVector<OutlineNodeId> children) {
        PVector<OutlineNodeId> visibleChildren = TreePVector.empty();
        for (OutlineNodeId childId : children) {
            OutlineNode child = outline.getNode(childId);
            if (child.isVisible(nowMillis)) {
                visibleChildren = visibleChildren.plus(childId);
            }
        }
        return visibleChildren;
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
