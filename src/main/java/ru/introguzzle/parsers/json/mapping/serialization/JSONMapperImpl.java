package ru.introguzzle.parsers.json.mapping.serialization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parsers.common.util.Maps;
import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.cache.CacheService;
import ru.introguzzle.parsers.common.cache.CacheSupplier;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.common.field.MethodHandleInvoker;
import ru.introguzzle.parsers.common.field.ReadingInvoker;
import ru.introguzzle.parsers.common.mapping.ClassTraverser;
import ru.introguzzle.parsers.common.mapping.Traverser;
import ru.introguzzle.parsers.common.mapping.serialization.TypeAdapter;
import ru.introguzzle.parsers.common.mapping.serialization.TypeAdapters;
import ru.introguzzle.parsers.json.entity.JSONArray;
import ru.introguzzle.parsers.json.entity.annotation.JSONField;
import ru.introguzzle.parsers.json.mapping.JSONFieldAccessor;
import ru.introguzzle.parsers.json.mapping.JSONFieldNameConverter;
import ru.introguzzle.parsers.json.mapping.JSONObjectConvertable;
import ru.introguzzle.parsers.json.mapping.MappingContext;
import ru.introguzzle.parsers.common.mapping.MappingException;
import ru.introguzzle.parsers.json.mapping.reference.StandardCircularReferenceStrategies.CircularReference;
import ru.introguzzle.parsers.json.mapping.type.JSONType;
import ru.introguzzle.parsers.json.entity.JSONObject;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

class JSONMapperImpl implements JSONMapper {
    private static final MethodHandle SET_PRODUCER_HANDLE;

