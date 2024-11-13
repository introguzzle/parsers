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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Optimized version of {@link ReflectionAnnotationInstanceSupplier}
 */
public abstract class CachingAnnotationInstanceSupplier<T, E extends Annotation, F extends Annotation>
        extends AnnotationInstanceSupplier<T, E, F> {
    private static final MethodHandles.Lookup LOOKUP;
    private static final MethodType EMPTY_CONSTRUCTOR_SHAPE;

    static {
        LOOKUP = MethodHandles.publicLookup();
        EMPTY_CONSTRUCTOR_SHAPE = MethodType.methodType(void.class);
    }

    public CachingAnnotationInstanceSupplier(WritingMapper<?> mapper, AnnotationData<E, F> annotationData) {
        super(mapper, annotationData);
    }

    private static MethodType shapeOf(List<Class<?>> argumentTypes) {
        return MethodType.methodType(void.class, argumentTypes);
    }

    private static final Cache<Class<?>, ConstructorWrapper<?>> CONSTRUCTOR_CACHE;
    private static final Cache<Class<?>, ConstructorData<?>> CONSTRUCTOR_DATA_CACHE;

    static {
        CacheSupplier supplier = CacheService.instance();

        CONSTRUCTOR_CACHE = supplier.newCache();
        CONSTRUCTOR_DATA_CACHE = supplier.newCache();
    }

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
                throw new MappingException(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <R> ConstructorWrapper<R> getConstructorWrapper(Class<R> type) {
        return (ConstructorWrapper<R>) CONSTRUCTOR_CACHE.get(type);
    }

    @SuppressWarnings("unchecked")
    private <R> ConstructorData<R> getConstructorData(Class<R> type, E annotation) {
        return (ConstructorData<R>) CONSTRUCTOR_DATA_CACHE.get(type, t -> createConstructorData(t, annotation));
    }

    public abstract Cache<Class<?>, E> getAnnotationCache();

    private <R> E getAnnotation(Class<R> type) {
        return getAnnotationCache().get(type, t -> t.getAnnotation(annotationData.entityAnnotationClass()));
    }

    @Override
    public <R> @NotNull R acquire(@NotNull T object, @NotNull Class<R> type) {
        requireNonNull(object, type);
        E annotation = getAnnotation(type);
        if (annotation == null || retrieveConstructorArguments(annotation).length == 0) {
            ConstructorWrapper<R> cw = getConstructorWrapper(type);

            if (cw == null) {
                MethodHandle constructorHandle;
                try {
                    constructorHandle = LOOKUP.findConstructor(type, EMPTY_CONSTRUCTOR_SHAPE);
                } catch (NoSuchMethodException | IllegalAccessException e) {
                    throw new MappingException(e);
                }

                cw = new ConstructorWrapper<>(constructorHandle, 0);
                CONSTRUCTOR_CACHE.put(type, cw);
            }

            return cw.invoke();
        }

        return getWithArguments(object, type, annotation);
    }

    private <R> R getWithArguments(T object, Class<R> type, E annotation) {
        ConstructorData<R> constructorData = getConstructorData(type, annotation);
        Object[] args = constructorData.fields.stream()
                .map(field -> {
                    String transformed = nameConverter.apply(field);
                    return hook.apply(retrieveValue(object, transformed), field.getType(), genericTypeAccessor.acquire(field));
                })
                .toArray();

        return constructorData.wrapper.invoke(args);
    }

    private <R> ConstructorData<R> createConstructorData(Class<R> type, E annotation) {
        String[] constructorNames = Arrays.stream(retrieveConstructorArguments(annotation))
                .map(ConstructorArgument::value)
                .toArray(String[]::new);

        List<Field> fields = fieldAccessor.acquire(type);

        List<Field> matchedFields = new ArrayList<>();
        List<Class<?>> constructorTypes = new ArrayList<>();

        // O(n^2) matching XD
        for (String name : constructorNames) {
            for (Field field : fields) {
                if (field.getName().equals(name)) {
                    matchedFields.add(field);
                    constructorTypes.add(field.getType());
                }
            }
        }

        if (constructorTypes.size() != constructorNames.length) {
            throw new MappingException("Invalid specified constructor arguments: " + Arrays.toString(constructorNames));
        }

        ConstructorWrapper<R> cw = getConstructorWrapper(type);
        if (cw == null) {
            MethodHandle constructorHandle;
            try {
                MethodType shape = shapeOf(constructorTypes);
                constructorHandle = LOOKUP.findConstructor(type, shape);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new MappingException(e);
            }

            cw = new ConstructorWrapper<>(constructorHandle, constructorTypes.size());
            CONSTRUCTOR_CACHE.put(type, cw);
        }

        return new ConstructorData<>(cw, matchedFields);
    }
}
