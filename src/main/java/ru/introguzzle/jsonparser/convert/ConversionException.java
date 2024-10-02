package ru.introguzzle.jsonparser.convert;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;

public class ConversionException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 3620219235610957002L;

    public ConversionException() {
        super();
    }

    public ConversionException(@NotNull String message) {
        super(message);
    }

    public ConversionException(@NotNull String message,
                               @NotNull Throwable cause) {
        super(message, cause);
    }
}
