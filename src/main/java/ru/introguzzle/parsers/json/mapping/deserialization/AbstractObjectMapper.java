package ru.introguzzle.parsers.json.mapping.deserialization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parsers.common.field.GenericTypeAccessor;
import ru.introguzzle.parsers.common.field.GenericTypeAccessorImpl;
import ru.introguzzle.parsers.common.function.TriFunction;
import ru.introguzzle.parsers.common.util.Maps;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.function.TriConsumer;
import ru.introguzzle.parsers.common.mapping.ClassTraverser;
import ru.introguzzle.parsers.common.mapping.MappingException;
import ru.introguzzle.parsers.common.mapping.Traverser;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeHandler;
import ru.introguzzle.parsers.json.entity.JSONArray;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.json.mapping.FieldAccessorImpl;
import ru.introguzzle.parsers.json.mapping.reference.StandardCircularReferenceStrategies.CircularReference;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

abstract class AbstractObjectMapper implements ObjectMapper {
    private <T extends Collection<Object>>
    Map.Entry<Class<T>, TypeHandler<T>> newEntryOfIterable(Class<T> type,
                                                           Supplier<? extends T> supplier) {
        return TypeHandler.newEntry(type, (source, genericTypes) -> {
            T collection = supplier.get();
            if (genericTypes.isEmpty()) {
                throw new MappingException("Untyped collection is not supported");
            }

            Class<?> genericType = genericTypes.getFirst();

            // Actually, instanceof JSONArray
            if (source instanceof Iterable<?> iterable) {
                for (Object object : iterable) {
                    collection.add(getForwardCaller().apply(object, genericType, List.of()));
                }
            }

            return collection;
        });
    }

    @SuppressWarnings("unchecked")
    private final Map<Class<?>, TypeHandler<?>> defaultTypeHandlers = Maps.of(TypeHandlers.DEFAULT, Map.ofEntries(
            newEntryOfIterable(List.class, ArrayList::new),
            newEntryOfIterable(Set.class, HashSet::new),
            newEntryOfIterable(Queue.class, LinkedList::new),
            newEntryOfIterable(Deque.class, LinkedList::new),
            TypeHandler.newEntry(Map.class, (source, genericTypes) -> {
                if (!(source instanceof JSONObject object)) {
                    throw new MappingException("Expected JSONObject for Map type, but got: " + source.getClass().getSimpleName());
                }

                // Ensure genericTypes has exactly two elements: key type and value type
                if (genericTypes.size() != 2) {
                    throw new MappingException("Map requires exactly two generic types: key and value");
                }

                Map<Object, Object> map = new HashMap<>();

                Class<?> keyType = genericTypes.get(0);
                Class<?> valueType = genericTypes.get(1);

                for (Map.Entry<String, Object> entry : object.entrySet()) {
                    Object key = keyType == String.class
                            ? entry.getKey()
                            : getForwardCaller().apply(entry.getKey(), keyType, List.of());
                    if (key == null) {
                        throw new MappingException("Failed to convert key: " + entry.getKey() + " to type: " + keyType.getName());
                    }

                    // Handle Value Conversion
                    Object value = getForwardCaller().apply(entry.getValue(), valueType, List.of());
                    if (value == null && entry.getValue() != null) {
                        throw new MappingException("Failed to convert value for key: " + entry.getKey() + " to type: " + valueType.getName());
                    }

                    map.put(key, value);
                }

                return map;
            })
    ));

    private final Traverser<Class<?>> traverser = new ClassTraverser();
    private final FieldAccessor fieldAccessor = new FieldAccessorImpl();
    private final GenericTypeAccessor genericTypeAccessor = new GenericTypeAccessorImpl();
    private final Map<JSONObject, Object> referenceMap = new IdentityHashMap<>();
    private final Map<Class<?>, TypeHandler<?>> typeHandlers = new ConcurrentHashMap<>(defaultTypeHandlers);

