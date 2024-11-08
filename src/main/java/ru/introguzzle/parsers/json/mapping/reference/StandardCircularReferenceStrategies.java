package ru.introguzzle.parsers.json.mapping.reference;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
@SuppressWarnings("unused")
public final class StandardCircularReferenceStrategies {
    @Getter
    @AllArgsConstructor
    @SuppressWarnings("ALL")
    public static final class CircularReference<T> {
        private final T value;

        @Override
        public String toString() {
            return "CircularReference@" + Integer.toHexString(System.identityHashCode(this));
        }
    }

    public static final String PLACEHOLDER = "<DUPLICATE_REFERENCE>";

    public static final CircularReferenceStrategy THROW_EXCEPTION = object -> {
        throw new CircularReferenceException(object);
    };

    public static final CircularReferenceStrategy USE_SPECIAL_OBJECT = o -> {
        if (o instanceof CircularReference<?>) {
            return o;
        }

        return new CircularReference<>(o);
    };

    public static final CircularReferenceStrategy USE_PLACEHOLDER = _ -> PLACEHOLDER;
}
