package net.avh4.outline.features.importing;

import net.avh4.outline.Generator;
import net.avh4.outline.OutlineNodeId;
import net.avh4.outline.events.Add;
import rx.Observable;
import rx.functions.Func1;

import java.util.HashMap;
import java.util.Map;

public class CsvImport implements Func1<ImportRecord, Observable<Add>> {

    private final Generator<OutlineNodeId> idGenerator;
    private Map<Integer, OutlineNodeId> lastParentAtLevel = new HashMap<>();

    public CsvImport(OutlineNodeId rootId, Generator<OutlineNodeId> idGenerator) {
        this.idGenerator = idGenerator;
        lastParentAtLevel.put(0, rootId);
    }

    @Override
    public Observable<Add> call(ImportRecord record) {
        if (record.isComplete()) {
            return Observable.empty();
        }
        int level = record.getLevel();

        OutlineNodeId parent = lastParentAtLevel.get(level);
        OutlineNodeId itemId = idGenerator.next();
        lastParentAtLevel.put(level + 1, itemId);

        return Observable.just(new Add(parent, itemId, record.getName()));
    }
}
