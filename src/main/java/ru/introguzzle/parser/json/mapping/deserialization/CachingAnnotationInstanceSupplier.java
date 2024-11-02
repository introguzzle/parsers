package ru.introguzzle.parser.json.mapping.deserialization;

import lombok.AllArgsConstructor;
import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.json.entity.annotation.JSONEntity;
import ru.introguzzle.parser.json.mapping.Fields;
import ru.introguzzle.parser.json.mapping.MappingException;
import ru.introguzzle.parser.json.mapping.MappingInstantiationException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * Optimized version of {@link AnnotationInstanceSupplier}
 */
public class CachingAnnotationInstanceSupplier implements InstanceSupplier {
    private static final MethodType EMPTY_CONSTRUCTOR_SHAPE;
    static {
        EMPTY_CONSTRUCTOR_SHAPE = MethodType.methodType(void.class);
    }

    private static MethodType shapeOf(List<Class<?>> argumentTypes) {
        return MethodType.methodType(void.class, argumentTypes);
    }

    private final MethodHandles.Lookup lookup = MethodHandles.publicLookup();
    private final Map<Class<?>, ConstructorWrapper<?>> constructorCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, ConstructorData<?>> constructorDataCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, JSONEntity> annotationCache = new ConcurrentHashMap<>();

    /**
     * Reference to parent mapper
     */
    private final JSONToObjectMapper mapper;

    /**
     * Method reference for forwarding recursive calls
     */
    private final BiFunction<Object, Class<?>, Object> hook;

    public CachingAnnotationInstanceSupplier(JSONToObjectMapper m) {
        mapper = m;
        hook = m.forwardCaller();
    }

    @SuppressWarnings("ALL")
    private static final class ConstructorData<T> {
        final ConstructorWrapper<T> wrapper;
        final List<Field> fields;

        ConstructorData(ConstructorWrapper<T> wrapper, List<Field> fields) {
            if (fields.size() != wrapper.argumentCount) {
                throw new MappingInstantiationException("Constructor argument length mismatch");
            }

            this.wrapper = wrapper;
            this.fields = fields;
        }
    }

    @AllArgsConstructor
    @SuppressWarnings("ALL")
    private static final class ConstructorWrapper<T> {
        final MethodHandle constructorHandle;
        final int argumentCount;

        @SuppressWarnings("unchecked")
        T invoke(Object... arguments) {
            if (argumentCount != arguments.length) {
                throw new MappingInstantiationException("Constructor argument length mismatch");
            }

            try {
                return (T) constructorHandle.invokeWithArguments(arguments);
            } catch (Throwable e) {
                throw new MappingInstantiationException(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> ConstructorWrapper<T> getConstructorWrapper(Class<T> type) {
        return (ConstructorWrapper<T>) constructorCache.get(type);
    }

    @SuppressWarnings("unchecked")
    private <T> ConstructorData<T> getConstructorData(Class<T> type, JSONEntity annotation) {
        return (ConstructorData<T>) constructorDataCache.computeIfAbsent(type, t -> createConstructorData(t, annotation));
    }

    private <T> JSONEntity getAnnotation(Class<T> type) {
        return annotationCache.computeIfAbsent(type, t -> t.getAnnotation(JSONEntity.class));
    }

    @Override
    public <T> T get(JSONObject object, Class<T> type) {
        JSONEntity annotation = getAnnotation(type);
        if (annotation == null || annotation.constructorArgs().length == 0) {
            ConstructorWrapper<T> cw = getConstructorWrapper(type);
            if (cw == null) {
                MethodHandle constructorHandle;
                try {
                    constructorHandle = lookup.findConstructor(type, EMPTY_CONSTRUCTOR_SHAPE);
                } catch (NoSuchMethodException | IllegalAccessException e) {
                    throw new MappingInstantiationException(e);
                }

                cw = new ConstructorWrapper<>(constructorHandle, 0);
                constructorCache.put(type, cw);
            }

            return cw.invoke();
        }

        return getWithArguments(type, object, annotation);
    }

    private <T> T getWithArguments(Class<T> type, JSONObject object, JSONEntity annotation) {
        ConstructorData<T> constructorData = getConstructorData(type, annotation);
        Object[] args = constructorData.fields.stream()
                .map(field -> {
                    String transformed = mapper.getNameConverter().convert(field);
                    return hook.apply(object.get(transformed), field.getType());
                })
                .toArray();

        return constructorData.wrapper.invoke(args);
    }

    private <T> ConstructorData<T> createConstructorData(Class<T> type, JSONEntity annotation) {
        String[] constructorNames = annotation.constructorArgs();
        List<Field> fields = Fields.getCached(type);

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

        ConstructorWrapper<T> cw = getConstructorWrapper(type);
        if (cw == null) {
            MethodHandle constructorHandle;
            try {
                MethodType shape = shapeOf(constructorTypes);
                constructorHandle = lookup.findConstructor(type, shape);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new MappingInstantiationException(e);
            }

            cw = new ConstructorWrapper<>(constructorHandle, constructorTypes.size());
            constructorCache.put(type, cw);
        }

        return new ConstructorData<>(cw, matchedFields);
    }
}
