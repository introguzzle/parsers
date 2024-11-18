package ru.introguzzle.parsers.json.mapping.deserialization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeAdapter;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeResolver;
import ru.introguzzle.parsers.common.type.Primitives;
import ru.introguzzle.parsers.common.util.Maps;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.function.TriConsumer;
import ru.introguzzle.parsers.common.mapping.ClassTraverser;
import ru.introguzzle.parsers.common.mapping.MappingException;
import ru.introguzzle.parsers.common.mapping.Traverser;
import ru.introguzzle.parsers.common.util.Nullability;
import ru.introguzzle.parsers.json.entity.JSONArray;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.json.mapping.FieldAccessorImpl;
import ru.introguzzle.parsers.json.mapping.reference.StandardCircularReferenceStrategies.CircularReference;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

abstract class AbstractObjectMapper implements ObjectMapper {
    private static final MethodHandle BIND_TO_HANDLE, UNBIND_HANDLE, UNBIND_ALL_HANDLE;

    static {
        try {
            Class<?> internal = Class.forName("ru.introguzzle.parsers.json.entity.Internal");
            Field f = internal.getDeclaredField("BIND_TO");
            f.setAccessible(true);
            BIND_TO_HANDLE = (MethodHandle) f.get(null);

            f = internal.getDeclaredField("UNBIND");
            f.setAccessible(true);
            UNBIND_HANDLE = (MethodHandle) f.get(null);

            f = internal.getDeclaredField("UNBIND_ALL");
            f.setAccessible(true);
            UNBIND_ALL_HANDLE = (MethodHandle) f.get(null);

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends Collection<Object>>
    Map.Entry<Class<T>, TypeAdapter<T>> newEntryOfIterable(Class<T> type,
                                                           Supplier<? extends T> supplier) {
        return TypeAdapter.newEntry(type, (source, t) -> {
            T collection = supplier.get();
            if (!(t instanceof ParameterizedType pt)) {
                throw new MappingException("Untyped collection is not supported");
            }

            Type genericType = pt.getActualTypeArguments()[0];

            // Actually, instanceof JSONArray
            if (source instanceof Iterable<?> iterable) {
                for (Object object : iterable) {
                    collection.add(getForwardCaller().apply(object, genericType));
                }
            }

            return collection;
        });
    }

    @SuppressWarnings("unchecked")
    private final Map<Class<?>, TypeAdapter<?>> defaultTypeHandlers = Maps.of(TypeHandlers.DEFAULT, Map.ofEntries(
            newEntryOfIterable(List.class, ArrayList::new),
            newEntryOfIterable(Set.class, HashSet::new),
            newEntryOfIterable(Queue.class, LinkedList::new),
            newEntryOfIterable(Deque.class, LinkedList::new),
            TypeAdapter.newEntry(Map.class, (source, type) -> {
                if (!(source instanceof JSONObject object)) {
                    throw new MappingException("Expected JSONObject for Map type, but got: " + source.getClass().getSimpleName());
                }

                // Ensure genericTypes has exactly two elements: key type and value type
                if (!(type instanceof ParameterizedType pt)) {
                    throw new MappingException("Map requires exactly two generic types: key and value");
                }

                Map<Object, Object> map = new HashMap<>();

                Type keyType = pt.getActualTypeArguments()[0];
                Type valueType = pt.getActualTypeArguments()[1];

                for (Map.Entry<String, Object> entry : object.entrySet()) {
                    Object key = keyType == String.class
                            ? entry.getKey()
                            : getForwardCaller().apply(entry.getKey(), keyType);
                    if (key == null) {
                        throw new MappingException("Failed to convert key: " + entry.getKey() + " to type: " + keyType);
                    }

                    // Handle Value Conversion
                    Object value = getForwardCaller().apply(entry.getValue(), valueType);
                    if (value == null && entry.getValue() != null) {
                        throw new MappingException("Failed to convert value for key: " + entry.getKey() + " to type: " + valueType);
                    }

                    map.put(key, value);
                }

                return map;
            })
    ));

    private final Traverser<Class<?>> traverser = new ClassTraverser();
    private final FieldAccessor fieldAccessor = new FieldAccessorImpl();
    private final Map<JSONObject, Object> referenceMap = new IdentityHashMap<>();
    private final Map<Class<?>, TypeAdapter<?>> typeHandlers = new ConcurrentHashMap<>(defaultTypeHandlers);
    private final TypeResolver typeResolver = TypeResolver.newResolver(fieldAccessor);

    @Override
    public @NotNull TypeResolver getTypeResolver() {
        return typeResolver;
    }

    private Class<?> getRawType(Type type) {
        return typeResolver.getRawType(type);
    }

    @Override
    public @NotNull FieldAccessor getFieldAccessor() {
        return fieldAccessor;
    }

    @Override
    public @NotNull ObjectMapper bindTo(@NotNull Class<?> targetType) {
        try {
            BIND_TO_HANDLE.invokeWithArguments(targetType, this);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    @Override
    public @NotNull ObjectMapper unbindAll() {
        try {
            UNBIND_ALL_HANDLE.invokeWithArguments();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    @Override
    public @NotNull ObjectMapper unbind(@NotNull Class<?> targetType) {
        try {
            UNBIND_HANDLE.invokeWithArguments(targetType);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    protected abstract @NotNull String getCircularPlaceholder();
    protected abstract @NotNull BiFunction<Type, Integer, Object> getArraySupplier();

    protected abstract @NotNull TriConsumer<Object, Integer, Object> getArraySetter();

    @Override
    public @NotNull Traverser<Class<?>> getTraverser() {
        return traverser;
    }

    @Override
    public <T> @NotNull ObjectMapper withTypeAdapter(@NotNull Class<T> type, @NotNull TypeAdapter<? extends T> adapter) {
        this.typeHandlers.put(type, adapter);
        return this;
    }

    @Override
    public @NotNull ObjectMapper withTypeAdapters(@NotNull Map<Class<?>, TypeAdapter<?>> adapters) {
        this.typeHandlers.putAll(adapters);
        return this;
    }

    @Override
    public @NotNull ObjectMapper clearTypeAdapters() {
        this.typeHandlers.clear();
        return this;
    }

    @Override
    public @Nullable <T> TypeAdapter<T> findTypeAdapter(@NotNull Class<T> type) {
        return findMostSpecificAdapter(type);
    }

    @SuppressWarnings("unchecked")
    protected <T> TypeAdapter<T> findMostSpecificAdapter(Class<T> type) {
        return (TypeAdapter<T>) getTraverser().findMostSpecificMatch(typeHandlers, type).orElse(null);
    }

    @Override
    public @NotNull BiFunction<Object, Type, Object> getForwardCaller() {
        return this::match;
    }

    @Override
    public @NotNull Object toObject(@NotNull JSONObject object, @NotNull Type type) {
        Object result = map(object, type);
        Nullability.requireNonNull(object, "object");
        Nullability.requireNonNull(type, "type");

        referenceMap.clear();
        assert result != null;
        return result;
    }

    @Override
    public @NotNull Object[] toArray(@NotNull JSONArray array, @NotNull Type type) {
        return (Object[]) handleArray(array, type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E, C extends Collection<E>> @NotNull C toCollection(@NotNull JSONArray array, @NotNull Type type, @NotNull Supplier<C> supplier) {
        C collection = supplier.get();
        for (Object item : array) {
            collection.add((E) getForwardCaller().apply(item, type));
        }

        return collection;
    }

    public @Nullable Object map(@Nullable JSONObject object, @NotNull Type type) {
        if (object == null) {
            return null;
        }

        Object instance = getInstanceSupplier().acquire(object, type);
        referenceMap.put(object, instance);

        Class<?> rawType = getRawType(type);
        List<Field> fields = getFieldAccessor().acquire(rawType);
        Map<String, Type> resolvedTypes = typeResolver.resolveTypes(rawType, type);

        for (Field field : fields) {
            String name = getNameConverter().apply(field);
            Object value = object.get(name);
            Type resolved = resolvedTypes.get(field.getName());

            if (getCircularPlaceholder().equals(value)) {
                value = referenceMap.get(object);
            }

            if (!Modifier.isFinal(field.getModifiers())) {
                if (value instanceof CircularReference<?> reference) {
                    getWritingInvoker().invoke(field, instance, reference.dereference());
                    continue;
                }

                if (value != null) {
                    Object applied = getForwardCaller().apply(value, resolved);
                    getWritingInvoker().invoke(field, instance, applied);
                }
            }
        }

        return instance;
    }

    protected @Nullable Object match(@Nullable Object value, @NotNull Type fieldType) {
        Class<?> raw = getRawType(fieldType);
        return switch (value) {
            case null -> null;
            case JSONObject object -> {
                if (referenceMap.containsKey(object)) {
                    yield referenceMap.get(object);
                }

                TypeAdapter<?> th = findTypeAdapter(raw);
                if (th != null) {
                    yield th.apply(value, fieldType);
                }

                yield map(object, fieldType);
            }
            case JSONArray array -> handleArray(array, fieldType);
            default -> {
                TypeAdapter<?> typeAdapter = findTypeAdapter(raw);
                yield typeAdapter == null
                        ? handlePrimitiveType(value, fieldType)
                        : typeAdapter.apply(value, fieldType);
            }
        };
    }

    protected Object handleArray(JSONArray array, @NotNull Type fieldType) {
        Type componentType = getTypeResolver().getComponentType(fieldType);
        if (componentType == null) {
            return handleCollection(array, fieldType);
        }

        int size = array.size();
        Object resultArray = getArraySupplier().apply(componentType, size);
        for (int i = 0; i < size; i++) {
            Object element = getForwardCaller().apply(array.get(i), componentType);
            getArraySetter().accept(resultArray, i, element);
        }

        return resultArray;
    }

    protected Object handleCollection(JSONArray array, @NotNull Type fieldType) {
        Class<?> raw = getRawType(fieldType);
        TypeAdapter<?> typeAdapter = findTypeAdapter(raw);
        if (typeAdapter != null) {
            return typeAdapter.apply(array, fieldType);
        }

        throw MappingException.ofConversion(raw, JSONArray.class);
    }

    private MappingException newConversionException(Object value, @NotNull Class<?> primitiveType) {
        return MappingException.ofConversion(value.getClass(), primitiveType);
    }

    protected Object handlePrimitiveType(Object value, @NotNull Type fieldType) {
        if (!(fieldType instanceof Class<?> cls)) {
            throw new MappingException(fieldType + " is not a primitive type");
        }

        if (!Primitives.isPrimitive(cls)) {
            throw newConversionException(value, cls);
        }

        return switch (cls.getSimpleName()) {
            case "boolean", "Boolean" -> {
                if (value instanceof Boolean b) yield b;
                if (value instanceof String s) yield Boolean.parseBoolean(s);
                throw newConversionException(value, cls);
            }
            case "byte", "Byte" -> {
                if (value instanceof Number n) yield n.byteValue();
                if (value instanceof String s) yield Byte.parseByte(s);
                throw newConversionException(value, cls);
            }
            case "short", "Short" -> {
                if (value instanceof Number n) yield n.shortValue();
                if (value instanceof String s) yield Short.parseShort(s);
                throw newConversionException(value, cls);
            }
            case "int", "Integer" -> {
                if (value instanceof Number n) yield n.intValue();
                if (value instanceof String s) yield Integer.parseInt(s);
                throw newConversionException(value, cls);
            }
            case "long", "Long" -> {
                if (value instanceof Number n) yield n.longValue();
                if (value instanceof String s) yield Long.parseLong(s);
                throw newConversionException(value, cls);
            }
            case "float", "Float" -> {
                if (value instanceof Number n) yield n.floatValue();
                if (value instanceof String s) yield Float.parseFloat(s);
                throw newConversionException(value, cls);
            }
            case "double", "Double" -> {
                if (value instanceof Number n) yield n.doubleValue();
                if (value instanceof String s) yield Double.parseDouble(s);
                throw newConversionException(value, cls);
            }
            case "char", "Character" -> {
                if (value instanceof String s) yield s.charAt(0);
                throw newConversionException(value, cls);
            }
            case "String" -> {
                if (value instanceof String s) yield s;
                throw newConversionException(value, cls);
            }

            default -> throw new IllegalStateException("Unexpected value: " + cls.getSimpleName());
        };
    }
}