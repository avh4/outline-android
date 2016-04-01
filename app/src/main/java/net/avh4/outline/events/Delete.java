package net.avh4.outline.events;

import com.fasterxml.jackson.core.JsonGenerator;
import net.avh4.Event;
import net.avh4.json.FromJsonValue;
import net.avh4.json.JsonValueReader;
import net.avh4.outline.Outline;
import net.avh4.outline.OutlineNodeId;

import java.io.IOException;

public class Delete implements Event<Outline> {
    public static final String eventType = "delete";
    public static final FromJsonValue<Delete> fromJson = new FromJsonValue<Delete>() {
        @Override
        public Delete call(JsonValueReader context) throws IOException {
            String node = context.getString();
            return new Delete(new OutlineNodeId(node));
        }
    };
    private final OutlineNodeId node;

    public Delete(OutlineNodeId node) {
        this.node = node;
    }

    @Override
    public Outline execute(Outline outline) {
        return outline.deleteNode(node);
    }

    @Override
    public void toJson(JsonGenerator generator) throws IOException {
        node.toJson(generator);
    }

    @Override
    public String eventType() {
        return eventType;
    }
}
