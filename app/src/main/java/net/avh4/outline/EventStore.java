package net.avh4.outline;

import android.content.Context;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SerializedString;
import net.avh4.F1;

import java.io.File;
import java.io.IOException;

class EventStore {
    private final Context context;
    private final JsonFactory jsonFactory = new JsonFactory();

    EventStore(Context context) {
        this.context = context;
    }

    void record(DataStore.Event e) {
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

    void iterate(F1<DataStore.Event> process) throws IOException {
        File[] files = context.getFilesDir().listFiles();
        for (File file : files) {
            try {
                DataStore.Event event = parseFile(file);
                process.call(event);
            } catch (IOException e) {
                throw new IOException("Error parsing " + file.getAbsolutePath(), e);
            }
        }
    }

    private DataStore.Event parseFile(File file) throws IOException {
        JsonParser parser = jsonFactory.createParser(file);
        if (parser.nextToken() != JsonToken.START_OBJECT) {
            throw new IOException("Expected JSON object");
        }
        if (!parser.nextFieldName(new SerializedString("type"))) {
            throw new IOException("Expected type field");
        }

        String type = parser.nextTextValue();
        if (!parser.nextFieldName(new SerializedString("data"))) {
            throw new IOException("Expected data field");
        }
        DataStore.Event event;
        if (type.equals(DataStore.Add.class.getCanonicalName())) {
            event = DataStore.Add.fromJson(parser);
        } else if (type.equals(DataStore.Delete.class.getCanonicalName())) {
            event = DataStore.Delete.fromJson(parser);
        } else {
            throw new IOException("Invalid event type: " + type);
        }
        parser.close();
        return event;
    }
}
