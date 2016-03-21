package net.avh4.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.SerializedString;

import java.io.IOException;

public class JsonHelper {
    private final ValueContext valueContext;

    public JsonHelper(JsonParser parser) {
        valueContext = new ValueContext(parser);
    }

    public <T> T getObject(ObjectCallback<T> callback) throws IOException {
        return valueContext.getObject(callback);
    }

    public interface ValueCallback<T> {
        T call(ValueContext valueContext) throws IOException;
    }

    public interface ObjectCallback<T> {
        T call(ObjectContext objectContext) throws IOException;
    }

    public static class ValueContext {
        private final JsonParser parser;
        private final ObjectContext objectContext;

        private ValueContext(JsonParser parser) {
            this.parser = parser;
            objectContext = new ObjectContext(parser, this);
        }

        public String getString() throws IOException {
            if (parser.nextToken() != JsonToken.VALUE_STRING) {
                throw new IOException("Expected string value, but got " + parser.getCurrentToken());
            }
            return parser.getText();
        }

        public int getInt() throws IOException {
            if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) {
                throw new IOException("Expected integer value, but got " + parser.getCurrentToken());
            }
            return parser.getIntValue();
        }

        public <T> T getValue(ValueCallback<T> valueCallback) throws IOException {
            return valueCallback.call(this);
        }

        public <T> T getObject(ObjectCallback<T> callback) throws IOException {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new IOException("Expected JSON object");
            }

            T result = callback.call(objectContext);

            //noinspection StatementWithEmptyBody
            while (parser.nextToken() != JsonToken.END_OBJECT) {
            }

            return result;

        }
    }

    public static class ObjectContext {
        private final JsonParser parser;
        private final ValueContext valueContext;

        private ObjectContext(JsonParser parser, ValueContext valueContext) {
            this.parser = parser;
            this.valueContext = valueContext;
        }

        private void checkField(String fieldName) throws IOException {
            if (!parser.nextFieldName(new SerializedString(fieldName))) {
                throw new IOException("Expected field named " + fieldName);
            }
        }

        public String getString(String fieldName) throws IOException {
            checkField(fieldName);
            return valueContext.getString();
        }

        public <T> T getValue(String fieldName, ValueCallback<T> valueCallback) throws IOException {
            checkField(fieldName);
            return valueContext.getValue(valueCallback);
        }
    }
}
