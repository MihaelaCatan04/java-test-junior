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
        int charsRead = readFromBuffer(cbuf, off, len);

        if (charsRead == -1) {
            charsRead = loadNextLine(cbuf, off, len);
        }

        return charsRead;
    }

    private int readFromBuffer(char[] cbuf, int off, int len) throws IOException {
        if (lineBuffer == null) {
            return -1;
        }

        return lineBuffer.read(cbuf, off, len);
    }

    private int loadNextLine(char[] cbuf, int off, int len) throws IOException {
        String line = reader.readLine();
        if (line == null) return -1;

        String processedLine = processLine(line);

        lineBuffer = new StringReader(processedLine + "\n");

        return lineBuffer.read(cbuf, off, len);
    }

    private String processLine(String line) {
        String processed = isHeader
                ? line + ",user_id"
                : line + "," + adminId;

        isHeader = false;
        return processed;
    }

    @Override
    public void close() throws IOException {
        reader.close();
        if (lineBuffer != null) lineBuffer.close();
    }
}