/**
 * This package provides custom annotations to support advanced configuration
 * and parsing mechanisms in the {@code ru.introguzzle.parsers.common} package.
 * <p>
 * The annotations are designed to work seamlessly with reflection-based
 * processing and serialization/deserialization frameworks. These annotations
 * enhance the capabilities of the library by allowing fine-grained control over
 * fields and constructors used in data parsing.
 * </p>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li>{@link ru.introguzzle.parsers.common.annotation.ConstructorArgument} - Marks a constructor argument
 *       that should be used during deserialization.</li>
 *   <li>{@link ru.introguzzle.parsers.common.annotation.ConstructorArguments} - A container annotation for
 *       {@link java.lang.annotation.Repeatable} {@code ConstructorArgument} annotations.</li>
 *   <li>{@link ru.introguzzle.parsers.common.annotation.Excluded} - Specifies fields to be excluded
 *       from serialization or deserialization processes.</li>
 * </ul>
 *
 * @author introguzzle
 * @since 1.0
 */
package ru.introguzzle.parsers.common.annotation;