package net.avh4.outline.events.legacy;

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

/**
 * @deprecated use {@link net.avh4.outline.events.CompleteItem} instead
 */
@Deprecated
public class LegacyCompleteItem1 implements Event<Outline> {
    public static final FromJsonValue<LegacyCompleteItem1> fromJson = new FromJsonValue<LegacyCompleteItem1>() {
        @Override
        public LegacyCompleteItem1 call(JsonValueReader context) throws IOException {
            String node = context.getString();
            return new LegacyCompleteItem1(new OutlineNodeId(node));
        }
    };
    public static String eventType = "complete";
    private final OutlineNodeId itemId;

    public LegacyCompleteItem1(OutlineNodeId itemId) {
        this.itemId = itemId;
    }

    @NonNull
    @Override
    public Outline execute(@NonNull Outline data) {
        return data.updateChild(itemId, new Func1<OutlineNode, OutlineNode>() {
            @Override
            public OutlineNode call(OutlineNode outlineNode) {
                return outlineNode.complete(1460146771000L);
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
