/**
 * This package contains common classes and utilities for parsing data.
 * It includes submodules for JSON, XML, YAML, and other formats.
 * <p>
 * Main features:
 * - Serialization and deserialization classes
 * - Annotations such as {@link ru.introguzzle.parsers.common.annotation.ConstructorArgument}
 * or {@link ru.introguzzle.parsers.common.annotation.Excluded}
 * - This library configuration support
 * - Cache that prevents {@link java.lang.OutOfMemoryError} exceptions.
 * Note: considering heavy relation on Reflection API and Invoke API,
 * we may need to cache results of such heavy computations, thus it's really
 * important component of this library
 * - {@link java.lang.reflect.Field} utilities
 * - Converters between various data formats support
 * - Throwing analogs of {@link java.util} function interfaces
 * - Method injection classes
 * - IO utilities
 * - Types that are considered as primitive support
 * - And other various utilities
 * </p>
 *
 * @author introguzzle
 * @since 1.0
 */

package ru.introguzzle.parsers.common;