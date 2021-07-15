package ru.zsc.util;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Created by Pavel Perepech
 */
public class LineSupplierReader extends Reader {

    private Supplier<String> source;

    public LineSupplierReader(Supplier<String> source) {
        super();
        this.source = source;
        buffer = new StringBuilder();
    }

    public LineSupplierReader(Object lock, Supplier<String> source) {
        super(lock);
        this.source = source;
        buffer = new StringBuilder();
    }

    private StringBuilder buffer;

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        synchronized (lock) {
            if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                    ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }

            while (buffer.length() < len) {
                final String line = source.get();

                if (Objects.nonNull(line)) {
                    buffer.append(line).append("\n");
                } else {
                    if (buffer.length() == 0) {
                        return -1;
                    }

                    break;
                }
            }

            final int readed = buffer.length() > len ? len: buffer.length();
            buffer.toString().getChars(0, readed, cbuf, off);
            buffer.delete(0, readed);

            return readed;
        }
    }

    @Override
    public void close() throws IOException {
         buffer = null;
    }
}
