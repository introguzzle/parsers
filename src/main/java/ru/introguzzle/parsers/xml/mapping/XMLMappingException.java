package ru.introguzzle.parsers.xml.mapping;

import ru.introguzzle.parsers.common.mapping.MappingException;

import java.io.Serial;

public class XMLMappingException extends MappingException {
    @Serial
    private static final long serialVersionUID = -7338203668748075435L;

    public XMLMappingException() {
        super();
    }

    public XMLMappingException(Class<?> target) {
        super(target);
    }

    public XMLMappingException(String message) {
        super(message);
    }

    public XMLMappingException(String message, Throwable cause) {
        super(message, cause);
    }

    public XMLMappingException(Throwable cause) {
        super(cause);
    }

    public XMLMappingException(Class<?> target, Class<?> from) {
        super(target, from);
    }
}
