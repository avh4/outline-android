package net.avh4.json;

import java.io.IOException;

public interface FromJsonValue<T> {
    T call(JsonValueReader valueContext) throws IOException;
}
