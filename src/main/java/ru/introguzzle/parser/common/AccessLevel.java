package ru.introguzzle.parser.common;

public final class AccessLevel {
    public static final int EXCLUDE_TRANSIENT = 1 << 1;
    public static final int EXCLUDE_FINAL = 1 << 2;
    public static final int EXCLUDE_STATIC = 1 << 3;
    public static final int EXCLUDE_VOLATILE = 1 << 4;

    public static final int DEFAULT = EXCLUDE_TRANSIENT
            | EXCLUDE_FINAL
            | EXCLUDE_STATIC
            | EXCLUDE_VOLATILE;
}
