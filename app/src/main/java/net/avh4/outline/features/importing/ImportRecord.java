package net.avh4.outline.features.importing;

import android.support.annotation.NonNull;

public class ImportRecord {
    private final @NonNull String name;
    private final int level;
    private final boolean complete;

    public ImportRecord(@NonNull String name, int level, boolean complete) {
        this.name = name;
        this.level = level;
        this.complete = complete;
    }

    int getLevel() {
        return level;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public boolean isComplete() {
        return complete;
    }
}
