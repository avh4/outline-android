package net.avh4.outline.events;

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
    private final OutlineNodeId parent;
    private final OutlineNodeId id;
    private final String value;

    public Add(OutlineNodeId parent, OutlineNodeId id, String value) {
        this.parent = parent;
        this.id = id;
        this.value = value;
    }

    @Override
    public Outline execute(Outline outline) {
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
}
