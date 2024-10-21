package ru.introguzzle.parser.json.mapping.context;

import ru.introguzzle.parser.json.mapping.CircularReferenceException;

public enum CircularReferenceStrategy {
    THROW_EXCEPTION,
    RETURN_NULL,
    USE_PLACEHOLDER;

    public static final String PLACEHOLDER = "<DUPLICATE_REFERENCE>";

    public Object handle(Object object) {
        switch (this) {
            case RETURN_NULL -> {
                return null;
            }

            case THROW_EXCEPTION -> throw new CircularReferenceException(object);
            case USE_PLACEHOLDER -> {
                return PLACEHOLDER;
            }

            default -> throw new AssertionError("Should never happen");
        }
    }
}
