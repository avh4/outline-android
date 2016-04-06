package net.avh4.outline;

import android.support.annotation.NonNull;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

public class OutlineNode {
    private final OutlineNodeId id;
    private final String text;
    private final PVector<OutlineNodeId> children;
    private final boolean isCompleted;

    OutlineNode(OutlineNodeId id) {
        this(id, "Outline");
    }

    OutlineNode(OutlineNodeId id, String text) {
        this(id, text, TreePVector.<OutlineNodeId>empty(), false);
    }

    private OutlineNode(OutlineNodeId id, String text, PVector<OutlineNodeId> children, boolean isCompleted) {
        this.id = id;
        this.text = text;
        this.children = children;
        this.isCompleted = isCompleted;
    }

    public OutlineNodeId getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public PVector<OutlineNodeId> getChildren() {
        return children;
    }

    @NonNull
    OutlineNode addChild(OutlineNodeId childId) {
        return new OutlineNode(id, text, children.plus(childId), isCompleted);
    }

    @NonNull
    OutlineNode removeChild(OutlineNodeId childId) {
        return new OutlineNode(id, text, children.minus(childId), isCompleted);
    }

    @NonNull
    public OutlineNode complete() {
        return new OutlineNode(id, text, children, true);
    }

    @NonNull
    public OutlineNode uncomplete() {
        return new OutlineNode(id, text, children, false);
    }

    public boolean isCompleted() {
        return isCompleted;
    }
}
