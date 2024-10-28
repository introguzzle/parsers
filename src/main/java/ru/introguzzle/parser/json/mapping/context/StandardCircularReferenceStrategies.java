package ru.introguzzle.parser.json.mapping.context;

import lombok.experimental.UtilityClass;
import ru.introguzzle.parser.json.mapping.CircularReferenceException;

@UtilityClass
public final class StandardCircularReferenceStrategies {
    public static final String PLACEHOLDER = "<DUPLICATE_REFERENCE>";
    public static final CircularReferenceStrategy THROW_EXCEPTION = object -> {
        throw new CircularReferenceException(object);
    };

    public static final CircularReferenceStrategy RETURN_NULL = _ -> null;
    public static final CircularReferenceStrategy USE_PLACEHOLDER = _ -> PLACEHOLDER;
}
