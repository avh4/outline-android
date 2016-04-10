package net.avh4.outline;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.util.concurrent.TimeUnit;

public class OutlineNode {
    private final OutlineNodeId id;
    private final String text;
    private final PVector<OutlineNodeId> children;
    @Nullable private final Long completedAt;

    OutlineNode(OutlineNodeId id) {
        this(id, "Outline");
    }

    OutlineNode(OutlineNodeId id, String text) {
        this(id, text, TreePVector.<OutlineNodeId>empty(), null);
    }

    private OutlineNode(OutlineNodeId id, String text, PVector<OutlineNodeId> children, @Nullable Long completedAt) {
        this.id = id;
        this.text = text;
        this.children = children;
        this.completedAt = completedAt;
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
        return new OutlineNode(id, text, children.plus(childId), completedAt);
    }

    @NonNull
    OutlineNode removeChild(OutlineNodeId childId) {
        return new OutlineNode(id, text, children.minus(childId), completedAt);
    }

    @NonNull
    public OutlineNode complete(long completedAt) {
        return new OutlineNode(id, text, children, completedAt);
    }

    @NonNull
    public OutlineNode uncomplete() {
        return new OutlineNode(id, text, children, null);
    }

    public boolean isCompleted() {
        return completedAt != null;
    }

    @Nullable
    public Long completedAt() {
        return completedAt;
    }

    boolean isVisible(long nowMillis) {
        Long completedAt = completedAt();
        return completedAt == null || completedAt > nowMillis - TimeUnit.DAYS.toMillis(1);
    }

    public OutlineNode reorderChildren(PVector<OutlineNodeId> newOrder) {
        PVector<OutlineNodeId> newChildren = TreePVector.empty();
        PSet<OutlineNodeId> remaining = HashTreePSet.from(children);

        // Add newOrder that exist in the parent
        for (OutlineNodeId c : newOrder) {
            if (remaining.contains(c)) {
                newChildren = newChildren.plus(c);
                remaining = remaining.minus(c);
            }
        }

        // Add any missing from newOrder
        for (OutlineNodeId child : children) {
            if (remaining.contains(child)) {
                newChildren = newChildren.plus(child);
                remaining = remaining.minus(child);
            }
        }

        if (!remaining.isEmpty()) {
            throw new RuntimeException("Reorder didn't match every child");
        }

        return new OutlineNode(id, text, newChildren, completedAt);
    }

    @Override
    public String toString() {
        return "OutlineNode{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", children=" + children +
                ", completedAt=" + completedAt +
                '}';
    }
}
