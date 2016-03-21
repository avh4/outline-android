package net.avh4.outline;

import android.content.Context;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import net.avh4.F1;
import net.avh4.json.JsonHelper;
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
        long lastSeq = Long.MIN_VALUE;
        for (File file : files) {
            try {
                long seq = Long.parseLong(file.getName().replace(".json", ""));
                if (lastSeq >= seq) {
                    throw new IOException("Got files our of order: " + file.getName() + " after " + lastSeq);
                }
                lastSeq = seq;
                DataStore.Event event = parseFile(file);
                process.call(event);
            } catch (IOException e) {
                throw new IOException("Error parsing " + file.getAbsolutePath(), e);
            }
        }
    }

    private DataStore.Event parseFile(File file) throws IOException {
        JsonParser parser = jsonFactory.createParser(file);
        JsonHelper helper = new JsonHelper(parser);

        DataStore.Event event = helper.getObject(new JsonHelper.ObjectCallback<DataStore.Event>() {
            @Override
            public DataStore.Event call(JsonHelper.ObjectContext context) throws IOException {
                String type = context.getString("type");

                HashPMap<String, JsonHelper.ValueCallback<? extends DataStore.Event>> typeMap =
                        HashTreePMap.<String, JsonHelper.ValueCallback<? extends DataStore.Event>>empty()
                                .plus(DataStore.Add.class.getCanonicalName(), DataStore.Add.fromJson)
                                .plus(DataStore.Delete.class.getCanonicalName(), DataStore.Delete.fromJson);

                JsonHelper.ValueCallback<? extends DataStore.Event> fromJson = typeMap.get(type);
                if (fromJson == null) {
                    throw new IOException("Invalid event type: " + type);
                }
                return context.getValue("data", fromJson);
            }
        });

        parser.close();
        return event;
    }
}
