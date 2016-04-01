package net.avh4.outline;

import com.fasterxml.jackson.core.JsonGenerator;
import net.avh4.Event;
import net.avh4.json.FromJsonValue;
import net.avh4.json.JsonValueReader;

import java.io.IOException;

class Delete implements Event<Outline> {
    static final FromJsonValue<Delete> fromJson = new FromJsonValue<Delete>() {
        @Override
        public Delete call(JsonValueReader context) throws IOException {
            String node = context.getString();
            return new Delete(new OutlineNodeId(node));
        }
    };
    private final OutlineNodeId node;

    Delete(OutlineNodeId node) {
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
}