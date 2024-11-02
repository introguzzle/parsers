package ru.introguzzle.parser.json.mapping;

import java.io.Serial;

public class MappingInstantiationException extends MappingException {
    @Serial
    private static final long serialVersionUID = 3693868843845958538L;

    public MappingInstantiationException(String message) {
        super(message);
    }

    public MappingInstantiationException(Throwable cause) {
        super(cause);
    }

    public MappingInstantiationException(Class<?> target) {
        this("Cannot instantiate " + target);
    }

    public MappingInstantiationException(Class<?> target, Class<?> from) {
        this(createMessage(target, from));
    }

    private static String createMessage(Class<?> target, Class<?> from) {
        return "Cannot instantiate " + target + " from " + from;
    }
}
