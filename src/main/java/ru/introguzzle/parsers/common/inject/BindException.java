package ru.introguzzle.parsers.common.inject;

import java.io.Serial;

@SuppressWarnings("unused")
public class BindException extends InjectException {
    @Serial
    private static final long serialVersionUID = 4132313175702044533L;

    public BindException() {
        super();
    }

    public BindException(String message) {
        super(message);
    }

    public BindException(String message, Throwable cause) {
        super(message, cause);
    }
}
