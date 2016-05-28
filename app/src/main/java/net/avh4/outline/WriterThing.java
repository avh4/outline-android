package net.avh4.outline;

import java.io.IOException;
import java.io.OutputStream;

public interface WriterThing {
    public void write(OutputStream os) throws IOException;
}
