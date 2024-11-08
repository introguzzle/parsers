package ru.introguzzle.parsers.common.inject;

import java.io.Serial;

@SuppressWarnings("unused")
public class InjectException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 8315952639532650735L;

    public InjectException() {
        super();
    }

    public InjectException(String message) {
        super(message);
    }

    public InjectException(String message, Throwable cause) {
        super(message, cause);
    }
}
