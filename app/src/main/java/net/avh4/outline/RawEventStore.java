package net.avh4.outline;

import android.content.Context;
import net.avh4.Action1E;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by avh4 on 5/28/16.
 */
public class RawEventStore {
    private File eventStoreRoot;

    RawEventStore(Context context) {
        eventStoreRoot = new File(context.getFilesDir(), "eventStore");
        if (!eventStoreRoot.exists()) {
            boolean result = eventStoreRoot.mkdir();
            if (!result) {
                throw new RuntimeException("Unable to initialize data directory: " + eventStoreRoot.getAbsolutePath());
            }
        }
    }


    public void write(String filename, WriterThing writerThing) throws IOException {
        File file = new File(eventStoreRoot, filename + ".json");
        if (file.exists()) {
            throw new RuntimeException("event already exists!! " + file);
        }
        FileOutputStream os = new FileOutputStream(file);
        writerThing.write(os);
        os.close();
    }

    public void iterate(Action1E<InputStream, IOException> process) throws IOException {
        File[] files = eventStoreRoot.listFiles();
        long lastSeq = Long.MIN_VALUE;
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
        for (File file : files) {
            try {
                long seq = Long.parseLong(file.getName().replace(".json", ""));
                if (lastSeq >= seq) {
                    throw new IOException("Got files our of order: " + file.getName() + " after " + lastSeq);
                }
                lastSeq = seq;
                FileInputStream is = new FileInputStream(file);
                process.process(is);
                is.close();
            } catch (Exception e) {
                throw new IOException("Error parsing " + file.getAbsolutePath(), e);
            }
        }
    }
}
