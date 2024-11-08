package ru.introguzzle.parsers.json.parse;

import java.io.Serial;

public class ParseException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -8502565945302904136L;

    public ParseException() {
        super();
    }

    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
