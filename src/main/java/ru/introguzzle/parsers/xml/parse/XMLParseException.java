package ru.introguzzle.parsers.xml.parse;

import java.io.Serial;

public class XMLParseException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 2963006154134885765L;

    public XMLParseException() {
    }

    public XMLParseException(String message) {
        super(message);
    }

    public XMLParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
