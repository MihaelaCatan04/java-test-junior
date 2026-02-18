package com.java.test.junior.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AdminIdInjectorStream extends InputStream {

    private final InputStream original;
    private final byte[] toInject;
    private int injectIndex = -1;
    private int nextByte = -1;

    public AdminIdInjectorStream(InputStream original, Long adminId) {
        this.original = original;
        this.toInject = ("," + adminId).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public int read() throws IOException {
        while (true) {
            if (injectIndex >= 0) {
                int b = toInject[injectIndex++] & 0xFF;
                if (injectIndex >= toInject.length) {
                    injectIndex = -1;
                }
                return b;
            }
            if (nextByte != -1) {
                int b = nextByte;
                nextByte = -1;
                return b;
            }
            int b = original.read();
            if (b == -1) return -1;
            if (b == '\r') continue;
            if (b == '\n') {
                injectIndex = 0;
                nextByte = '\n';
                continue;
            }
            return b;
        }
    }
}
