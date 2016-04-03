package net.avh4.outline.domain;

import net.avh4.outline.Filesystem;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import java.io.Reader;
import java.io.StringReader;

public class FakeFilesystem implements Filesystem {
    private PMap<String, String> contents = HashTreePMap.empty();

    public void link(String filename, String fileContents) {
        contents = contents.plus(filename, fileContents);
    }

    @Override
    public Reader read(String filename) {
        return new StringReader(contents.get(filename));
    }
}
