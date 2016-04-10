package net.avh4.outline;

import android.support.annotation.NonNull;
import com.fasterxml.jackson.core.JsonGenerator;
import net.avh4.json.FromJsonValue;
import net.avh4.json.JsonValueReader;

import java.io.IOException;

public class OutlineNodeId {
    public static FromJsonValue<OutlineNodeId> fromJson = new FromJsonValue<OutlineNodeId>() {
        @Override
        public OutlineNodeId call(JsonValueReader valueContext) throws IOException {
            String id = valueContext.getString();
            return new OutlineNodeId(id);
        }
    };
    private final @NonNull String id;

    public OutlineNodeId(@NonNull String id) {
        this.id = id;
    }

    public void toJson(JsonGenerator generator) throws IOException {
        generator.writeString(id);
    }

    boolean isRootNode() {
        return id.equals("");
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OutlineNodeId that = (OutlineNodeId) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
