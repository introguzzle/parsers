package ru.introguzzle.parser.json.mapping;

import java.io.Serial;

public class MappingException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -688119882777768646L;

    public MappingException() {
        super();
    }

    public MappingException(String message) {
        super(message);
    }

    public MappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
