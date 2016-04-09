package net.avh4.outline.events;

import android.support.annotation.NonNull;
import com.fasterxml.jackson.core.JsonGenerator;
import net.avh4.Event;
import net.avh4.json.FromJsonObject;
import net.avh4.json.FromJsonValue;
import net.avh4.json.JsonObjectReader;
import net.avh4.json.JsonValueReader;
import net.avh4.outline.Outline;
import net.avh4.outline.OutlineNode;
import net.avh4.outline.OutlineNodeId;
import rx.functions.Func1;

import java.io.IOException;

public class CompleteItem implements Event<Outline> {
    public static final FromJsonValue<CompleteItem> fromJson = new FromJsonValue<CompleteItem>() {
        @Override
        public CompleteItem call(JsonValueReader context) throws IOException {
            return context.getObject(new FromJsonObject<CompleteItem>() {
                @Override
                public CompleteItem call(JsonObjectReader context) throws IOException {
                    String node = context.getString("node");
                    long completedAt = context.getLong("completedAt");
                    return new CompleteItem(new OutlineNodeId(node), completedAt);
                }
            });
        }
    };
    public static String eventType = "complete2";
    private final OutlineNodeId itemId;
    private final long completedAt;

    public CompleteItem(OutlineNodeId itemId, long completedAt) {
        this.itemId = itemId;
        this.completedAt = completedAt;
    }

    @NonNull
    @Override
    public Outline execute(@NonNull Outline data) {
        return data.updateChild(itemId, new Func1<OutlineNode, OutlineNode>() {
            @Override
            public OutlineNode call(OutlineNode outlineNode) {
                return outlineNode.complete(completedAt);
            }
        });
    }

    @Override
    public void toJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("node");
        itemId.toJson(generator);
        generator.writeNumberField("completedAt", completedAt);
        generator.writeEndObject();
    }

    @Override
    public String eventType() {
        return eventType;
    }
}
