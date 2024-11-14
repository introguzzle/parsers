package ru.introguzzle.parsers.xml.mapping.deserialization;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.cache.CacheService;
import ru.introguzzle.parsers.common.cache.CacheSupplier;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.common.field.GenericTypeAccessor;
import ru.introguzzle.parsers.common.field.WritingInvoker;
import ru.introguzzle.parsers.common.function.TriFunction;
import ru.introguzzle.parsers.common.type.Primitives;
import ru.introguzzle.parsers.common.util.Maps;
import ru.introguzzle.parsers.common.mapping.MappingException;
import ru.introguzzle.parsers.common.mapping.Traverser;
import ru.introguzzle.parsers.common.mapping.deserialization.InstanceSupplier;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeHandler;
import ru.introguzzle.parsers.xml.entity.XMLAttribute;
import ru.introguzzle.parsers.xml.entity.XMLElement;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class ObjectElementMapperImpl implements ObjectElementMapper {
    private static final CacheSupplier CACHE_SUPPLIER = CacheService.instance();
    private static final Cache<Class<?>, TypeHandler<?>> HANDLER_CACHE = CACHE_SUPPLIER.newCache();

    private final ObjectMapper parent;

    private <T extends Collection<Object>>
    Map.Entry<Class<T>, TypeHandler<T>> newEntryOfIterable(Class<T> type,
                                                           Supplier<? extends T> supplier) {
        return TypeHandler.newEntry(type, (source, genericTypes) -> {
            T collection = supplier.get();
            if (genericTypes.isEmpty()) {
                throw new MappingException("Untyped collection is not supported");
            }

            Class<?> genericType = genericTypes.getFirst();
            if (source instanceof XMLElement element && element.isIterable()) {
                for (Object object : element.getChildren()) {
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
            newEntryOfIterable(Deque.class, LinkedList::new)
    ));

    private final Map<Class<?>, TypeHandler<?>> typeHandlers = new ConcurrentHashMap<>(defaultTypeHandlers);

    @Override
    public <T> @NotNull T toObject(@NotNull XMLElement root, @NotNull Class<T> type) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(type);
        T result = map(root, type);
        return Objects.requireNonNull(result);
    }

    private <T> @Nullable T map(@Nullable XMLElement element, @NotNull Class<T> type) {
        if (element == null) {
            return null;
        }

        T instance = getInstanceSupplier().acquire(element, type);
        List<Field> fields = getFieldAccessor().acquire(type);
        for (Field field : fields) {
            String name = getNameConverter().apply(field);
            Object value = element.get(name);
            List<Class<?>> genericTypes = getGenericTypeAccessor().acquire(field);

            getWritingInvoker().invoke(field, instance, match(value, field.getType(), genericTypes));
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
    public @Nullable Object match(@Nullable Object object, @NotNull Class<?> fieldType, @NotNull List<Class<?>> genericTypes) {
        TypeHandler<?> handler = getTypeHandler(fieldType);
        if (handler != null) {
            return handler.apply(object, genericTypes);
        }

        if (fieldType.isArray()) {
            if (!(object instanceof XMLElement element)) {
                throw new MappingException("Cannot map an array element to an object of type " + fieldType.getName());
            }

            return handleArray(element, fieldType, genericTypes);
        }

        boolean primitive = Primitives.isPrimitive(fieldType);

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

    private Object handleArray(XMLElement iterable, Class<?> fieldType, List<Class<?>> genericTypes) {
        Class<?> componentType = fieldType.getComponentType();
        int length = iterable.getChildren().size();
        Object array = Array.newInstance(componentType, length);

        for (int i = 0; i < length; i++) {
            XMLElement item = iterable.getChildren().get(i);
            Object element = getForwardCaller().apply(item, componentType, genericTypes);
            Array.set(array, i, element);
        }

        return array;
    }

    private Object handlePrimitiveType(@Nullable String value, @NotNull Class<?> fieldType) {
        if (value == null) {
            return null;
        }

        @SuppressWarnings("ALL")
        Class<?> ft = fieldType;

        try {
            if (ft == String.class) return value;
            if (value.isEmpty()) return null;
            if (ft == int.class || ft == Integer.class) return Integer.parseInt(value);
            if (ft == long.class || ft == Long.class) return Long.parseLong(value);
            if (ft == double.class || ft == Double.class) return Double.parseDouble(value);
            if (ft == float.class || ft == Float.class) return Float.parseFloat(value);
            if (ft == boolean.class || ft == Boolean.class) return Boolean.parseBoolean(value);
            if (ft == short.class || ft == Short.class) return Short.parseShort(value);
            if (ft == byte.class || ft == Byte.class) return Byte.parseByte(value);
            if (ft == char.class || ft == Character.class) {
                if (value.length() != 1) {
                    throw new MappingException("Cannot convert text to char: " + value);
                }

                return value.charAt(0);
            }

        } catch (NumberFormatException e) {
            throw new MappingException("Failed to parse element text '" + value + "' to type " + ft.getName(), e);
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable TypeHandler<T> getTypeHandler(@NotNull Class<T> fieldType) {
        return (TypeHandler<T>) HANDLER_CACHE.get(fieldType, this::findMostSpecificHandler);
    }

    @SuppressWarnings("unchecked")
    private <T> TypeHandler<T> findMostSpecificHandler(Class<T> fieldType) {
        return (TypeHandler<T>) getTraverser().findMostSpecificMatch(typeHandlers, fieldType).orElse(null);
    }

    @Override
    public @NotNull ObjectElementMapper bindTo(@NotNull Class<?> targetType) {
        parent.bindTo(targetType);
        return this;
    }

    @Override
    public @NotNull ObjectElementMapper unbind(@NotNull Class<?> targetType) {
        parent.unbind(targetType);
        return this;
    }

    @Override
    public @NotNull ObjectElementMapper unbindAll() {
        parent.unbindAll();
        return this;
    }

    @Override
    public @NotNull WritingInvoker getWritingInvoker() {
        return parent.getWritingInvoker();
    }

    @Override
    public @NotNull GenericTypeAccessor getGenericTypeAccessor() {
        return parent.getGenericTypeAccessor();
    }

    @Override
    public @NotNull TriFunction<Object, Class<?>, List<Class<?>>, Object> getForwardCaller() {
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
    public @NotNull <T> ObjectElementMapper withTypeHandler(@NotNull Class<T> type, @NotNull TypeHandler<? extends T> handler) {
        clearCache();
        typeHandlers.put(type, handler);
        return this;
    }

    @Override
    public @NotNull ObjectElementMapper withTypeHandlers(@NotNull Map<Class<?>, @NotNull TypeHandler<?>> handlers) {
        clearCache();
        typeHandlers.putAll(handlers);
        return this;
    }

    @Override
    public @NotNull ObjectElementMapper clearTypeHandlers() {
        typeHandlers.clear();
        return this;
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
