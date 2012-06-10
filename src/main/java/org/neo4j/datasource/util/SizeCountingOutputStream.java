package org.neo4j.datasource.util;

import java.io.OutputStream;
import java.io.IOException;

public class SizeCountingOutputStream extends OutputStream {
    private long count;

    public void write(final int b) throws IOException {
        count++;
    }

    public long getCount() {
        return count;
    }
}
