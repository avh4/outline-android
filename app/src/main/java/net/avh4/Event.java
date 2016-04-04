package net.avh4;

import android.support.annotation.NonNull;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public interface Event<T> {
    @NonNull
    T execute(@NonNull T data);

    void toJson(JsonGenerator generator) throws IOException;

    String eventType();
}
