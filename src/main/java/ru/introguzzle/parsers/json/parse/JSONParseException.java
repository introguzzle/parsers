package ru.introguzzle.parsers.json.parse;

import java.io.Serial;

public class JSONParseException extends ParseException {
    @Serial
    private static final long serialVersionUID = 2663952195860114090L;

    public JSONParseException() {
        super();
    }

    public JSONParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public JSONParseException(String message) {
        super(message);
    }
}
