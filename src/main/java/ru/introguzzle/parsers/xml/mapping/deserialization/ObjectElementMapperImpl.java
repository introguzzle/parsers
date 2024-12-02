package ru.introguzzle.parsers.xml.mapping.deserialization;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.cache.CacheService;
import ru.introguzzle.parsers.common.cache.CacheSupplier;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.common.field.WritingInvoker;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeAdapter;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeResolver;
import ru.introguzzle.parsers.common.type.Primitives;
import ru.introguzzle.parsers.common.util.Maps;
import ru.introguzzle.parsers.common.mapping.MappingException;
import ru.introguzzle.parsers.common.mapping.Traverser;
import ru.introguzzle.parsers.common.mapping.deserialization.InstanceSupplier;
import ru.introguzzle.parsers.xml.entity.XMLAttribute;
import ru.introguzzle.parsers.xml.entity.XMLElement;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class ObjectElementMapperImpl implements ObjectElementMapper {
    private static final CacheSupplier CACHE_SUPPLIER = CacheService.instance();
    private static final Cache<Class<?>, TypeAdapter<?>> HANDLER_CACHE = CACHE_SUPPLIER.newCache();

    private final ObjectMapper parent;

    private <T extends Collection<Object>>
    Map.Entry<Class<T>, TypeAdapter<T>> newEntryOfIterable(Class<T> type,
                                                           Supplier<? extends T> supplier) {
        return TypeAdapter.newEntry(type, (source, t) -> {
            T collection = supplier.get();
            if (!(t instanceof ParameterizedType pt)) {
                throw new MappingException("Untyped collection is not supported");
            }

            Type genericType = pt.getActualTypeArguments()[0];
            if (source instanceof XMLElement element && element.isIterable()) {
                for (Object object : element.getChildren()) {
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
            newEntryOfIterable(Deque.class, LinkedList::new)
    ));

    private final Map<Class<?>, TypeAdapter<?>> typeHandlers = new ConcurrentHashMap<>(defaultTypeHandlers);

    @Override
    public @NotNull Object toObject(@NotNull XMLElement root, @NotNull Type type) {
        Objects.requireNonNull(type);
        Object result = map(root, type);
        return Objects.requireNonNull(result);
    }

    private @Nullable Object map(@Nullable XMLElement element, @NotNull Type type) {
        if (element == null) {
            return null;
        }

        Object instance = getInstanceSupplier().acquire(element, type);
        Class<?> rawType = getTypeResolver().getRawType(type);
        List<Field> fields = getFieldAccessor().acquire(rawType);
        Map<String, Type> resolved = getTypeResolver().resolveTypes(rawType, type);

        for (Field field : fields) {
            String name = getNameConverter().apply(field);
            Object value = element.get(name);
            Type fieldType = resolved.get(field.getName());

            Object matched = getForwardCaller().apply(value, fieldType);
            getWritingInvoker().invoke(field, instance, matched);
        }

        return instance;
    }

    /**
     *
     * @param object Actually, either {@code XMLElement}, {@code XMLAttribute}
     *               or {@code String} instance
     * @param fieldType field type
     * @return matched object for field
     */
    public @Nullable Object match(@Nullable Object object, @NotNull Type fieldType) {
        Class<?> raw = getTypeResolver().getRawType(fieldType);
        TypeAdapter<?> handler = findTypeAdapter(raw);
        if (handler != null) {
            return handler.apply(object, fieldType);
        }

        Class<?> componentType = getTypeResolver().getComponentType(fieldType);
        if (componentType != null) {
            if (!(object instanceof XMLElement element)) {
                throw new MappingException("Cannot map an array element to an object of type " + fieldType);
            }

            return handleArray(element, componentType);
        }

        boolean primitive = fieldType instanceof Class<?> cls
                && Primitives.isPrimitive(cls);

        return switch (object) {
            case null -> null;
            case XMLElement element -> {
                if (primitive) {
                    yield handlePrimitiveType(element.getText(), fieldType);
                }

                yield map(element, fieldType);
            }

            case XMLAttribute attribute -> {
                if (primitive) {
                    yield handlePrimitiveType(attribute.value(), fieldType);
                }

                throw new MappingException();
            }

            case String string -> {
                if (primitive) {
                    yield handlePrimitiveType(string, fieldType);
                }

                throw new MappingException();
            }

            default -> throw new AssertionError("Impossible to get here");
        };
    }

    private Object handleArray(XMLElement iterable, Class<?> componentType) {
        int length = iterable.getChildren().size();
        Object array = Array.newInstance(componentType, length);

        for (int i = 0; i < length; i++) {
            XMLElement item = iterable.getChildren().get(i);
            Object element = getForwardCaller().apply(item, componentType);
            Array.set(array, i, element);
        }

        return array;
    }

    private Object handlePrimitiveType(@Nullable String value, @NotNull Type fieldType) {
        if (value == null) {
            return null;
        }

        if (!(fieldType instanceof Class<?> pt)) {
            throw new MappingException(fieldType + " is not a primitive type");
        }

        try {
            if (pt == String.class) return value;
            if (value.isEmpty()) return null;
            if (pt == int.class || pt == Integer.class) return Integer.parseInt(value);
            if (pt == long.class || pt == Long.class) return Long.parseLong(value);
            if (pt == double.class || pt == Double.class) return Double.parseDouble(value);
            if (pt == float.class || pt == Float.class) return Float.parseFloat(value);
            if (pt == boolean.class || pt == Boolean.class) return Boolean.parseBoolean(value);
            if (pt == short.class || pt == Short.class) return Short.parseShort(value);
            if (pt == byte.class || pt == Byte.class) return Byte.parseByte(value);
            if (pt == char.class || pt == Character.class) {
                if (value.length() != 1) {
                    throw new MappingException("Cannot convert text to char: " + value);
                }

                return value.charAt(0);
            }

        } catch (NumberFormatException e) {
            throw new MappingException("Failed to parse element text '" + value + "' to type " + pt.getName(), e);
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable TypeAdapter<T> findTypeAdapter(@NotNull Class<T> fieldType) {
        return (TypeAdapter<T>) HANDLER_CACHE.get(fieldType, this::findMostSpecificHandler);
    }

    @SuppressWarnings("unchecked")
    private <T> TypeAdapter<T> findMostSpecificHandler(Class<T> fieldType) {
        return (TypeAdapter<T>) getTraverser().findMostSpecificMatch(typeHandlers, fieldType).orElse(null);
    }

    @Override
    public @NotNull WritingInvoker getWritingInvoker() {
        return parent.getWritingInvoker();
    }

    @Override
    public @NotNull BiFunction<Object, Type, Object> getForwardCaller() {
        return this::match;
    }

    @Override
    public @NotNull InstanceSupplier<XMLElement> getInstanceSupplier() {
        return parent.getInstanceSupplier();
    }

    private static void clearCache() {
        HANDLER_CACHE.invalidateAll();
    }

    @Override
    public @NotNull <T> ObjectElementMapper withTypeAdapter(@NotNull Class<T> type, @NotNull TypeAdapter<? extends T> adapter) {
        clearCache();
        typeHandlers.put(type, adapter);
        return this;
    }

    @Override
    public @NotNull ObjectElementMapper withTypeAdapters(@NotNull Map<Class<?>, @NotNull TypeAdapter<?>> adapters) {
        clearCache();
        typeHandlers.putAll(adapters);
        return this;
    }

    @Override
    public @NotNull ObjectElementMapper clearTypeAdapters() {
        typeHandlers.clear();
        return this;
    }

    @Override
    public @NotNull TypeResolver getTypeResolver() {
        return parent.getTypeResolver();
    }

    @Override
    public @NotNull FieldNameConverter<? extends Annotation> getNameConverter() {
        return parent.getNameConverter();
    }

    @Override
    public @NotNull FieldAccessor getFieldAccessor() {
        return parent.getFieldAccessor();
    }

    @Override
    public @NotNull Traverser<Class<?>> getTraverser() {
        return parent.getTraverser();
    }
}
