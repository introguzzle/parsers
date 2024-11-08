package ru.introguzzle.parsers.json.mapping.serialization;

import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.cache.CacheService;
import ru.introguzzle.parsers.common.cache.CacheSupplier;
import ru.introguzzle.parsers.common.field.Extensions;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.common.inject.BindException;
import ru.introguzzle.parsers.common.inject.Binder;
import ru.introguzzle.parsers.json.entity.JSONArray;
import ru.introguzzle.parsers.json.entity.annotation.JSONField;
import ru.introguzzle.parsers.common.mapping.ClassHierarchyTraverseUtilities;
import ru.introguzzle.parsers.json.mapping.FieldAccessorImpl;
import ru.introguzzle.parsers.json.mapping.JSONFieldNameConverter;
import ru.introguzzle.parsers.json.mapping.JSONObjectConvertable;
import ru.introguzzle.parsers.json.mapping.MappingContext;
import ru.introguzzle.parsers.json.mapping.MappingException;
import ru.introguzzle.parsers.json.mapping.reference.StandardCircularReferenceStrategies.CircularReference;
import ru.introguzzle.parsers.json.mapping.type.JSONType;
import ru.introguzzle.parsers.json.entity.JSONObject;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static ru.introguzzle.parsers.json.mapping.serialization.TypeHandler.newEntry;

@ExtensionMethod({Extensions.class})
public class JSONMapperImpl implements JSONMapper {
    private static final CacheSupplier CACHE_SUPPLIER = CacheService.getInstance();

    private final Map<Class<?>, TypeHandler<?>> defaultTypeHandlers = Map.ofEntries(
            newEntry(Number.class, this::handleNumber),
            newEntry(Boolean.class, this::handleBoolean),
            newEntry(String.class, this::handleString)
    );

    private final FieldNameConverter<JSONField> nameConverter;
    private final Map<Class<?>, TypeHandler<?>> typeHandlers = new HashMap<>(DefaultTypeHandler.asMap());

    {
        typeHandlers.putAll(defaultTypeHandlers);
    }

    private final Map<Class<?>, TypeHandler<?>> typeHandlerCache = new ConcurrentHashMap<>();

    private static final FieldAccessor FIELD_ACCESSOR = new FieldAccessorImpl();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Cache<Field, MethodHandle> GETTER_CACHE;

    static {
        GETTER_CACHE = CACHE_SUPPLIER.newCache();
    }

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
    public JSONMapper bindTo(Class<? extends Bindable> type) throws BindException {
        createBinder(type).inject(type);
        return this;
    }

    @Override
    public JSONMapper unbind(Class<? extends Bindable> type) throws BindException {
        createBinder(type).uninject(type);
        return this;
    }

    @Override
    public FieldAccessor getFieldAccessor() {
        return FIELD_ACCESSOR;
    }

    private static MethodHandle acquireGetter(Field field) {
        return GETTER_CACHE.get(field, LOOKUP::unreflectGetter);
    }

    private TypeHandler<?> findMostSpecificTypeHandler(Class<?> type) {
        return ClassHierarchyTraverseUtilities.findMostSpecificMatch(typeHandlers, type).orElse(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeHandler<T> findTypeHandler(Class<T> type) {
        return (TypeHandler<T>) typeHandlerCache.computeIfAbsent(type, this::findMostSpecificTypeHandler);
    }

    @Override
    public <T> JSONMapper withTypeHandler(Class<T> type, TypeHandler<? super T> typeHandler) {
        this.typeHandlers.put(type, typeHandler);
        return this;
    }

    @Override
    public JSONMapper withTypeHandlers(Map<Class<?>, TypeHandler<?>> typeHandlers) {
        this.typeHandlers.putAll(typeHandlers);
        return this;
    }

    @Override
    public JSONMapper clearTypeHandlers() {
        this.typeHandlers.clear();
        return this;
    }

    @Override
    public JSONObject toJSONObject(@Nullable Object object, MappingContext context) {
        if (object == null) return null;

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
                value = acquireGetter(field).invokeWithArguments(object);
            } catch (Throwable e) {
                throw new MappingException("Cannot retrieve value from property: " + field.getName(), e);
            }

            Object handled = handle(field, value, context);
            if (handled instanceof CircularReference<?>) {
                result.getReferenceMap().put(result, result);
            }

            result.put(name, handled);
        }

        return result;
    }

    @Override
    public FieldNameConverter<JSONField> getNameConverter() {
        return nameConverter;
    }

    @Override
    public JSONArray toJSONArray(@NotNull Object iterable, MappingContext context) {
        if (iterable.getClass().isArray() || iterable instanceof Iterable<?>) {
            return handleArray(iterable.getClass(), iterable, context);
        }

        throw new MappingException("Cannot convert " + iterable + " to JSON array");
    }

    @SuppressWarnings("unchecked")
    private Object handle(@NotNull Field field, Object fieldValue, MappingContext context) {
        if (fieldValue == null) {
            return null;
        }

        JSONType actualType = field.getAnnotationAsOptional(JSONField.class)
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

        throw MappingException.of(fieldValue.getClass(), Boolean.class);
    }

    private Number handleNumber(Object fieldValue) {
        if (fieldValue instanceof Number number) {
            return number;
        }

        if (fieldValue instanceof String string) {
            return Double.parseDouble(string);
        }

        throw MappingException.of(fieldValue.getClass(), Number.class);
    }
}
