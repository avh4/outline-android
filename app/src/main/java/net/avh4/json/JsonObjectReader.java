package net.avh4.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.io.SerializedString;

import java.io.IOException;

public class JsonObjectReader {
    private final JsonParser parser;
    private final JsonValueReader valueContext;

    JsonObjectReader(JsonParser parser, JsonValueReader valueContext) {
        this.parser = parser;
        this.valueContext = valueContext;
    }

    private static void throwException(String expected, JsonParser parser) throws IOException {
        throw new IOException("Expected " + expected + ", but got " + parser.getCurrentToken() + " " + parser.getCurrentLocation());
    }

    private void checkField(String fieldName) throws IOException {
        if (!parser.nextFieldName(new SerializedString(fieldName))) {
            throwException("field name " + fieldName, parser);
        }
    }

    public String getString(String fieldName) throws IOException {
        checkField(fieldName);
        return valueContext.getString();
    }

    public <T> T getValue(String fieldName, FromJsonValue<T> fromJsonValue) throws IOException {
        checkField(fieldName);
        return valueContext.getValue(fromJsonValue);
    }
}
