package ru.introguzzle.parsers.common.field;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class GenericTypeAccessorImpl implements GenericTypeAccessor {
    @Override
    public List<Class<?>> acquire(@NotNull Field field) {
        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType parameterizedType)) {
            return List.of();
        }

        try {
            Stream<Class<?>> stream = Arrays.stream(parameterizedType.getActualTypeArguments())
                    .map(GenericTypeAccessorImpl::acquireClassFromType);
            return stream.toList();
        } catch (Throwable e) {
            return List.of();
        }
    }

    private static Class<?> acquireClassFromType(Type type) {
        return switch (type) {
            case Class<?> cls -> cls;
            case ParameterizedType parameterizedType -> (Class<?>) parameterizedType.getRawType();
            case TypeVariable<?> typeVariable -> acquireClassFromBounds(typeVariable.getBounds());
            case WildcardType wildcard -> acquireClassFromBounds(wildcard.getUpperBounds());
            case null, default -> throw new IllegalArgumentException("Cannot extract class from type: " + type);
        };
    }

    private static Class<?> acquireClassFromBounds(Type[] bounds) {
        if (bounds.length == 0) {
            return Object.class;
        }

        Type bound = bounds[0];
        return switch (bound) {
            case Class<?> cls -> cls;
            case ParameterizedType parameterizedType -> (Class<?>) parameterizedType.getRawType();
            case TypeVariable<?> typeVariable -> acquireClassFromBounds(typeVariable.getBounds());
            case null, default -> Object.class;
        };
    }
}