    static {
        try {
            Class<?> internal = Class.forName("ru.introguzzle.parsers.json.entity.Internal");
            Field f = internal.getDeclaredField("SET_PRODUCER");
            f.setAccessible(true);
            SET_PRODUCER_HANDLE = (MethodHandle) f.get(null);

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static final CacheSupplier CACHE_SUPPLIER = CacheService.instance();
    private static final Cache<Class<?>, TypeAdapter<?>> TYPE_HANDLER_CACHE = CACHE_SUPPLIER.newCache();

    private final FieldNameConverter<JSONField> nameConverter;
    private final Map<Class<?>, TypeAdapter<?>> typeAdapters =
            Maps.of(TypeAdapters.DEFAULT, Map.ofEntries(
                    TypeAdapter.newEntry(Number.class, this::handleNumber),
                    TypeAdapter.newEntry(Boolean.class, this::handleBoolean),
                    TypeAdapter.newEntry(String.class, this::handleString)
            ));

    private final FieldAccessor fieldAccessor = new JSONFieldAccessor();
    private final Traverser<Class<?>> traverser = new ClassTraverser();
    private final ReadingInvoker readingInvoker = new MethodHandleInvoker.Reading();

    public JSONMapperImpl() {
        this(new JSONFieldNameConverter());
    }

    public JSONMapperImpl(FieldNameConverter<JSONField> nameConverter) {
        this.nameConverter = nameConverter;
    }

    @Override
    public @NotNull FieldAccessor getFieldAccessor() {
        return fieldAccessor;
    }

    @Override
    public @NotNull Traverser<Class<?>> getTraverser() {
        return traverser;
    }

    @Override
    public @NotNull ReadingInvoker getReadingInvoker() {
        return readingInvoker;
    }

    private TypeAdapter<?> findMostSpecificTypeHandler(Class<?> type) {
        return getTraverser().findMostSpecificMatch(typeAdapters, type).orElse(null);
    }

    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> findTypeHandler(@NotNull Class<T> type) {
        return (TypeAdapter<T>) TYPE_HANDLER_CACHE.get(type, this::findMostSpecificTypeHandler);
    }

    @Override
    public <T> @NotNull JSONMapper withTypeAdapter(@NotNull Class<T> type, @NotNull TypeAdapter<? super T> typeAdapter) {
        typeAdapters.put(type, typeAdapter);
        return this;
    }

    @Override
    public @NotNull JSONMapper withTypeAdapters(@NotNull Map<Class<?>, TypeAdapter<?>> adapters) {
        typeAdapters.putAll(adapters);
        return this;
    }

    @Override
    public @NotNull JSONMapper clearTypeAdapters() {
        typeAdapters.clear();
        return this;
    }

    @Override
    public @NotNull JSONObject toJSONObject(@NotNull Object object, @NotNull MappingContext context) {
        Objects.requireNonNull(object);
        return (JSONObject) map(object, context);
    }

    @SuppressWarnings("unchecked")
    private Object map(@Nullable Object object, MappingContext context) {
        if (object == null) return null;
        if (context.containsReference(object)) {
            if (findTypeHandler(object.getClass()) == null) {
                return context.getCircularReferenceStrategy().handle(object);
            }

            return object;
        }

        context.putReference(object);

        if (object instanceof JSONObjectConvertable convertable) {
            return convertable.toJSONObject();
        }

        JSONObject result = new JSONObject();
        TypeAdapter<?> typeAdapter;
        if ((typeAdapter = findTypeHandler(object.getClass())) != null) {
            return ((TypeAdapter<Object>) typeAdapter).apply(object);
        }

        List<Field> fields = getFieldAccessor().acquire(object.getClass());

        for (Field field : fields) {
            String name = getNameConverter().apply(field);
            Object value;

            try {
                value = Modifier.isStatic(field.getModifiers())
                        ? getReadingInvoker().invokeStatic(field, object)
                        : getReadingInvoker().invoke(field, object);
            } catch (Throwable e) {
                throw new MappingException("Cannot retrieve value from property: " + field.getName(), e);
            }

            Object handled = handle(field, value, context);
            if (handled instanceof CircularReference<?>) {
                try {
                    SET_PRODUCER_HANDLE.invokeWithArguments(result, this);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }

            result.put(name, handled);
        }

        return result;
    }

    @Override
    public @NotNull FieldNameConverter<JSONField> getNameConverter() {
        return nameConverter;
    }

    @Override
    public @NotNull JSONArray toJSONArray(@NotNull Object[] array, @NotNull MappingContext context) {
        return toJSONArray((Object) array, context);
    }

    @Override
    public @NotNull JSONArray toJSONArray(@NotNull Iterable<?> iterable, @NotNull MappingContext context) {
        return toJSONArray((Object) iterable, context);
    }

    public @NotNull JSONArray toJSONArray(@NotNull Object iterable, @NotNull MappingContext context) {
        if (iterable.getClass().isArray() || iterable instanceof Iterable<?>) {
            return handleArray(iterable.getClass(), iterable, context);
        }

        throw new MappingException("Cannot convert " + iterable + " to JSON array");
    }

    @SuppressWarnings("unchecked")
    private Object handle(@NotNull Field field, @Nullable Object fieldValue, @NotNull MappingContext context) {
        if (fieldValue == null) {
            return null;
        }

        JSONType actualType = Optional.ofNullable(field.getAnnotation(JSONField.class))
                .map(JSONField::type)
                .orElse(JSONType.UNSPECIFIED);

        if (actualType == JSONType.UNSPECIFIED) {
            TypeAdapter<?> typeAdapter = findTypeHandler(field.getType());
            if (typeAdapter != null) {
                return ((TypeAdapter<Object>) typeAdapter).apply(fieldValue);
            }

            actualType = inferType(field, fieldValue);
        }

        return infer(actualType, field, fieldValue, context);
    }

    private JSONType inferType(Field field, Object fieldValue) {
        Class<?> type = field.getType();

        if (fieldValue instanceof Number) {
            return JSONType.NUMBER;
        }

        if (fieldValue instanceof Boolean) {
            return JSONType.BOOLEAN;
        }

        if (fieldValue instanceof String) {
            return JSONType.STRING;
        }

        if (fieldValue instanceof Iterable<?> || type.isArray()) {
            return JSONType.ARRAY;
        }

        return JSONType.OBJECT;
    }

    private Object infer(JSONType type, Field field, Object fieldValue, MappingContext context) {
        Class<?> fieldType = field.getType();
        return switch (type) {
            case NUMBER      -> handleNumber(fieldValue);
            case BOOLEAN     -> handleBoolean(fieldValue);
            case STRING      -> handleString(fieldValue);
            case ARRAY       -> handleArray(fieldType, fieldValue, context);
            case OBJECT      -> {
                if (Map.class.isAssignableFrom(fieldType)) {
                    JSONObject object = new JSONObject();
                    Map<?, ?> map = (Map<?, ?>) fieldValue;
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        if (!(entry.getKey() instanceof String key)) {
                            throw new MappingException("Map keys must be of type String");
                        }

                        object.put(key, map(entry.getValue(), context));
                    }

                    yield object;
                }

                yield map(fieldValue, context);
            }
            case UNSPECIFIED -> throw new AssertionError("Impossible to get here");
        };
    }

    private JSONArray handleArray(Class<?> fieldType, Object fieldValue, MappingContext context) {
        JSONArray array = new JSONArray();
        if (fieldType.isArray()) {
            Class<?> componentType = fieldType.getComponentType();

            if (componentType.isPrimitive()) {
                int length = Array.getLength(fieldValue);
                for (int i = 0; i < length; i++) {
                    Object element = Array.get(fieldValue, i);
                    array.add(map(element, context));
                }

            } else {
                Object[] objects = (Object[]) fieldValue;
                for (Object item : objects) {
                    boolean nested = item != null && (item.getClass().isArray() || item instanceof Iterable<?>);

                    array.add(nested
                            // item is not null
                            ? handleArray(item.getClass(), item, context)
                            : map(item, context)
                    );
                }
            }
            return array;
        }

        if (Iterable.class.isAssignableFrom(fieldType)) {
            Iterable<?> iterable = (Iterable<?>) fieldValue;
            for (Object item : iterable) {
                boolean nested = item != null && (item.getClass().isArray() || item instanceof Iterable<?>);
                array.add(nested
                        // item is not null
                        ? handleArray(item.getClass(), item, context)
                        : map(item, context)
                );
            }
        }

        return array;
    }

    private String handleString(Object fieldValue) {
        if (fieldValue instanceof String string) {
            return string;
        }

        return fieldValue.toString();
    }

    private Boolean handleBoolean(Object fieldValue) {
        if (fieldValue instanceof Boolean bool) {
            return bool;
        }

        if (fieldValue instanceof String string) {
            return Boolean.parseBoolean(string);
        }

        throw MappingException.ofConversion(fieldValue.getClass(), Boolean.class);
    }

    private Number handleNumber(Object fieldValue) {
        if (fieldValue instanceof Number number) {
            return number;
        }

        if (fieldValue instanceof String string) {
            return Double.parseDouble(string);
        }

        throw MappingException.ofConversion(fieldValue.getClass(), Number.class);
    }
}
