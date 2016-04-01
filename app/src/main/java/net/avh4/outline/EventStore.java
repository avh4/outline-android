package net.avh4.outline;

import android.content.Context;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import net.avh4.Event;
import net.avh4.F1;
import net.avh4.json.FromJsonObject;
import net.avh4.json.FromJsonValue;
import net.avh4.json.JsonObjectReader;
import net.avh4.json.JsonValueReader;
import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;

import java.io.File;
import java.io.IOException;

class EventStore {
    private final Context context;
    private final JsonFactory jsonFactory = new JsonFactory();

    EventStore(Context context) {
        this.context = context;
    }

    void record(Event e) {
        String filename = Long.toString(System.currentTimeMillis());
        File file = new File(context.getFilesDir(), filename + ".json");
        try {
            JsonGenerator generator = jsonFactory.createGenerator(file, JsonEncoding.UTF8);
            generator.writeStartObject();
            generator.writeStringField("type", e.getClass().getCanonicalName());
            generator.writeFieldName("data");
            e.toJson(generator);
            generator.writeEndObject();
            generator.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    void iterate(F1<Event<Outline>> process) throws IOException {
        File[] files = context.getFilesDir().listFiles();
        long lastSeq = Long.MIN_VALUE;
        for (File file : files) {
            try {
                long seq = Long.parseLong(file.getName().replace(".json", ""));
                if (lastSeq >= seq) {
                    throw new IOException("Got files our of order: " + file.getName() + " after " + lastSeq);
                }
                lastSeq = seq;
                Event<Outline> event = parseFile(file);
                process.call(event);
            } catch (IOException e) {
                throw new IOException("Error parsing " + file.getAbsolutePath(), e);
            }
        }
    }

    private Event<Outline> parseFile(File file) throws IOException {
        JsonParser parser = jsonFactory.createParser(file);
        JsonValueReader helper = new JsonValueReader(parser);

        Event<Outline> event = helper.getObject(new FromJsonObject<Event<Outline>>() {
            @Override
            public Event<Outline> call(JsonObjectReader json) throws IOException {
                String type = json.getString("type");

                HashPMap<String, FromJsonValue<? extends Event<Outline>>> typeMap =
                        HashTreePMap.<String, FromJsonValue<? extends Event<Outline>>>empty()
                                .plus("net.avh4.outline.DataStore.Add", Add.fromJson)
                                .plus(Add.class.getCanonicalName(), Add.fromJson)
                                .plus("net.avh4.outline.DataStore.Delete", Delete.fromJson)
                                .plus(Delete.class.getCanonicalName(), Delete.fromJson);

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
