package net.avh4.net.avh4.csv;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;

import java.io.StringReader;

import static org.mockito.Mockito.verify;

public class CsvObservableTest {
    @Mock
    private Observer<String> observer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldParserCsv() {
        Observable<CSVRecord> subject = CsvObservable.createFromReader(new StringReader("A,B\nC,D"));
        assert subject != null;
        subject.map(new Func1<CSVRecord, String>() {
            @Override
            public String call(CSVRecord strings) {
                return strings.toString();
            }
        }).subscribe(observer);

        verify(observer).onNext("CSVRecord [comment=null, mapping=null, recordNumber=1, values=[A, B]]");
        verify(observer).onNext("CSVRecord [comment=null, mapping=null, recordNumber=2, values=[C, D]]");
        verify(observer).onCompleted();
    }
}
