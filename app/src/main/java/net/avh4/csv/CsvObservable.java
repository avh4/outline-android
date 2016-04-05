package net.avh4.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import rx.Observable;

import java.io.IOException;
import java.io.Reader;

public class CsvObservable {
    public static Observable<CSVRecord> createFromReader(Reader reader) {
        try {
            CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT);
            return Observable.from(parser);
        } catch (IOException e) {
            return Observable.error(e);
        }
    }
}
