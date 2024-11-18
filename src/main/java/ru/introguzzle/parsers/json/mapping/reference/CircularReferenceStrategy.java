package ru.introguzzle.parsers.json.mapping.reference;

import org.jetbrains.annotations.NotNull;

/**
 * A functional interface that defines a strategy for handling circular references during JSON serialization or deserialization.
 * <p>
 * Implementations of this interface specify how to handle objects that are detected as part of a circular reference chain.
 * Circular references occur when an object references itself directly or indirectly, which can lead to infinite loops
 * or stack overflows during serialization processes.
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <p>
 * You can implement this interface to define custom behavior when a circular reference is encountered.
 * For example:
 * </p>
 *
 * <pre>{@code
 * CircularReferenceStrategy strategy = object -> {
 *     // Custom logic to handle circular references
 *     return new CustomCircularReferencePlaceholder();
 * };
 * }</pre>
 *
 * <p>
 * Alternatively, you can use predefined strategies such as those provided in {@link StandardCircularReferenceStrategies}.
 * </p>
 *
 * <h2>Method Detail:</h2>
 *
 * <ul>
 *   <li>
 *     {@link #handle(Object)} - Handles the provided object that is part of a circular reference.
 *   </li>
 * </ul>
 *
 * <h2>Notes:</h2>
 * <ul>
 *   <li>This interface is a functional interface and can be used as a lambda expression or method reference.</li>
 *   <li>The {@code handle} method should return an object that will be used in place of the circular reference during serialization.</li>
 *   <li>Implementations should ensure thread safety if the strategy is used in a multithreaded context.</li>
 * </ul>
 *
 * @see StandardCircularReferenceStrategies
 */
@FunctionalInterface
public interface CircularReferenceStrategy {

    /**
     * Handles an object that is part of a circular reference chain.
     * <p>
     * This method is called when a circular reference is detected during serialization or deserialization.
     * The implementation should specify how to handle the circular reference, such as replacing it with a placeholder,
     * throwing an exception, or any other custom logic.
     * </p>
     *
     * @param object the object involved in the circular reference (never {@code null})
     * @return an object to be used in place of the circular reference in the serialization output
     * @throws CircularReferenceException if the strategy decides to throw an exception to indicate the error
     */
    Object handle(@NotNull Object object);
}
