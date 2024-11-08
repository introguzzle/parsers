package ru.introguzzle.parsers.json.mapping.deserialization;

import lombok.experimental.ExtensionMethod;
import ru.introguzzle.parsers.common.field.Extensions;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.utility.ReflectionUtilities;
import ru.introguzzle.parsers.json.entity.JSONArray;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.common.mapping.ClassHierarchyTraverseUtilities;
import ru.introguzzle.parsers.json.mapping.FieldAccessorImpl;
import ru.introguzzle.parsers.json.mapping.MappingException;
import ru.introguzzle.parsers.json.mapping.MappingInstantiationException;
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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@ExtensionMethod({ReflectionUtilities.class, Extensions.class})
public abstract class AbstractObjectMapper implements ObjectMapper {
    private static <T> Map.Entry<Class<T>, TypeHandler<? extends T>> entryOf(Class<T> type, TypeHandler<? extends T> typeHandler) {
        return Map.entry(type, typeHandler);
    }

    private final Map<Class<?>, TypeHandler<?>> defaultTypeHandlers = Map.ofEntries(
            entryOf(Date.class, o -> {
                if (o instanceof String string) {
                    return Date.from(Instant.parse(string));
                }

                throw new MappingInstantiationException(Date.class, o.getClass());
            }),
            entryOf(BigDecimal.class, o -> {
                if (o instanceof String string) {
                    return new BigDecimal(string);
                }

                throw new MappingInstantiationException(BigDecimal.class, o.getClass());
            }),
            entryOf(BigInteger.class, o -> {
                if (o instanceof String string) {
                    return new BigInteger(string);
                }

                throw new MappingInstantiationException(BigInteger.class, o.getClass());
            }),
            entryOf(URI.class, o -> {
                if (o instanceof String string) {
                    return URI.create(string);
                }

                throw new MappingInstantiationException(URI.class, o.getClass());
            }),
            entryOf(Throwable.class, o -> {
                if (o instanceof String string) {
                    return new Throwable(string);
                }

                throw new MappingInstantiationException(Throwable.class, o.getClass());
            }),
            entryOf(Class.class, o -> {
                if (o instanceof String string) {
                    try {
                        return Class.forName(string);
                    } catch (ClassNotFoundException e) {
                        throw new MappingException("Cannot instantiate Class", e);
                    }
                }

                throw new MappingInstantiationException(Class.class, o.getClass());
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

    private static final FieldAccessor FIELD_ACCESSOR = new FieldAccessorImpl();

    @Override
    public FieldAccessor getFieldAccessor() {
        return FIELD_ACCESSOR;
    }

    private final Map<JSONObject, Object> referenceMap = new IdentityHashMap<>();
    private final Map<Class<?>, TypeHandler<?>> typeHandlers = new ConcurrentHashMap<>(defaultTypeHandlers);

    protected abstract String getCircularPlaceholder();
    protected abstract InstanceSupplier getInstanceSupplier();
    protected abstract BiFunction<Class<?>, Integer, Object> getArraySupplier();

    @FunctionalInterface
    public interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }

    protected abstract TriConsumer<Object, Integer, Object> getArraySetter();

    @Override
    public <T> ObjectMapper withTypeHandler(Class<T> type, TypeHandler<? extends T> typeHandler) {
        this.typeHandlers.put(type, typeHandler);
        return this;
    }

    @Override
    public ObjectMapper withTypeHandlers(Map<Class<?>, TypeHandler<?>> typeHandlers) {
        this.typeHandlers.putAll(typeHandlers);
        return this;
    }

    @Override
    public ObjectMapper clearTypeHandlers() {
        this.typeHandlers.clear();
        return this;
    }

    @Override
    public <T> TypeHandler<T> getTypeHandler(Class<T> type) {
        synchronized (this) {
            return findMostSpecificHandler(type);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> TypeHandler<T> findMostSpecificHandler(Class<T> type) {
        return (TypeHandler<T>) ClassHierarchyTraverseUtilities.findMostSpecificMatch(typeHandlers, type).orElse(null);
    }

    @Override
    public BiFunction<Object, Class<?>, Object> getForwardCaller() {
        return this::match;
    }

    @Override
    public <T> T toObject(JSONObject object, Class<T> type) {
        T result = map(object, type);
        referenceMap.clear();
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(JSONArray array, Class<T[]> type) {
        return (T[]) handleArray(array, type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, R extends Collection<T>> R toCollection(JSONArray array, Class<T> type, Supplier<R> supplier) {
        R collection = supplier.get();
        for (Object item : array) {
            collection.add((T) getForwardCaller().apply(item, type));
        }

        return collection;
    }

    @SuppressWarnings("unchecked")
    public <T> T map(JSONObject object, Class<T> type) {
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
                    field.setValue(instance, reference.getValue());
                    continue;
                }

                if (value != null) {
                    field.setValue(instance, getForwardCaller().apply(value, field.getType()));
                }
            }
        }

        return instance;
    }

    protected Object match(Object value, Class<?> fieldType) {
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

    protected Object handleArray(JSONArray array, Class<?> fieldType) {
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

    protected Object handleCollection(JSONArray array, Class<?> fieldType) {
        TypeHandler<?> typeHandler = getTypeHandler(fieldType);
        if (typeHandler != null) {
            return typeHandler.apply(array);
        }

        throw new MappingInstantiationException(fieldType, JSONArray.class);
    }

    protected Object handlePrimitiveType(Object value, Class<?> fieldType) {
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