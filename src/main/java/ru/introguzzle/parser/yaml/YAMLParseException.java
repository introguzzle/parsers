package ru.introguzzle.parser.yaml;

import java.io.Serial;

public class YAMLParseException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -6140465365176445071L;

    public YAMLParseException() {
        super();
    }

    public YAMLParseException(String message) {
        super(message);
    }

    public YAMLParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
