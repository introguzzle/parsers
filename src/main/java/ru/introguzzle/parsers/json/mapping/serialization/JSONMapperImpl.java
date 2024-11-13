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
import ru.introguzzle.parsers.common.inject.BindException;
import ru.introguzzle.parsers.common.inject.Binder;
import ru.introguzzle.parsers.common.mapping.ClassTraverser;
import ru.introguzzle.parsers.common.mapping.Traverser;
import ru.introguzzle.parsers.common.mapping.serialization.TypeHandler;
import ru.introguzzle.parsers.common.mapping.serialization.TypeHandlers;
import ru.introguzzle.parsers.json.entity.JSONArray;
import ru.introguzzle.parsers.json.entity.annotation.JSONField;
import ru.introguzzle.parsers.json.mapping.FieldAccessorImpl;
import ru.introguzzle.parsers.json.mapping.JSONFieldNameConverter;
import ru.introguzzle.parsers.json.mapping.JSONObjectConvertable;
import ru.introguzzle.parsers.json.mapping.MappingContext;
import ru.introguzzle.parsers.common.mapping.MappingException;
import ru.introguzzle.parsers.json.mapping.reference.StandardCircularReferenceStrategies.CircularReference;
import ru.introguzzle.parsers.json.mapping.type.JSONType;
import ru.introguzzle.parsers.json.entity.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class JSONMapperImpl implements JSONMapper {
    private static final CacheSupplier CACHE_SUPPLIER = CacheService.instance();
    private static final Cache<Class<?>, TypeHandler<?>> TYPE_HANDLER_CACHE = CACHE_SUPPLIER.newCache();

    private final FieldNameConverter<JSONField> nameConverter;
    private final Map<Class<?>, TypeHandler<?>> typeHandlers =
            Maps.of(TypeHandlers.DEFAULT, Map.ofEntries(
                    TypeHandler.newEntry(Number.class, this::handleNumber),
                    TypeHandler.newEntry(Boolean.class, this::handleBoolean),
                    TypeHandler.newEntry(String.class, this::handleString)
            ));

    private final FieldAccessor fieldAccessor = new FieldAccessorImpl();
    private final Traverser<Class<?>> traverser = new ClassTraverser();
    private final ReadingInvoker readingInvoker = new MethodHandleInvoker.Reading();

    public JSONMapperImpl() {
        this(new JSONFieldNameConverter());
    }

    public JSONMapperImpl(FieldNameConverter<JSONField> nameConverter) {
        this.nameConverter = nameConverter;
    }

    private Binder<JSONMapper, Bindable> createBinder(Class<? extends Bindable> type) {
        return new JSONMethodBinder(this, type);
    }

    @Override
    public @NotNull JSONMapper bindTo(@NotNull Class<? extends Bindable> type) throws BindException {
        createBinder(type).inject(type);
        return this;
    }

    @Override
    public @NotNull JSONMapper unbind(@NotNull Class<? extends Bindable> type) throws BindException {
        createBinder(type).uninject(type);
        return this;
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

    private TypeHandler<?> findMostSpecificTypeHandler(Class<?> type) {
        return getTraverser().findMostSpecificMatch(typeHandlers, type).orElse(null);
    }

    @SuppressWarnings("unchecked")
    public <T> TypeHandler<T> findTypeHandler(@NotNull Class<T> type) {
        return (TypeHandler<T>) TYPE_HANDLER_CACHE.get(type, this::findMostSpecificTypeHandler);
    }

    @Override
    public <T> @NotNull JSONMapper withTypeHandler(@NotNull Class<T> type, @NotNull TypeHandler<? super T> typeHandler) {
        this.typeHandlers.put(type, typeHandler);
        return this;
    }

    @Override
    public @NotNull JSONMapper withTypeHandlers(@NotNull Map<Class<?>, TypeHandler<?>> handlers) {
        this.typeHandlers.putAll(handlers);
        return this;
    }

    @Override
    public @NotNull JSONMapper clearTypeHandlers() {
        this.typeHandlers.clear();
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
        TypeHandler<?> typeHandler;
        if ((typeHandler = findTypeHandler(object.getClass())) != null) {
            return ((TypeHandler<Object>) typeHandler).apply(object);
        }

        List<Field> fields = getFieldAccessor().acquire(object.getClass());

        for (Field field : fields) {
            String name = getNameConverter().apply(field);
            Object value;

            try {
                value = getReadingInvoker().invoke(field, object);
            } catch (Throwable e) {
                throw new MappingException("Cannot retrieve value from property: " + field.getName(), e);
            }

            Object handled = handle(field, value, context);
            if (handled instanceof CircularReference<?>) {
                result.setApplier(this);
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
            TypeHandler<?> typeHandler = findTypeHandler(field.getType());
            if (typeHandler != null) {
                return ((TypeHandler<Object>) typeHandler).apply(fieldValue);
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
                    array.add(map(item, context));
                }
            }
            return array;
        }

        if (Iterable.class.isAssignableFrom(fieldType)) {
            Iterable<?> iterable = (Iterable<?>) fieldValue;
            for (Object item : iterable) {
                array.add(map(item, context));
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
