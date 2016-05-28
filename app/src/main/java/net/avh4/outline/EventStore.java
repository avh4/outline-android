package net.avh4.outline;

import android.content.Context;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import net.avh4.Action1E;
import net.avh4.Event;
import net.avh4.ISO8601;
import net.avh4.UniqueClock;
import net.avh4.json.FromJsonObject;
import net.avh4.json.FromJsonValue;
import net.avh4.json.JsonObjectReader;
import net.avh4.json.JsonValueReader;
import net.avh4.outline.events.*;
import net.avh4.outline.events.legacy.LegacyCompleteItem1;
import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;
import rx.functions.Action1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class EventStore {
    private final JsonFactory jsonFactory = new JsonFactory();
    private final UniqueClock uniqueClock = new UniqueClock();
    private RawEventStore rawEventStore;

    EventStore(Context context) {
        rawEventStore = new RawEventStore(context);
    }

    void record(final Event e) {
        String filename = Long.toString(uniqueClock.get());
        try {
            rawEventStore.write(filename, new WriterThing() {
                public void write(OutputStream os) throws IOException {
                    JsonGenerator generator = jsonFactory.createGenerator(os, JsonEncoding.UTF8);
                    generator.writeStartObject();
                    generator.writeStringField("at", ISO8601.format(new Date()));
                    generator.writeStringField("type", e.eventType());
                    generator.writeFieldName("data");
                    e.toJson(generator);
                    generator.writeEndObject();
                    generator.close();
                }
            });
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    void iterate(final Action1<Event<Outline>> process) throws IOException {
        rawEventStore.iterate(new Action1E<InputStream, IOException>() {
            @Override
            public void process(InputStream inputStream) throws IOException {
                Event<Outline> event = parseFile(inputStream);
                process.call(event);
            }
        });
    }

    private Event<Outline> parseFile(InputStream file) throws IOException {
        JsonParser parser = jsonFactory.createParser(file);
        JsonValueReader helper = new JsonValueReader(parser);

        Event<Outline> event = helper.getObject(new FromJsonObject<Event<Outline>>() {
            @Override
            public Event<Outline> call(JsonObjectReader json) throws IOException {
                json.getString("at");
                String type = json.getString("type");

                HashPMap<String, FromJsonValue<? extends Event<Outline>>> typeMap =
                        HashTreePMap.<String, FromJsonValue<? extends Event<Outline>>>empty()
                                .plus("net.avh4.outline.DataStore.Add", Add.fromJson)
                                .plus("net.avh4.outline.Add", Add.fromJson)
                                .plus(Add.eventType, Add.fromJson)
                                .plus("net.avh4.outline.DataStore.Delete", Delete.fromJson)
                                .plus("net.avh4.outline.Delete", Delete.fromJson)
                                .plus(Delete.eventType, Delete.fromJson)
                                .plus(LegacyCompleteItem1.eventType, LegacyCompleteItem1.fromJson)
                                .plus(CompleteItem.eventType, CompleteItem.fromJson)
                                .plus(UncompleteItem.eventType, UncompleteItem.fromJson)
                                .plus(Move.eventType, Move.fromJson)
                                .plus(Reorder.eventType, Reorder.fromJson);

                FromJsonValue<? extends Event<Outline>> fromJson = typeMap.get(type);
                if (fromJson == null) {
                    throw new IOException("Invalid event type: " + type);
                }
                return json.getValue("data", fromJson);
            }
        });

        parser.close();
        return event;
    }
}
