package net.avh4.outline;

import android.support.annotation.NonNull;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

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
    public OutlineNode getRoot() {
        return nodes.get(root);
    }

    @NonNull
    public OutlineNode getNode(OutlineNodeId id) {
        OutlineNode outlineNode = nodes.get(id);
        if (outlineNode == null) {
            throw new IllegalArgumentException(this.toString() + " does not contain " + id.toString() + " (" + nodes.size() + " nodes)");
        }
        return outlineNode;
    }
}
