package net.avh4.outline;

import java.io.IOException;
import java.io.Reader;

public interface Filesystem {
    Reader read(String filename) throws IOException;
}
