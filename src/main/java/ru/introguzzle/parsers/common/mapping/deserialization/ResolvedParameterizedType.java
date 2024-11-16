package ru.introguzzle.parsers.common.mapping.deserialization;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

/**
 * Implementation of the {@link ParameterizedType},
 * representing a parameterized type with resolved type arguments.
 */
class ResolvedParameterizedType implements ParameterizedType {

    private final Type ownerType;
    private final Type rawType;
    private final Type[] typeArguments;

    /**
     * Constructs a new {@code ResolvedParameterizedType} with the specified owner type,
     * raw type, and type arguments.
     *
     * @param ownerType     the owner type of this parameterized type, or {@code null} if none
     * @param rawType       the raw type representing the class or interface declaring this type
     * @param typeArguments the actual type arguments, must not be {@code null}
     */
    public ResolvedParameterizedType(Type ownerType, Type rawType, Type[] typeArguments) {
        this.ownerType = ownerType;
        this.rawType = Objects.requireNonNull(rawType, "rawType must not be null");
        this.typeArguments = typeArguments.clone();
    }

    /**
     * Returns an array of the actual type arguments for this type.
     *
     * @return an array of {@link Type} objects representing the actual type arguments
     */
    @Override
    public Type[] getActualTypeArguments() {
        return typeArguments.clone();
    }

    /**
     * Returns the raw type representing the class or interface declaring this type.
     *
     * @return the raw type
     */
    @Override
    public Type getRawType() {
        return rawType;
    }

    /**
     * Returns the owner type of this parameterized type.
     *
     * @return the owner type, or {@code null} if none
     */
    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Two {@code ResolvedParameterizedType} instances are equal if their owner types,
     * raw types, and actual type arguments are equal.
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is equal to the obj argument; {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ParameterizedType other)) {
            return false;
        }
        return Objects.equals(ownerType, other.getOwnerType()) &&
                Objects.equals(rawType, other.getRawType()) &&
                Arrays.equals(typeArguments, other.getActualTypeArguments());
    }

    /**
     * Returns a hash code value for this object.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(typeArguments) ^
                Objects.hashCode(ownerType) ^
                Objects.hashCode(rawType);
    }

    /**
     * Returns a string representation of this parameterized type.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (ownerType != null) {
            sb.append(ownerType.getTypeName()).append(".");
        }
        sb.append(rawType.getTypeName());
        if (typeArguments != null && typeArguments.length > 0) {
            sb.append("<");
            boolean first = true;
            for (Type t : typeArguments) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(t.getTypeName());
                first = false;
            }
            sb.append(">");
        }
        return sb.toString();
    }
}
