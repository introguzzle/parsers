package ru.introguzzle.parser.json.mapping.reference;

import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class StandardCircularReferenceStrategies {
    @Getter
    public static final class CircularReference<T> {
        private final T value;
        private final int hash;

        private CircularReference(T value) {
            this.value = value;
            this.hash = value.hashCode();
        }

        @Override
        public String toString() {
            return "CircularReference@" + System.identityHashCode(value);
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
