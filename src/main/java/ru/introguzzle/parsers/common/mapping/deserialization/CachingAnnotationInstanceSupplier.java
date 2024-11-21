package ru.introguzzle.parsers.common.mapping.deserialization;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.cache.CacheService;
import ru.introguzzle.parsers.common.cache.CacheSupplier;
import ru.introguzzle.parsers.common.mapping.AnnotationData;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.common.mapping.MappingException;
import ru.introguzzle.parsers.common.mapping.WritingMapper;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Optimized version of {@link ReflectionAnnotationInstanceSupplier}
 *
 * @param <T> the type of the source object used during instance acquisition (e.g., {@code XMLDocument})
 * @param <E> the type of the entity-level annotation providing metadata for constructor argument mapping
 * @param <F> the type of the field-level annotation
 *
 * @see AnnotationInstanceSupplier
 * @see ReflectionAnnotationInstanceSupplier
 */
public abstract class CachingAnnotationInstanceSupplier<T, E extends Annotation, F extends Annotation>
        extends AnnotationInstanceSupplier<T, E, F> {
    private static final MethodHandles.Lookup LOOKUP;
    private static final MethodType DEFAULT_CONSTRUCTOR_SHAPE;

    static {
        LOOKUP = MethodHandles.publicLookup();
        DEFAULT_CONSTRUCTOR_SHAPE = MethodType.methodType(void.class);
    }

    /**
     * Constructs a new instance of {@code CachingAnnotationInstanceSupplier} with the specified mapper and annotation data.
     *
     * @param mapper         the writing mapper used for accessing field and type information
     * @param annotationData the annotation data containing entity-level and field-level annotation classes
     */
    public CachingAnnotationInstanceSupplier(WritingMapper<?> mapper, AnnotationData<E, F> annotationData) {
        super(mapper, annotationData);
    }

    /**
     * Creates a new {@code MethodType} for constructor with specified {@code argumentTypes}
     * @param argumentTypes constructor argument types
     * @return shape of constructor with specified {@code argumentTypes}
     */
    private static MethodType shapeOf(List<Class<?>> argumentTypes) {
        return MethodType.methodType(void.class, argumentTypes);
    }

    private static final Cache<Type, ConstructorWrapper<?>> CONSTRUCTOR_CACHE;
    private static final Cache<Type, ConstructorData<?>> CONSTRUCTOR_DATA_CACHE;

    static {
        CacheSupplier supplier = CacheService.instance();

        CONSTRUCTOR_CACHE = supplier.newCache();
        CONSTRUCTOR_DATA_CACHE = supplier.newCache();
    }

    /**
     * Wrapper that associate {@code ConstructorWrapper} with matched fields
     * @param <R> type of constructor that belongs to
     */
    @SuppressWarnings("ALL")
    private static final class ConstructorData<R> {
        final ConstructorWrapper<R> wrapper;
        final List<Field> fields;

        ConstructorData(ConstructorWrapper<R> wrapper, List<Field> fields) {
            if (fields.size() != wrapper.argumentCount) {
                throw new MappingException("Constructor argument length mismatch");
            }

            this.wrapper = wrapper;
            this.fields = fields;
        }
    }

    /**
     * Wrapper that associate {@code MethodHandle} of constructor with its count of arguments
     * @param <R> type of constructor that belongs to
     */
    @AllArgsConstructor
    @SuppressWarnings("ALL")
    private static final class ConstructorWrapper<R> {
        final MethodHandle constructorHandle;
        final int argumentCount;

        @SuppressWarnings("unchecked")
        R invoke(Object... arguments) {
            if (argumentCount != arguments.length) {
                throw new MappingException("Constructor argument length mismatch");
            }

            try {
                return (R) constructorHandle.invokeWithArguments(arguments);
            } catch (Throwable e) {
                throw new MappingException("Can't invoke MethodHandle " + constructorHandle, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <R> ConstructorWrapper<R> getConstructorWrapper(Type type) {
        return (ConstructorWrapper<R>) CONSTRUCTOR_CACHE.get(type);
    }

    @SuppressWarnings("unchecked")
    private <R> ConstructorData<R> getConstructorData(Type type, E annotation) {
        return (ConstructorData<R>) CONSTRUCTOR_DATA_CACHE.get(type, t -> createConstructorData(t, annotation));
    }

    public abstract Cache<Class<?>, E> getAnnotationCache();

    private <R> E getAnnotation(Class<R> type) {
        return getAnnotationCache().get(type, t -> t.getAnnotation(annotationData.entityAnnotationClass()));
    }

    @Override
    public <R> @NotNull R acquire(@NotNull T object, @NotNull Type type) {
        requireNonNull(object, type);
        Class<R> rawType = rawType(type);

        E annotation = getAnnotation(rawType);
        if (annotation == null || retrieveConstructorArguments(annotation).length == 0) {
            ConstructorWrapper<R> cw = getConstructorWrapper(rawType);

            if (cw == null) {
                MethodHandle constructorHandle;
                try {
                    constructorHandle = LOOKUP.findConstructor(rawType, DEFAULT_CONSTRUCTOR_SHAPE);
                } catch (NoSuchMethodException e) {
                    throw new MappingException("No default constructor", e);
                } catch (IllegalAccessException e) {
                    throw new MappingException(e);
                }

                cw = new ConstructorWrapper<>(constructorHandle, 0);
                CONSTRUCTOR_CACHE.put(rawType, cw);
            }

            return cw.invoke();
        }

        return getWithArguments(object, type, annotation);
    }

    private <R> R getWithArguments(T object, Type type, E annotation) {
        Class<R> rawType = rawType(type);
        ConstructorData<R> constructorData = getConstructorData(type, annotation);
        Map<String, Type> resolved = mapper.getTypeResolver().resolveTypes(rawType, type);

        Object[] args = constructorData.fields.stream()
                .map(field -> {
                    String transformed = getFieldNameConverter().apply(field);
                    Type fieldType = resolved.get(field.getName());

                    return mapper.getForwardCaller().apply(retrieveValue(object, transformed), fieldType);
                })
                .toArray();

        return constructorData.wrapper.invoke(args);
    }

    private <R> ConstructorData<R> createConstructorData(Type type, E annotation) {
        ConstructorArgument[] arguments = retrieveConstructorArguments(annotation);
        String[] constructorNames = Arrays.stream(arguments)
                .map(ConstructorArgument::value)
                .toArray(String[]::new);

        Class<R> rawType = rawType(type);
        List<Field> fields = mapper.getFieldAccessor().acquire(rawType);

        List<Field> matchedFields = new ArrayList<>();
        List<Class<?>> constructorTypes = new ArrayList<>();

        if (rawType.getGenericSuperclass() instanceof ParameterizedType) {
            // Resolve actual parameter types in case our type is subclass of GenericType
            Map<String, Type> resolvedTypes = mapper.getTypeResolver().resolveTypes(rawType, type);
            for (String name : constructorNames) {
                for (Field field : fields) {
                    String fn = field.getName();
                    if (fn.equals(name)) {
                        matchedFields.add(field);
                        Type resolved = resolvedTypes.get(fn);
                        constructorTypes.add(resolved instanceof Class<?> cls
                                ? cls
                                : field.getType());
                    }
                }
            }
        } else {
            for (String name : constructorNames) {
                for (Field field : fields) {
                    String fn = field.getName();
                    if (fn.equals(name)) {
                        matchedFields.add(field);
                        constructorTypes.add(field.getType());
                    }
                }
            }
        }

        if (constructorTypes.size() != constructorNames.length) {
            for (int i = 0; i < arguments.length; i++) {
                Class<?> ct = arguments[i].type();
                if (ct != void.class) {
                    constructorTypes.add(i, ct);
                    for (Field field : fields) {
                        Class<?> ft = field.getType();
                        if (ft == ct || ft.isAssignableFrom(ct)) {
                            matchedFields.add(i, field);
                        }
                    }
                }
            }

            if (constructorTypes.size() != constructorNames.length) {
                throw new MappingException("Invalid specified constructor arguments: " + Arrays.toString(constructorNames));
            }
        }

        ConstructorWrapper<R> cw = getConstructorWrapper(type);
        if (cw == null) {
            MethodHandle constructorHandle;
            try {
                MethodType shape = shapeOf(constructorTypes);
                constructorHandle = LOOKUP.findConstructor(rawType, shape);
            } catch (NoSuchMethodException e) {
                throw new MappingException("No constructor with such types: " + constructorTypes, e);
            } catch (IllegalAccessException e) {
                throw new MappingException(e);
            }

            cw = new ConstructorWrapper<>(constructorHandle, constructorTypes.size());
            CONSTRUCTOR_CACHE.put(type, cw);
        }

        return new ConstructorData<>(cw, matchedFields);
    }
}
