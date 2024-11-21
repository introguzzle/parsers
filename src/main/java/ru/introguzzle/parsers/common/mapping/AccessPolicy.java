package ru.introguzzle.parsers.common.mapping;

import ru.introguzzle.parsers.common.util.Meta;

/**
 * The {@code AccessPolicy} class defines a set of bitmask constants used to specify
 * which field modifiers should be excluded during the mapping or deserialization process.
 * These constants are typically used in combination to control the visibility and inclusion
 * of class fields based on their access modifiers.
 *
 * <p>This utility class provides predefined exclusion policies that can be leveraged
 * by mapping frameworks to filter out fields that are marked as {@code transient},
 * {@code final}, {@code static}, or {@code volatile}. By configuring these exclusion
 * policies, developers can fine-tune the mapping behavior to suit their specific needs.</p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Example: Excluding all fields with transient, final, static, or volatile modifiers
 * int exclusionPolicy = AccessPolicy.EXCLUDE_ALL;
 *
 * // Example: Excluding only transient and static fields
 * int customExclusion = AccessPolicy.EXCLUDE_TRANSIENT | AccessPolicy.EXCLUDE_STATIC;
 *
 * // Applying the exclusion policy in a hypothetical mapping framework
 * mapper.setExclusionPolicy(customExclusion);
 * }</pre>
 *
 * <p><strong>Note:</strong> The constants in this class are designed to be used as bitmask flags.
 * They can be combined using bitwise OR operations to create custom exclusion policies.</p>
 *
 * @see java.lang.reflect.Modifier
 * @since 1.0
 */
public final class AccessPolicy {
    /**
     * Excludes fields that are marked as {@code transient}.
     *
     * <p>The {@code transient} modifier indicates that a field should not be serialized.
     * When this exclusion policy is applied, all fields with the {@code transient} modifier
     * will be ignored during the mapping or deserialization process.</p>
     */
    public static final int EXCLUDE_TRANSIENT = 1 << 1;

    /**
     * Excludes fields that are marked as {@code final}.
     *
     * <p>The {@code final} modifier indicates that a field's value cannot be changed once
     * initialized. When this exclusion policy is applied, all fields with the {@code final}
     * modifier will be ignored during the mapping or deserialization process.</p>
     */
    public static final int EXCLUDE_FINAL = 1 << 2;

    /**
     * Excludes fields that are marked as {@code static}.
     *
     * <p>The {@code static} modifier indicates that a field belongs to the class rather than
     * to any specific instance. When this exclusion policy is applied, all fields with the
     * {@code static} modifier will be ignored during the mapping or deserialization process.</p>
     */
    public static final int EXCLUDE_STATIC = 1 << 3;

    /**
     * Excludes fields that are marked as {@code volatile}.
     *
     * <p>The {@code volatile} modifier indicates that a field may be modified asynchronously
     * by multiple threads. When this exclusion policy is applied, all fields with the
     * {@code volatile} modifier will be ignored during the mapping or deserialization process.</p>
     */
    public static final int EXCLUDE_VOLATILE = 1 << 4;

    /**
     * Flag that defines excluding fields that are marked as {@code public} policy.
     */
    public static final int EXCLUDE_PUBLIC = 1 << 5; // 0b00100000

    /**
     * Flag that defines excluding fields that are marked as {@code protected} policy.
     */
    public static final int EXCLUDE_PROTECTED = 1 << 6; // 0b01000000

    /**
     * Flag that defines excluding fields that are marked as {@code private} policy.
     */
    public static final int EXCLUDE_PRIVATE = 1 << 7; // 0b10000000

    /**
     * Flag that defines excluding fields that have default (package-private) access policy
     */
    public static final int EXCLUDE_PACKAGE_PRIVATE = 1 << 8; // 0b0001_0000_0000

    /**
     * Flag that defines excluding synthetic fields policy
     */
    public static final int EXCLUDE_SYNTHETIC = 1 << 9;

    /**
     * Represents the default exclusion policy, which excludes all fields marked as {@code transient},
     * {@code final}, {@code static}, or {@code volatile}, or synthetic.
     */
    public static final int DEFAULT = EXCLUDE_TRANSIENT
            | EXCLUDE_STATIC | EXCLUDE_VOLATILE | EXCLUDE_SYNTHETIC;

    /**
     * Includes all fields regardless of their modifiers.
     *
     * <p>This constant represents an inclusion policy where no fields are excluded based on their modifiers.
     * When applied, it ensures that all fields, regardless of being {@code transient}, {@code final},
     * {@code static}, or {@code volatile}, are included during the mapping or deserialization process.</p>
     */
    public static final int INCLUDE_ALL = 0;

    /**
     * Flag that defines including all fields policy
     */
    public static final int NONE = DEFAULT | EXCLUDE_FINAL
            | EXCLUDE_PUBLIC | EXCLUDE_PROTECTED | EXCLUDE_PRIVATE;

    /**
     * Private constructor. Always throws {@code AssertionError}
     */
    private AccessPolicy() {
        Meta.throwInstantiationError(AccessPolicy.class);
    }
}