    @Override
    public @NotNull FieldAccessor getFieldAccessor() {
        return fieldAccessor;
    }

    @Override
    public @NotNull GenericTypeAccessor getGenericTypeAccessor() {
        return genericTypeAccessor;
    }

    @Override
    public @NotNull ObjectMapper bindTo(@NotNull Class<?> targetType) {
        JSONObject.bindTo(targetType, this);
        return this;
    }

    @Override
    public @NotNull ObjectMapper unbindAll() {
        JSONObject.unbindAll();
        return this;
    }

    @Override
    public @NotNull ObjectMapper unbind(@NotNull Class<?> targetType) {
        JSONObject.unbind(targetType);
        return this;
    }

    protected abstract @NotNull String getCircularPlaceholder();
    protected abstract @NotNull BiFunction<Class<?>, Integer, Object> getArraySupplier();

    protected abstract @NotNull TriConsumer<Object, Integer, Object> getArraySetter();

    @Override
    public @NotNull Traverser<Class<?>> getTraverser() {
        return traverser;
    }

    @Override
    public <T> @NotNull ObjectMapper withTypeHandler(@NotNull Class<T> type, @NotNull TypeHandler<? extends T> handler) {
        this.typeHandlers.put(type, handler);
        return this;
    }

    @Override
    public @NotNull ObjectMapper withTypeHandlers(@NotNull Map<Class<?>, TypeHandler<?>> handlers) {
        this.typeHandlers.putAll(handlers);
        return this;
    }

    @Override
    public @NotNull ObjectMapper clearTypeHandlers() {
        this.typeHandlers.clear();
        return this;
    }

    @Override
    public @Nullable <T> TypeHandler<T> getTypeHandler(@NotNull Class<T> type) {
        return findMostSpecificHandler(type);
    }

    @SuppressWarnings("unchecked")
    protected <T> TypeHandler<T> findMostSpecificHandler(Class<T> type) {
        return (TypeHandler<T>) getTraverser().findMostSpecificMatch(typeHandlers, type).orElse(null);
    }

    @Override
    public @NotNull TriFunction<Object, Class<?>, List<Class<?>>, Object> getForwardCaller() {
        return this::match;
    }

