package ru.introguzzle.parsers.common.mapping.deserialization;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Getter
@RequiredArgsConstructor
public class ArrayType implements Type {
    private final Type componentType;

    public Class<?> asArrayType() {
        if (componentType instanceof Class<?>) {
            return Array.newInstance((Class<?>) componentType, 0).getClass();
        }

        if (componentType instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) componentType).getRawType();
            if (rawType instanceof Class<?>) {
                return Array.newInstance((Class<?>) rawType, 0).getClass();
            }
        }

        throw new RuntimeException("Unsupported component type: " + componentType);
    }

    @Override
    public String toString() {
        return "[L" + componentType + ";";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ArrayType other)) return false;
        return componentType.equals(other.componentType);
    }

    @Override
    public int hashCode() {
        return componentType.hashCode();
    }
}
