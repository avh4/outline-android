package net.avh4.outline;

import android.support.annotation.NonNull;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import rx.functions.Func1;

import java.util.Map;

public class Outline {
    private final OutlineNodeId root;
    private final PMap<OutlineNodeId, OutlineNode> nodes;

    private Outline(OutlineNodeId root, PMap<OutlineNodeId, OutlineNode> nodes) {
        this.root = root;
        this.nodes = nodes;
    }

    static Outline empty() {
        OutlineNodeId root = new OutlineNodeId("");
        return new Outline(root, HashTreePMap.singleton(root, new OutlineNode(root)));
    }

    public Outline addChild(OutlineNodeId parent, OutlineNodeId childId, String input) {
        PMap<OutlineNodeId, OutlineNode> newNodes = nodes
                .plus(parent, getNode(parent).addChild(childId))
                .plus(childId, new OutlineNode(childId, input));
        return new Outline(root, newNodes);
    }

    public Outline deleteNode(OutlineNodeId node) {
        if (node.equals(root)) {
            throw new IllegalArgumentException("Cannot delete root node");
        }
        PMap<OutlineNodeId, OutlineNode> newNodes = nodes.minus(node);
        for (Map.Entry<OutlineNodeId, OutlineNode> entry : newNodes.entrySet()) {
            newNodes = newNodes.plus(entry.getKey(), entry.getValue().removeChild(node));
        }
        return new Outline(root, newNodes);
    }

    @NonNull
    public OutlineNodeId getRoot() {
        return root;
    }

    @NonNull
    public OutlineNode getNode(OutlineNodeId id) {
        OutlineNode outlineNode = nodes.get(id);
        if (outlineNode == null) {
            throw new IllegalArgumentException(this.toString() + " does not contain " + id.toString() + " (" + nodes.size() + " nodes)");
        }
        return outlineNode;
    }

    @NonNull
    public Outline updateChild(OutlineNodeId itemId, Func1<OutlineNode, OutlineNode> transform) {
        PMap<OutlineNodeId, OutlineNode> newNodes = nodes
                .plus(itemId, transform.call(getNode(itemId)));
        return new Outline(root, newNodes);
    }

    @NonNull
    public Outline move(@NonNull OutlineNodeId id, @NonNull OutlineNodeId from, @NonNull OutlineNodeId to) {
        OutlineNode newTo = nodes.get(to).addChild(id);
        OutlineNode newFrom = nodes.get(from).removeChild(id);
        PMap<OutlineNodeId, OutlineNode> newNodes = nodes
                .plus(from, newFrom)
                .plus(to, newTo);
        return new Outline(root, newNodes);
    }
}
