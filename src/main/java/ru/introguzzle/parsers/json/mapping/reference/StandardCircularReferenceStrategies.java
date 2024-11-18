package ru.introguzzle.parsers.json.mapping.reference;

import lombok.AllArgsConstructor;
import ru.introguzzle.parsers.common.util.Meta;

import java.io.Serial;
import java.io.Serializable;

/**
 * A utility class providing standard strategies for handling circular references during JSON mapping.
 * <p>
 * This class contains predefined implementations of {@link CircularReferenceStrategy} that specify how to handle
 * circular references when serializing objects to JSON. Circular references occur when an object references itself
 * directly or indirectly, which can lead to infinite loops during serialization.
 * </p>
 */
@SuppressWarnings("unused")
public final class StandardCircularReferenceStrategies {

    /**
     * A wrapper class representing a circular reference to an object.
     * <p>
     * Used by the {@link #USE_SPECIAL_OBJECT} strategy to wrap the original object, indicating that a circular
     * reference has been encountered. This allows the serialization process to continue without entering an infinite loop.
     * </p>
     *
     * @param <T> the type of the referent object
     */
    @SuppressWarnings("ALL")
    public static final class CircularReference<T> implements Serializable {
        @Serial
        private static final long serialVersionUID = 1136724425825302768L;

        private final T referent;

        public CircularReference(T referent) {
            if (referent instanceof CircularReference<?>) {
                throw new IllegalArgumentException("Referent cannot be a CircularReference");
            }

            this.referent = referent;
        }

        /**
         * Returns a string representation of the circular reference.
         *
         * @return a string in the format "CircularReference@<hashcode>"
         */
        @Override
        public String toString() {
            return "CircularReference@" + Integer.toHexString(System.identityHashCode(this));
        }

        /**
         * Dereferences the circular reference to obtain the original object.
         *
         * @return the original referent object
         */
        public T dereference() {
            return referent;
        }
    }

    /**
     * A placeholder string used to represent a duplicate reference in the JSON output.
     * <p>
     * This placeholder is used by the {@link #USE_PLACEHOLDER} strategy to indicate that a circular reference
     * has been detected. It replaces the actual object with this string in the serialized JSON.
     * </p>
     */
    public static final String PLACEHOLDER = "<DUPLICATE_REFERENCE>";

    /**
     * A circular reference strategy that throws a {@link CircularReferenceException} when a circular reference is detected.
     * <p>
     * When this strategy is used, encountering a circular reference during serialization will result in an exception,
     * preventing the serialization process from completing. This is useful when circular references are not expected
     * and should be treated as errors.
     * </p>
     */
    public static final CircularReferenceStrategy THROW_EXCEPTION = object -> {
        throw new CircularReferenceException(object);
    };

    /**
     * A circular reference strategy that uses a special object to represent circular references.
     * <p>
     * When this strategy is used, circular references are replaced with instances of {@link CircularReference},
     * which wrap the original object. This allows the serialization process to continue, and the circular reference
     * can be handled or recognized in the output.
     * </p>
     */
    public static final CircularReferenceStrategy USE_SPECIAL_OBJECT = o -> {
        if (o instanceof CircularReference<?>) {
            return o;
        }
        return new CircularReference<>(o);
    };

    /**
     * A circular reference strategy that uses a placeholder string to represent circular references.
     * <p>
     * When this strategy is used, circular references are replaced with the {@link #PLACEHOLDER} string.
     * This allows the serialization process to continue, and the circular reference is indicated by the placeholder
     * in the output.
     * </p>
     */
    public static final CircularReferenceStrategy USE_PLACEHOLDER = _ -> PLACEHOLDER;

    /**
     * Private constructor. Always throws {@code AssertionError}
     */
    private StandardCircularReferenceStrategies() {
        Meta.throwInstantiationError(StandardCircularReferenceStrategies.class);
    }
}
