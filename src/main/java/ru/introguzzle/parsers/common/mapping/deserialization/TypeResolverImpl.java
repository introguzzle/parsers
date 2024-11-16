package ru.introguzzle.parsers.common.mapping.deserialization;

import lombok.RequiredArgsConstructor;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.mapping.MappingException;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@SuppressWarnings("ALL")
class TypeResolverImpl implements TypeResolver {
    final FieldAccessor fieldAccessor;

    @Override
    public FieldAccessor getFieldAccessor() {
        return fieldAccessor;
    }

    @Override
    public Map<String, Type> resolveTypes(Class<?> rawType, Type type) {
        Map<String, Type> fieldTypeMap = new HashMap<>();
        Map<TypeVariable<?>, Type> typeVariableMap = buildTypeVariableMap(rawType, type);

        for (Field field : getFieldAccessor().acquire(rawType)) {
            Type genericFieldType = field.getGenericType();
            Type resolvedType = resolveTypeVariables(genericFieldType, typeVariableMap);
            fieldTypeMap.put(field.getName(), resolvedType);
        }

        return fieldTypeMap;
    }

    @Override
    public Class<?> getRawType(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?>) {
                return (Class<?>) rawType;
            }
        }

        throw new IllegalArgumentException("Unsupported Type: " + type);
    }

    @Override
    public Class<?> getComponentType(Type type) {
        if (type instanceof GenericArrayType genericArrayType) {
            return getRawType(genericArrayType.getGenericComponentType());
        } else if (type instanceof Class<?> cls && cls.isArray()) {
            return cls.getComponentType();
        } else {
            return null;
        }
    }

    private Map<TypeVariable<?>, Type> buildTypeVariableMap(Class<?> rawType, Type type) {
        Map<TypeVariable<?>, Type> typeVariableMap = new HashMap<>();

        if (type instanceof ParameterizedType parameterizedType) {
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            TypeVariable<?>[] typeParameters = rawType.getTypeParameters();

            if (typeParameters.length != actualTypeArguments.length) {
                throw new IllegalArgumentException("Mismatch between type parameters and actual types");
            }

            for (int i = 0; i < typeParameters.length; i++) {
                typeVariableMap.put(typeParameters[i], actualTypeArguments[i]);
            }
        }

        return typeVariableMap;
    }

    private Type resolveTypeVariables(Type type, Map<TypeVariable<?>, Type> typeVariableMap) {
        if (type instanceof TypeVariable<?> typeVariable) {
            Type resolvedType = typeVariableMap.get(typeVariable);
            if (resolvedType == null) {
                throw new MappingException("Unresolved type variable: " + typeVariable);
            }
            return resolvedType;
        } else if (type instanceof ParameterizedType parameterizedType) {
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            Type[] resolvedTypeArguments = new Type[actualTypeArguments.length];

            for (int i = 0; i < actualTypeArguments.length; i++) {
                resolvedTypeArguments[i] = resolveTypeVariables(actualTypeArguments[i], typeVariableMap);
            }

            return new ResolvedParameterizedType(
                    parameterizedType.getOwnerType(),
                    parameterizedType.getRawType(),
                    resolvedTypeArguments
            );
        } else {
            return type;
        }
    }
}
