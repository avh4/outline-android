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

public class Move implements Event<Outline> {
    public static final FromJsonValue<Move> fromJson = new FromJsonValue<Move>() {
        @Override
        public Move call(JsonValueReader context) throws IOException {
            return context.getObject(new FromJsonObject<Move>() {
                @Override
                public Move call(JsonObjectReader context) throws IOException {
                    String id = context.getString("id");
                    String from = context.getString("from");
                    String to = context.getString("to");
                    return new Move(new OutlineNodeId(id), new OutlineNodeId(from), new OutlineNodeId(to));
                }
            });
        }
    };
    public static final String eventType = "move";
    @NonNull private final OutlineNodeId id;
    @NonNull private final OutlineNodeId from;
    @NonNull private final OutlineNodeId to;

    public Move(@NonNull OutlineNodeId id, @NonNull OutlineNodeId from, @NonNull OutlineNodeId to) {
        this.id = id;
        this.from = from;
        this.to = to;
    }

    @NonNull
    @Override
    public Outline execute(@NonNull Outline outline) {
        return outline.move(id, from, to);
    }

    @Override
    public void toJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("id", id.toString());
        generator.writeStringField("from", from.toString());
        generator.writeStringField("to", to.toString());
        generator.writeEndObject();
    }

    @Override
    public String eventType() {
        return eventType;
    }

    @Override
    public String toString() {
        return "Move{" +
                "id=" + id +
                ", from=" + from +
                ", to=" + to +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Move move = (Move) o;

        return id.equals(move.id) && from.equals(move.from) && to.equals(move.to);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + from.hashCode();
        result = 31 * result + to.hashCode();
        return result;
    }
}
