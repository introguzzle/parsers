package ru.introguzzle.parsers.json.mapping;

import ru.introguzzle.parsers.common.mapping.MappingException;

import java.io.Serial;

public class JSONMappingException extends MappingException {
    @Serial
    private static final long serialVersionUID = 1664705082835965886L;

    public JSONMappingException() {
        super();
    }

    public JSONMappingException(String message, Throwable cause) {
        super(message, cause);
    }

    public JSONMappingException(String message) {
        super(message);
    }

    public JSONMappingException(Class<?> target, Class<?> from) {
        super(target, from);
    }

    public JSONMappingException(Class<?> target) {
        super(target);
    }

    public JSONMappingException(Throwable cause) {
        super(cause);
    }
}
