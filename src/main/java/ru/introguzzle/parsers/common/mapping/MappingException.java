package ru.introguzzle.parsers.common.mapping;

import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 * An unchecked exception that indicates a failure during the mapping process.
 * <p>
 * This exception is thrown when an error occurs while mapping data from one form to another,
 * such as during serialization or deserialization processes. It encapsulates various scenarios
 * where mapping operations may fail, providing meaningful messages and causes to aid in debugging.
 * </p>
 *
 * <p>Common scenarios where {@code MappingException} might be thrown include:</p>
 * <ul>
 *     <li>Failure to instantiate a class due to missing or inaccessible constructors.</li>
 *     <li>Issues during type conversion, such as incompatible types.</li>
 *     <li>Invalid constructor arguments specified via annotations.</li>
 *     <li>General reflection-related errors during the mapping process.</li>
 * </ul>
 *
 * <p>This exception is designed to be informative and provide context about the mapping failure,
 * making it easier to identify and resolve issues in the mapping logic.</p>
 */
@NoArgsConstructor
public class MappingException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -688119882777768646L;

    /**
     * Constructs a new mapping exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public MappingException(String message) {
        super(message);
    }

    /**
     * Constructs a new mapping exception with the specified detail message and
     * cause.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public MappingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new mapping exception with the specified cause and a
     * detail message of {@code (cause==null ? null : cause.toString())}
     * (which typically contains the class and detail message of
     * {@code cause}).
     *
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public MappingException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new {@code MappingException} indicating that the specified target class
     * cannot be instantiated from the provided source class.
     *
     * @param target the class that was attempted to be instantiated
     * @param from the source class from which instantiation was attempted
     * @return a new {@code MappingException} with a detailed message
     */
    public static MappingException ofInstantiation(Class<?> target, Class<?> from) {
        return new MappingException("Cannot instantiate " + target.getName() + " from " + from.getName());
    }

    /**
     * Creates a new {@code MappingException} indicating that the specified target class
     * cannot be instantiated from the provided source class, including the underlying cause.
     *
     * @param target the class that was attempted to be instantiated
     * @param from the source class from which instantiation was attempted
     * @param cause the underlying cause of the instantiation failure
     * @return a new {@code MappingException} with a detailed message and cause
     */
    public static MappingException ofInstantiation(Class<?> target, Class<?> from, Throwable cause) {
        return new MappingException("Cannot instantiate " + target.getName() + " from " + from.getName(), cause);
    }

    /**
     * Creates a new {@code MappingException} indicating that the specified target class
     * cannot be instantiated, including the underlying cause.
     *
     * @param target the class that was attempted to be instantiated
     * @param cause the underlying cause of the instantiation failure
     * @return a new {@code MappingException} with a detailed message and cause
     */
    public static MappingException ofInstantiation(Class<?> target, Throwable cause) {
        return new MappingException("Cannot instantiate " + target.getName(), cause);
    }

    /**
     * Creates a new {@code MappingException} indicating that a conversion from one type to another
     * is not possible.
     *
     * @param from the source class type
     * @param to the target class type
     * @return a new {@code MappingException} with a detailed message
     */
    public static MappingException ofConversion(Class<?> from, Class<?> to) {
        String message = String.format("Cannot map %s to %s", from.getName(), to.getName());
        return new MappingException(message);
    }
}
