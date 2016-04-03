package net.avh4.outline;

import net.avh4.outline.events.Add;
import net.avh4.outline.features.importing.CsvImport;
import net.avh4.outline.features.importing.ImportRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rx.Observer;
import rx.subjects.PublishSubject;

import static org.mockito.Mockito.*;

public class CsvImportTest {

    private static final OutlineNodeId rootId = new OutlineNodeId("ROOT");
    private PublishSubject<ImportRecord> records;
    @Mock private Observer<Add> output;
    @Mock private IdGenerator idGenerator;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(idGenerator.next()).thenReturn(
                new OutlineNodeId("_A"),
                new OutlineNodeId("_B")
        )
                .thenThrow(new RuntimeException("Too many calls"));

        records = PublishSubject.create();
        records.flatMap(new CsvImport(rootId, idGenerator)).subscribe(output);
    }

    @Test
    public void shouldImportRootItem() throws Exception {
        records.onNext(new ImportRecord("By Time", 0, false));
        verify(output).onNext(new Add(rootId, new OutlineNodeId("_A"), "By Time"));
    }

    @Test
    public void shouldImportTopLevelItems() throws Exception {
        records.onNext(new ImportRecord("By Time", 0, false));
        reset(output);
        records.onNext(new ImportRecord("By Priority", 0, false));
        verify(output).onNext(new Add(rootId, new OutlineNodeId("_B"), "By Priority"));
    }

    @Test
    public void shouldImportChildItems() throws Exception {
        records.onNext(new ImportRecord("By Time", 0, false));
        reset(output);
        records.onNext(new ImportRecord("Today", 1, false));
        verify(output).onNext(new Add(new OutlineNodeId("_A"), new OutlineNodeId("_B"), "Today"));
    }
}