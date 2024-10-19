package ru.introguzzle.jsonparser.parse;

public class JSONParseException extends ParseException {
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
