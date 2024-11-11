package ru.introguzzle.parsers.json.mapping.deserialization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.function.TriConsumer;
import ru.introguzzle.parsers.common.mapping.ClassTraverser;
import ru.introguzzle.parsers.common.mapping.MappingException;
import ru.introguzzle.parsers.common.mapping.Traverser;
import ru.introguzzle.parsers.common.mapping.deserialization.InstanceSupplier;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeHandler;
import ru.introguzzle.parsers.json.entity.JSONArray;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.json.mapping.FieldAccessorImpl;
import ru.introguzzle.parsers.json.mapping.JSONMappingException;
import ru.introguzzle.parsers.json.mapping.reference.StandardCircularReferenceStrategies.CircularReference;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class AbstractObjectMapper implements ObjectMapper {
    private static <T> Map.Entry<Class<T>, TypeHandler<? extends T>> entryOf(Class<T> type, TypeHandler<? extends T> typeHandler) {
        return Map.entry(type, typeHandler);
    }

    private final Map<Class<?>, TypeHandler<?>> defaultTypeHandlers = Map.ofEntries(
            entryOf(Date.class, o -> {
                if (o instanceof String string) {
                    return Date.from(Instant.parse(string));
                }

                throw new JSONMappingException(Date.class, o.getClass());
            }),
            entryOf(BigDecimal.class, o -> {
                if (o instanceof String string) {
                    return new BigDecimal(string);
                }

                throw new JSONMappingException(BigDecimal.class, o.getClass());
            }),
            entryOf(BigInteger.class, o -> {
                if (o instanceof String string) {
                    return new BigInteger(string);
                }

                throw new JSONMappingException(BigInteger.class, o.getClass());
            }),
            entryOf(URI.class, o -> {
                if (o instanceof String string) {
                    return URI.create(string);
                }

                throw new JSONMappingException(URI.class, o.getClass());
            }),
            entryOf(Throwable.class, o -> {
                if (o instanceof String string) {
                    return new Throwable(string);
                }

                throw new JSONMappingException(Throwable.class, o.getClass());
            }),
            entryOf(Class.class, o -> {
                if (o instanceof String string) {
                    try {
                        return Class.forName(string);
                    } catch (ClassNotFoundException e) {
                        throw new JSONMappingException("Cannot instantiate Class", e);
                    }
                }

                throw new JSONMappingException(Class.class, o.getClass());
            }),
            entryOf(List.class, o -> {
                List<Object> list = new ArrayList<>();
                if (o instanceof Collection<?> collection) {
                    for (Object object : collection) {
                        list.add(getForwardCaller().apply(object, object.getClass()));
                    }
                }

                return list;
            }),
            entryOf(Set.class, o -> {
                Set<Object> set = new HashSet<>();
                if (o instanceof Collection<?> collection) {
                    for (Object object : collection) {
                        set.add(getForwardCaller().apply(object, object.getClass()));
                    }
                }
                return set;
            })
    );

    private final Traverser<Class<?>> traverser = new ClassTraverser();
    private final FieldAccessor fieldAccessor = new FieldAccessorImpl();
    private final Map<JSONObject, Object> referenceMap = new IdentityHashMap<>();
    private final Map<Class<?>, TypeHandler<?>> typeHandlers = new ConcurrentHashMap<>(defaultTypeHandlers);

    @Override
    public @NotNull FieldAccessor getFieldAccessor() {
        return fieldAccessor;
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
    protected abstract @NotNull InstanceSupplier<JSONObject> getInstanceSupplier();
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

    private @Nullable <T> TypeHandler<T> getTypeHandler(@NotNull Class<T> type) {
        return findMostSpecificHandler(type);
    }

    @SuppressWarnings("unchecked")
    protected <T> TypeHandler<T> findMostSpecificHandler(Class<T> type) {
        return (TypeHandler<T>) getTraverser().findMostSpecificMatch(typeHandlers, type).orElse(null);
    }

    @Override
    public @NotNull BiFunction<Object, Class<?>, Object> getForwardCaller() {
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
        return (T[]) handleArray(array, type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, C extends Collection<T>> @NotNull C toCollection(@NotNull JSONArray array, @NotNull Class<T> type, @NotNull Supplier<C> supplier) {
        C collection = supplier.get();
        for (Object item : array) {
            collection.add((T) getForwardCaller().apply(item, type));
        }

        return collection;
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T map(@Nullable JSONObject object, @NotNull Class<T> type) {
        if (object == null) {
            return null;
        }

        if (Map.class.isAssignableFrom(type)) {
            if (type == Map.class) {
                return (T) new HashMap<>(object);
            }

            Map<String, Object> map = (Map<String, Object>) getInstanceSupplier().get(object, type);
            map.putAll(object);
            return (T) map;
        }

        T instance = getInstanceSupplier().get(object, type);
        referenceMap.put(object, instance);

        List<Field> fields = getFieldAccessor().acquire(type);
        for (Field field : fields) {
            field.setAccessible(true);

            String name = getNameConverter().apply(field);
            Object value = object.get(name);
            if (getCircularPlaceholder().equals(value)) {
                value = referenceMap.get(object);
            }

            if (!Modifier.isFinal(field.getModifiers())) {
                if (value instanceof CircularReference<?> reference) {
                    getWritingInvoker().invoke(field, instance, reference.getValue());
                    continue;
                }

                if (value != null) {
                    Object applied = getForwardCaller().apply(value, field.getType());
                    getWritingInvoker().invoke(field, instance, applied);
                }
            }
        }

        return instance;
    }

    protected @Nullable Object match(@Nullable Object value, @NotNull Class<?> fieldType) {
        return switch (value) {
            case null -> null;
            case JSONObject object -> {
                if (referenceMap.containsKey(object)) {
                    yield referenceMap.get(object);
                }

                yield map(object, fieldType);
            }
            case JSONArray array -> handleArray(array, fieldType);
            default -> {
                TypeHandler<?> typeHandler = getTypeHandler(fieldType);
                yield typeHandler == null
                        ? handlePrimitiveType(value, fieldType)
                        : typeHandler.apply(value);
            }
        };
    }

    protected Object handleArray(JSONArray array, @NotNull Class<?> fieldType) {
        if (fieldType.isArray()) {
            int size = array.size();
            Class<?> componentType = fieldType.getComponentType();
            Object resultArray = getArraySupplier().apply(componentType, size);
            for (int i = 0; i < size; i++) {
                Object element = getForwardCaller().apply(array.get(i), fieldType.getComponentType());
                getArraySetter().accept(resultArray, i, element);
            }

            return resultArray;
        }

        return handleCollection(array, fieldType);
    }

    protected Object handleCollection(JSONArray array, @NotNull Class<?> fieldType) {
        TypeHandler<?> typeHandler = getTypeHandler(fieldType);
        if (typeHandler != null) {
            return typeHandler.apply(array);
        }

        throw new JSONMappingException(fieldType, JSONArray.class);
    }

    protected Object handlePrimitiveType(Object value, @NotNull Class<?> fieldType) {
        Supplier<MappingException> e = () -> MappingException.of(value.getClass(), fieldType);
        return switch (fieldType.getSimpleName()) {
            case "boolean", "Boolean" -> {
                if (value instanceof Boolean b) yield b;
                if (value instanceof String s) yield Boolean.parseBoolean(s);
                throw e.get();
            }
            case "byte", "Byte" -> {
                if (value instanceof Byte b) yield b;
                if (value instanceof String s) yield Byte.parseByte(s);
                throw e.get();
            }
            case "short", "Short" -> {
                if (value instanceof Short s) yield s;
                if (value instanceof String s) yield Short.parseShort(s);
                throw e.get();
            }
            case "int", "Integer" -> {
                if (value instanceof Integer i) yield i;
                if (value instanceof String s) yield Integer.parseInt(s);
                throw e.get();
            }
            case "long", "Long" -> {
                if (value instanceof Long l) yield l;
                if (value instanceof String s) yield Long.parseLong(s);
                throw e.get();
            }
            case "float", "Float" -> {
                if (value instanceof Float f) yield f;
                if (value instanceof String s) yield Float.parseFloat(s);
                throw e.get();
            }
            case "double", "Double" -> {
                if (value instanceof Double d) yield d;
                if (value instanceof String s) yield Double.parseDouble(s);
                throw e.get();
            }
            case "char", "Character" -> {
                if (value instanceof String s) yield s.charAt(0);
                throw e.get();
            }
            case "String" -> {
                if (value instanceof String s) yield s;
                throw e.get();
            }

            default -> throw new IllegalStateException("Unexpected value: " + fieldType.getSimpleName());
        };
    }
}