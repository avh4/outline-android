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

import java.io.IOException;

public class Add implements Event<Outline> {
    public static final FromJsonValue<Add> fromJson = new FromJsonValue<Add>() {
        @Override
        public Add call(JsonValueReader context) throws IOException {
            return context.getObject(new FromJsonObject<Add>() {
                @Override
                public Add call(JsonObjectReader context) throws IOException {
                    String parent = context.getString("parent");
                    String id = context.getString("id");
                    String value = context.getString("value");
                    return new Add(new OutlineNodeId(parent), new OutlineNodeId(id), value);
                }
            });
        }
    };
    public static final String eventType = "add";
    private final @NonNull OutlineNodeId parent;
    private final @NonNull OutlineNodeId id;
    private final @NonNull String value;

    public Add(@NonNull OutlineNodeId parent, @NonNull OutlineNodeId id, @NonNull String value) {
        this.parent = parent;
        this.id = id;
        this.value = value;
    }

    @NonNull
    @Override
    public Outline execute(@NonNull Outline outline) {
        return outline.addChild(parent, id, value);
    }

    @Override
    public void toJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("parent", parent.toString());
        generator.writeStringField("id", id.toString());
        generator.writeStringField("value", value);
        generator.writeEndObject();
    }

    @Override
    public String eventType() {
        return eventType;
    }

    @Override
    public String toString() {
        return "Add{" +
                "parent=" + parent +
                ", id=" + id +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Add add = (Add) o;

        return parent.equals(add.parent) && id.equals(add.id) && value.equals(add.value);
    }

    @Override
    public int hashCode() {
        int result = parent.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}
