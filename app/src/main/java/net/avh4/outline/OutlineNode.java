package net.avh4.outline;

import org.pcollections.PVector;
import org.pcollections.TreePVector;

class OutlineNode {
    private final OutlineNodeId id;
    private final String text;
    private final PVector<OutlineNodeId> children;

    OutlineNode(OutlineNodeId id) {
        this(id, "Outline");
    }

    OutlineNode(OutlineNodeId id, String text) {
        this(id, text, TreePVector.<OutlineNodeId>empty());
    }

    private OutlineNode(OutlineNodeId id, String text, PVector<OutlineNodeId> children) {
        this.id = id;
        this.text = text;
        this.children = children;
    }

    public OutlineNodeId getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    PVector<OutlineNodeId> getChildren() {
        return children;
    }

    OutlineNode addChild(OutlineNodeId childId) {
        return new OutlineNode(id, text, children.plus(childId));
    }

    OutlineNode removeChild(OutlineNodeId childId) {
        return new OutlineNode(id, text, children.minus(childId));
    }

    boolean isRootNode() {
        return id.isRootNode();
    }
}
