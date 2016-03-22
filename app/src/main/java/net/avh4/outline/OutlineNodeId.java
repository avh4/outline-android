package net.avh4.outline;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

class OutlineNodeId {
    private final String id;

    OutlineNodeId(String id) {
        this.id = id;
    }

    void toJson(JsonGenerator generator) throws IOException {
        generator.writeString(id);
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