    @Override
    public <T> @NotNull T toObject(@NotNull JSONObject object, @NotNull Class<T> type) {
        T result = map(object, type);
        Objects.requireNonNull(object);
        Objects.requireNonNull(type);
        referenceMap.clear();
        Objects.requireNonNull(result);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @NotNull T[] toArray(@NotNull JSONArray array, @NotNull Class<T[]> type) {
        return (T[]) handleArray(array, type, List.of());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, C extends Collection<T>> @NotNull C toCollection(@NotNull JSONArray array, @NotNull Class<T> type, @NotNull Supplier<C> supplier) {
        C collection = supplier.get();
        for (Object item : array) {
            collection.add((T) getForwardCaller().apply(item, type, List.of()));
        }

        return collection;
    }

    public <T> @Nullable T map(@Nullable JSONObject object, @NotNull Class<T> type) {
        if (object == null) {
            return null;
        }

        T instance = getInstanceSupplier().acquire(object, type);
        referenceMap.put(object, instance);

        List<Field> fields = getFieldAccessor().acquire(type);
        for (Field field : fields) {
            String name = getNameConverter().apply(field);
            Object value = object.get(name);

            List<Class<?>> genericTypes = getGenericTypeAccessor().acquire(field);
            if (getCircularPlaceholder().equals(value)) {
                value = referenceMap.get(object);
            }

            if (!Modifier.isFinal(field.getModifiers())) {
                if (value instanceof CircularReference<?> reference) {
                    getWritingInvoker().invoke(field, instance, reference.getValue());
                    continue;
                }

                if (value != null) {
                    Object applied = getForwardCaller().apply(value, field.getType(), genericTypes);
                    getWritingInvoker().invoke(field, instance, applied);
                }
            }
        }

        return instance;
    }

    protected @Nullable Object match(@Nullable Object value, @NotNull Class<?> fieldType, @NotNull List<Class<?>> genericTypes) {
        return switch (value) {
            case null -> null;
            case JSONObject object -> {
                if (referenceMap.containsKey(object)) {
                    yield referenceMap.get(object);
                }

                TypeHandler<?> th = getTypeHandler(fieldType);
                if (th != null) {
                    yield th.apply(value, genericTypes);
                }

                yield map(object, fieldType);
            }
            case JSONArray array -> handleArray(array, fieldType, genericTypes);
            default -> {
                TypeHandler<?> typeHandler = getTypeHandler(fieldType);
                yield typeHandler == null
                        ? handlePrimitiveType(value, fieldType)
                        : typeHandler.apply(value, genericTypes);
            }
        };
    }

    protected Object handleArray(JSONArray array, @NotNull Class<?> fieldType, @NotNull List<Class<?>> genericTypes) {
        if (fieldType.isArray()) {
            int size = array.size();
            Class<?> componentType = fieldType.getComponentType();
            Object resultArray = getArraySupplier().apply(componentType, size);
            for (int i = 0; i < size; i++) {
                Object element = getForwardCaller().apply(array.get(i), componentType, genericTypes);
                getArraySetter().accept(resultArray, i, element);
            }

            return resultArray;
        }

        return handleCollection(array, fieldType, genericTypes);
    }

    protected Object handleCollection(JSONArray array, @NotNull Class<?> fieldType, @NotNull List<Class<?>> genericTypes) {
        TypeHandler<?> typeHandler = getTypeHandler(fieldType);
        if (typeHandler != null) {
            return typeHandler.apply(array, genericTypes);
        }

        throw MappingException.ofConversion(fieldType, JSONArray.class);
    }

    private MappingException conversionException(Object value, @NotNull Class<?> fieldType) {
        return MappingException.ofConversion(value.getClass(), fieldType);
    }

    protected Object handlePrimitiveType(Object value, @NotNull Class<?> fieldType) {
        return switch (fieldType.getSimpleName()) {
            case "boolean", "Boolean" -> {
                if (value instanceof Boolean b) yield b;
                if (value instanceof String s) yield Boolean.parseBoolean(s);
                throw conversionException(value, fieldType);
            }
            case "byte", "Byte" -> {
                if (value instanceof Byte b) yield b;
                if (value instanceof String s) yield Byte.parseByte(s);
                throw conversionException(value, fieldType);
            }
            case "short", "Short" -> {
                if (value instanceof Short s) yield s;
                if (value instanceof String s) yield Short.parseShort(s);
                throw conversionException(value, fieldType);
            }
            case "int", "Integer" -> {
                if (value instanceof Integer i) yield i;
                if (value instanceof String s) yield Integer.parseInt(s);
                throw conversionException(value, fieldType);
            }
            case "long", "Long" -> {
                if (value instanceof Long l) yield l;
                if (value instanceof String s) yield Long.parseLong(s);
                throw conversionException(value, fieldType);
            }
            case "float", "Float" -> {
                if (value instanceof Float f) yield f;
                if (value instanceof String s) yield Float.parseFloat(s);
                throw conversionException(value, fieldType);
            }
            case "double", "Double" -> {
                if (value instanceof Double d) yield d;
                if (value instanceof String s) yield Double.parseDouble(s);
                throw conversionException(value, fieldType);
            }
            case "char", "Character" -> {
                if (value instanceof String s) yield s.charAt(0);
                throw conversionException(value, fieldType);
            }
            case "String" -> {
                if (value instanceof String s) yield s;
                throw conversionException(value, fieldType);
            }

            default -> throw new IllegalStateException("Unexpected value: " + fieldType.getSimpleName());
        };
    }
}