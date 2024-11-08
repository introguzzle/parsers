package ru.introguzzle.parsers.json.mapping;

import lombok.NoArgsConstructor;
import java.io.Serial;

@NoArgsConstructor
public class MappingException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -688119882777768646L;

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
        String message = String.format("Cannot convert %s to %s", from.getSimpleName(), to.getSimpleName());

        return new MappingException(message);
    }
}
