package net.avh4.outline.android;

import net.avh4.outline.Filesystem;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class AndroidFilesystem implements Filesystem {
    @Override
    public Reader read(final String filename) throws IOException {
        File file = new File(filename);
        return new FileReader(file);
    }
}
