package ru.zsc.util;

import java.io.IOException;
import java.io.Writer;
import java.util.function.Consumer;

/**
 * Created by Pavel Perepech.
 */
public class LineConsumerWriter extends Writer {

    private Consumer<String> target;
    private StringBuilder buffer;

    public LineConsumerWriter(Consumer<String> target) {
        super();
        init(target);
    }

    public LineConsumerWriter(Object lock, Consumer<String> target) {
        super(lock);
        init(target);
    }

    private void init(Consumer<String> target) {
        this.target = target;
        buffer = new StringBuilder();
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        buffer.append(cbuf, off, len);
        flushBuffer(true);
    }

    @Override
    public void flush() throws IOException {
        flushBuffer(false);
    }

    @Override
    public void close() throws IOException {
        buffer = null;
    }

    private void flushBuffer(final boolean requireCrlf) {
        int idx = buffer.indexOf("\n");
        int start = 0;
        while (idx >= 0) {
            target.accept(buffer.substring(start, idx));
            start = idx + 1;

            if (start < buffer.length()) {
                idx = buffer.indexOf("\n", start);
            } else {
                idx = -1;
            }
        }
        buffer.delete(0, Math.min(start - 1, buffer.length()));

        if (!requireCrlf && buffer.length() > 0) {
            target.accept(buffer.toString());
            buffer.delete(0, buffer.length());
        }
    }
}
