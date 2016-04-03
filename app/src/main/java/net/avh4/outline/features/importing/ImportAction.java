package net.avh4.outline.features.importing;

import net.avh4.net.avh4.csv.CsvObservable;
import net.avh4.outline.*;
import net.avh4.outline.events.Add;
import org.apache.commons.csv.CSVRecord;
import rx.functions.Action1;
import rx.functions.Func1;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class ImportAction implements AppAction {

    private final DataStore dataStore;
    private final Generator<OutlineNodeId> idGenerator;
    private final Filesystem filesystem;
    private final String filename;

    public ImportAction(DataStore dataStore, Generator<OutlineNodeId> idGenerator, Filesystem filesystem, String filename) {
        this.dataStore = dataStore;
        this.idGenerator = idGenerator;
        this.filesystem = filesystem;
        this.filename = filename;
    }

    @Override
    public void run(final OnError e) {
        dataStore.getOutline().first().subscribe(new Action1<Outline>() {
            @Override
            public void call(final Outline outline) {
                final OutlineNodeId importRoot = idGenerator.next();
                final Add addImportRoot = new Add(outline.getRoot().getId(), importRoot, "Import from CSV");

                try {
                    Reader reader = filesystem.read(filename);

                    CsvObservable.createFromReader(reader)
                            .map(new Func1<CSVRecord, ImportRecord>() {
                                @Override
                                public ImportRecord call(CSVRecord fields) {
                                    String name = fields.get(0);
                                    int level = Integer.parseInt(fields.get(3));
                                    boolean complete = Integer.parseInt(fields.get(4)) >= 100;
                                    return new ImportRecord(name, level, complete);
                                }
                            })
                            .flatMap(new CsvImport(importRoot, idGenerator))
                            .toList()
                            .subscribe(new Action1<List<Add>>() {
                                @Override
                                public void call(List<Add> adds) {
                                    dataStore.processEvent(addImportRoot);
                                    for (Add add : adds) {
                                        dataStore.processEvent(add);
                                    }
                                }
                            });
                } catch (IOException err) {
                    e.onError(err);
                }
            }
        });
    }
}
