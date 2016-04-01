package net.avh4.outline;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public class OutlineNodeId {
    private final String id;

    public OutlineNodeId(String id) {
        this.id = id;
    }

    public void toJson(JsonGenerator generator) throws IOException {
        generator.writeString(id);
    }

    boolean isRootNode() {
        return id.equals("");
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OutlineNodeId that = (OutlineNodeId) o;

        return id != null ? id.equals(that.id) : that.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
