package ru.introguzzle.parsers.common.mapping.deserialization;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.mapping.MappingException;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
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
        Map<String, Type> resolvedTypes = new HashMap<>();
        Map<TypeVariable<?>, Type> typeVariables = createTypeVariables(rawType, type);

        for (Field field : getFieldAccessor().acquire(rawType)) {
            Type resolvedType = resolveTypeVariables(field.getGenericType(), typeVariables);
            resolvedTypes.put(field.getName(), resolvedType);
        }

        return resolvedTypes;
    }

    @Override
    public Class<?> getRawType(Type type) {
        return switch (type) {
            case Class cls -> cls;
            case ArrayType arrayType -> {
                yield arrayType.asArrayType();
            }

            case ParameterizedType parameterizedType -> {
                Type rawType = parameterizedType.getRawType();
                if (rawType instanceof Class<?>) {
                    yield (Class<?>) rawType;
                }

                throw new IllegalArgumentException("Unsupported type: " + rawType);
            }

            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }

    @Override
    public Class<?> getComponentType(@NotNull Type type) {
        return switch (type) {
            case Class cls -> {
                if (cls.isArray()) {
                    yield cls.getComponentType();
                }

                yield null;
            }
            case ArrayType arrayType -> {
                if (arrayType.getComponentType() instanceof Class<?> cls) {
                    yield cls;
                }

                if (arrayType.getComponentType() instanceof ParameterizedType pt) {
                    yield (Class<?>) pt.getRawType();
                }

                if (arrayType.getComponentType() instanceof ArrayType at) {
                    yield getComponentType(at.getComponentType());
                }

                throw new IllegalArgumentException("Unsupported type: " + arrayType);
            }

            case GenericArrayType genericArrayType -> {
                Type component = genericArrayType.getGenericComponentType();
                yield getRawType(component);
            }

            default -> null;
        };
    }

    private Map<TypeVariable<?>, Type> createTypeVariables(Class<?> rawType, Type type) {
        Map<TypeVariable<?>, Type> typeVariables = new HashMap<>();
        if (rawType.getGenericSuperclass() instanceof ParameterizedType superType) {
            typeVariables.putAll(createTypeVariables(getRawType(superType), superType));
        }

        if (type instanceof ParameterizedType parameterizedType) {
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            TypeVariable<?>[] typeParameters = rawType.getTypeParameters();

            if (typeParameters.length != actualTypeArguments.length) {
                throw new IllegalArgumentException("Mismatch between type parameters and actual types");
            }

            for (int i = 0; i < typeParameters.length; i++) {
                typeVariables.put(typeParameters[i], actualTypeArguments[i]);
            }
        }

        return typeVariables;
    }

    private Type resolveTypeVariables(Type type, Map<TypeVariable<?>, Type> actualTypes) {
        return switch (type) {
            // Suppose field is declared like this: T[]
            case GenericArrayType genericArrayType -> {
                // Then genericComponentType (T) must be in actualTypes
                Type genericComponentType = genericArrayType.getGenericComponentType();
                Type actualComponentType = actualTypes.get(genericComponentType);
                yield new ArrayType(actualComponentType != null ? actualComponentType : resolveTypeVariables(genericComponentType, actualTypes));
            }

            case ParameterizedType parameterizedType -> {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                Type[] resolvedTypeArguments = new Type[actualTypeArguments.length];

                for (int i = 0; i < actualTypeArguments.length; i++) {
                    resolvedTypeArguments[i] = resolveTypeVariables(actualTypeArguments[i], actualTypes);
                }

                yield new ResolvedParameterizedType(
                        parameterizedType.getOwnerType(),
                        parameterizedType.getRawType(),
                        resolvedTypeArguments
                );
            }

            case TypeVariable<?> typeVariable -> {
                Type resolvedType = actualTypes.get(typeVariable);
                if (resolvedType == null) {
                    throw new MappingException("Unresolved type variable: " + typeVariable);
                }

                yield resolvedType;
            }

            default -> type;
        };
    }
}
