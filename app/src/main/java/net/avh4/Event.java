package net.avh4;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public interface Event<T> {
    T execute(T data);

    void toJson(JsonGenerator generator) throws IOException;
}
