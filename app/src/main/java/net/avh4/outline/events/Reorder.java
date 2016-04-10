package net.avh4.outline.events;

import android.support.annotation.NonNull;
import com.fasterxml.jackson.core.JsonGenerator;
import net.avh4.Event;
import net.avh4.json.FromJsonObject;
import net.avh4.json.FromJsonValue;
import net.avh4.json.JsonObjectReader;
import net.avh4.json.JsonValueReader;
import net.avh4.outline.Outline;
import net.avh4.outline.OutlineNodeId;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.io.IOException;
import java.util.List;

public class Reorder implements Event<Outline> {
    public static final FromJsonValue<Reorder> fromJson = new FromJsonValue<Reorder>() {
        @Override
        public Reorder call(JsonValueReader context) throws IOException {
            return context.getObject(new FromJsonObject<Reorder>() {
                @Override
                public Reorder call(JsonObjectReader context) throws IOException {
                    OutlineNodeId parent = context.getValue("parent", OutlineNodeId.fromJson);
                    List<OutlineNodeId> children = context.getArray("children", OutlineNodeId.fromJson);
                    return new Reorder(parent, TreePVector.from(children));
                }
            });
        }
    };
    public static final String eventType = "reorder";
    @NonNull private final OutlineNodeId parent;
    @NonNull private final PVector<OutlineNodeId> children;

    public Reorder(@NonNull OutlineNodeId parent, @NonNull PVector<OutlineNodeId> children) {
        this.parent = parent;
        this.children = children;
    }

    @NonNull
    @Override
    public Outline execute(@NonNull Outline outline) {
        return outline.reorder(parent, children);
    }

    @Override
    public void toJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("parent");
        parent.toJson(generator);
        generator.writeArrayFieldStart("children");
        for (OutlineNodeId child : children) {
            child.toJson(generator);
        }
        generator.writeEndArray();
        generator.writeEndObject();
    }

    @Override
    public String eventType() {
        return eventType;
    }

    @Override
    public String toString() {
        return "Reorder{" +
                "parent=" + parent +
                ", children=" + children +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reorder reorder = (Reorder) o;

        return parent.equals(reorder.parent) && children.equals(reorder.children);
    }

    @Override
    public int hashCode() {
        int result = parent.hashCode();
        result = 31 * result + children.hashCode();
        return result;
    }
}
