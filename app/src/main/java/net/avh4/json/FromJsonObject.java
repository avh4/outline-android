package net.avh4.json;

import java.io.IOException;

public interface FromJsonObject<T> {
    T call(JsonObjectReader objectContext) throws IOException;
}
