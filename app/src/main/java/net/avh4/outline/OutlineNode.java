package net.avh4.outline;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
