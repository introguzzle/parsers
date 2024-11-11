package ru.introguzzle.parsers.common.mapping;

import lombok.NoArgsConstructor;
import java.io.Serial;

@NoArgsConstructor
public class MappingException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -688119882777768646L;

    public MappingException(Class<?> target) {
        this("Cannot instantiate " + target);
    }

    public MappingException(Class<?> target, Class<?> from) {
        this(createMessage(target, from));
    }

    public MappingException(String message) {
        super(message);
    }

    public MappingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MappingException(Throwable cause) {
        super(cause);
    }

    public static MappingException of(Class<?> from, Class<?> to) {
        String message = String.format("Cannot map %s to %s", from, to);

        return new MappingException(message);
    }

    private static String createMessage(Class<?> target, Class<?> from) {
        return "Cannot instantiate " + target + " from " + from;
    }
}
