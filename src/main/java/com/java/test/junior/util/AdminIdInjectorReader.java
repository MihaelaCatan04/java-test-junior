package com.java.test.junior.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class AdminIdInjectorReader extends Reader {
    private final BufferedReader reader;
    private final Long adminId;
    private Reader lineBuffer;
    private boolean isHeader = true;

    public AdminIdInjectorReader(Reader source, Long adminId) {
        this.reader = new BufferedReader(source);
        this.adminId = adminId;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int charsRead = -1;

        if (lineBuffer != null) {
            charsRead = lineBuffer.read(cbuf, off, len);
        }

        if (charsRead == -1) {
            String line = reader.readLine();
            if (line == null) return -1;

            String processed = isHeader ? line + ",user_id" : line + "," + adminId;
            isHeader = false;

            lineBuffer = new StringReader(processed + "\n");
            return lineBuffer.read(cbuf, off, len);
        }

        return charsRead;
    }

    @Override
    public void close() throws IOException {
        reader.close();
        if (lineBuffer != null) lineBuffer.close();
    }
}