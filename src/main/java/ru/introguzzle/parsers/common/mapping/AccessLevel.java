package ru.introguzzle.parsers.common.mapping;

/**
 * The {@code AccessLevel} class defines a set of bitmask constants used to specify
 * which field modifiers should be excluded during the mapping or deserialization process.
 * These constants are typically used in combination to control the visibility and inclusion
 * of class fields based on their access modifiers.
 *
 * <p>This utility class provides predefined exclusion levels that can be leveraged
 * by mapping frameworks to filter out fields that are marked as {@code transient},
 * {@code final}, {@code static}, or {@code volatile}. By configuring these exclusion
 * levels, developers can fine-tune the mapping behavior to suit their specific needs.</p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Example: Excluding all fields with transient, final, static, or volatile modifiers
 * int exclusionLevel = AccessLevel.EXCLUDE_ALL;
 *
 * // Example: Excluding only transient and static fields
 * int customExclusion = AccessLevel.EXCLUDE_TRANSIENT | AccessLevel.EXCLUDE_STATIC;
 *
 * // Applying the exclusion level in a hypothetical mapping framework
 * mapper.setExclusionLevel(customExclusion);
 * }</pre>
 *
 * <p><strong>Note:</strong> The constants in this class are designed to be used as bitmask flags.
 * They can be combined using bitwise OR operations to create custom exclusion levels.</p>
 *
 * @see java.lang.reflect.Modifier
 * @since 1.0
 */
public final class AccessLevel {
    /**
     * Excludes fields that are marked as {@code transient}.
     *
     * <p>The {@code transient} modifier indicates that a field should not be serialized.
     * When this exclusion level is applied, all fields with the {@code transient} modifier
     * will be ignored during the mapping or deserialization process.</p>
     */
    public static final int EXCLUDE_TRANSIENT = 1 << 1;

    /**
     * Excludes fields that are marked as {@code final}.
     *
     * <p>The {@code final} modifier indicates that a field's value cannot be changed once
     * initialized. When this exclusion level is applied, all fields with the {@code final}
     * modifier will be ignored during the mapping or deserialization process.</p>
     */
    public static final int EXCLUDE_FINAL = 1 << 2;

    /**
     * Excludes fields that are marked as {@code static}.
     *
     * <p>The {@code static} modifier indicates that a field belongs to the class rather than
     * to any specific instance. When this exclusion level is applied, all fields with the
     * {@code static} modifier will be ignored during the mapping or deserialization process.</p>
     */
    public static final int EXCLUDE_STATIC = 1 << 3;

    /**
     * Excludes fields that are marked as {@code volatile}.
     *
     * <p>The {@code volatile} modifier indicates that a field may be modified asynchronously
     * by multiple threads. When this exclusion level is applied, all fields with the
     * {@code volatile} modifier will be ignored during the mapping or deserialization process.</p>
     */
    public static final int EXCLUDE_VOLATILE = 1 << 4;

    /**
     * Excludes fields that are marked as {@code transient}, {@code final}, {@code static}, or {@code volatile}.
     *
     * <p>This constant represents a combination of all exclusion levels defined in this class.
     * When applied, it ensures that fields with any of the {@code transient}, {@code final},
     * {@code static}, or {@code volatile} modifiers are ignored during the mapping or
     * deserialization process.</p>
     */
    public static final int EXCLUDE_ALL = EXCLUDE_TRANSIENT
            | EXCLUDE_FINAL
            | EXCLUDE_STATIC
            | EXCLUDE_VOLATILE;

    /**
     * Represents the default exclusion level, which excludes all fields marked as {@code transient},
     * {@code final}, {@code static}, or {@code volatile}.
     *
     * <p>This constant is synonymous with {@link #EXCLUDE_ALL} and can be used interchangeably
     * to apply the combined exclusion of all specified field modifiers.</p>
     */
    public static final int DEFAULT = EXCLUDE_ALL;

    /**
     * Private constructor. Always throws {@code AssertionError}
     */
    private AccessLevel() {
        throw new AssertionError("AccessLevel is a utility class and cannot be instantiated");
    }
}
