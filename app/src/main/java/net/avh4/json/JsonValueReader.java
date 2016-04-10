package net.avh4.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonValueReader {
    private final JsonParser parser;
    private final JsonObjectReader objectContext;

    public JsonValueReader(JsonParser parser) {
        this.parser = parser;
        objectContext = new JsonObjectReader(parser, this);
    }

    private static void throwException(String expected, JsonParser parser) throws IOException {
        throw new IOException("Expected " + expected + ", but got " + parser.getCurrentToken() + " " + parser.getCurrentLocation());
    }

    public String getString() throws IOException {
        if (parser.nextToken() != JsonToken.VALUE_STRING) {
            throwException("string value", parser);
        }
        return parser.getText();
    }

    public int getInt() throws IOException {
        if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) {
            throwException("integer value", parser);
        }
        return parser.getIntValue();
    }

    public long getLong() throws IOException {
        if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) {
            throwException("long value", parser);
        }
        return parser.getLongValue();
    }

    <T> T getValue(FromJsonValue<T> fromJsonValue) throws IOException {
        return fromJsonValue.call(this);
    }

    public <T> List<T> getArray(FromJsonValue<T> fromJsonValue) throws IOException {
        if (parser.nextToken() != JsonToken.START_ARRAY) {
            throwException("JSON array", parser);
        }

        ArrayList<T> result = new ArrayList<>();

        while (true) {
            try {
                T value = fromJsonValue.call(this);
                result.add(value);
            } catch (IOException e) {
                if (parser.getCurrentToken() == JsonToken.END_ARRAY) {
                    return result;
                }
            }
        }
    }

    public <T> T getObject(FromJsonObject<T> fromJsonObject) throws IOException {
        if (parser.nextToken() != JsonToken.START_OBJECT) {
            throwException("JSON object", parser);
        }

        T result = fromJsonObject.call(objectContext);

        //noinspection StatementWithEmptyBody
        while (parser.nextToken() != JsonToken.END_OBJECT) {
        }

        return result;
    }
}
