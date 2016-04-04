package net.avh4.outline.events;

import android.support.annotation.NonNull;
import com.fasterxml.jackson.core.JsonGenerator;
import net.avh4.Event;
import net.avh4.json.FromJsonValue;
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
            String node = context.getString();
            return new CompleteItem(new OutlineNodeId(node));
        }
    };
    public static String eventType = "complete";
    private final OutlineNodeId itemId;

    public CompleteItem(OutlineNodeId itemId) {
        this.itemId = itemId;
    }

    @NonNull
    @Override
    public Outline execute(@NonNull Outline data) {
        return data.updateChild(itemId, new Func1<OutlineNode, OutlineNode>() {
            @Override
            public OutlineNode call(OutlineNode outlineNode) {
                return outlineNode.complete();
            }
        });
    }

    @Override
    public void toJson(JsonGenerator generator) throws IOException {
        itemId.toJson(generator);
    }

    @Override
    public String eventType() {
        return eventType;
    }
}
