package ru.introguzzle.parsers.common.io;

import java.io.IOException;
import java.io.Serial;

public class WrappedIOException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 4787819002712473929L;

    public WrappedIOException(String message, IOException cause) {
        super(message, cause);
    }

    public WrappedIOException(String message, WrappedIOException cause) {
        super(message, cause);
    }

    public WrappedIOException(IOException cause) {
        super(cause);
    }

    public WrappedIOException(WrappedIOException cause) {
        super(cause);
    }
}
