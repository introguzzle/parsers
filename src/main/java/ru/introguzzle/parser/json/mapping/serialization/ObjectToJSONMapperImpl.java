package ru.introguzzle.parser.json.mapping.serialization;

import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parser.common.utilities.NamingUtilities;
import ru.introguzzle.parser.json.entity.JSONArray;
import ru.introguzzle.parser.json.entity.annotation.JSONField;
import ru.introguzzle.parser.json.mapping.*;
import ru.introguzzle.parser.json.mapping.reference.StandardCircularReferenceStrategies.CircularReference;
import ru.introguzzle.parser.json.mapping.type.JSONType;
import ru.introguzzle.parser.json.entity.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ExtensionMethod({Fields.Extensions.class})
public class ObjectToJSONMapperImpl implements ObjectToJSONMapper {
    private static <T> Map.Entry<Class<T>, TypeHandler<? super T>> entryOf(Class<T> type, TypeHandler<? super T> typeHandler) {
        return Map.entry(type, typeHandler);
    }

    private final Map<Class<?>, TypeHandler<?>> defaultTypeHandlers = Map.ofEntries(
            entryOf(Number.class, this::handleNumber),
            entryOf(Boolean.class, this::handleBoolean),
            entryOf(String.class, this::handleString),
            entryOf(Character.class, Object::toString),
            entryOf(Date.class, Object::toString),
            entryOf(Enum.class, Enum::name),
            entryOf(Temporal.class, Object::toString),
            entryOf(TemporalAdjuster.class, Object::toString),
            entryOf(TemporalAmount.class, Object::toString),
            entryOf(UUID.class, Object::toString),
            entryOf(BigDecimal.class, BigDecimal::toPlainString),
            entryOf(BigInteger.class, Object::toString),
            entryOf(URL.class, Object::toString),
            entryOf(URI.class, Object::toString),
            entryOf(Throwable.class, Throwable::getMessage),
            entryOf(Class.class, Class::getSimpleName)
    );

    private final FieldNameConverter<Field> nameConverter;
    private final Map<Class<?>, TypeHandler<?>> typeHandlers = new HashMap<>(defaultTypeHandlers);
    private final Map<Class<?>, TypeHandler<?>> handlerCache = new ConcurrentHashMap<>();

    public ObjectToJSONMapperImpl() {
        this(new ReflectionFieldNameConverter(NamingUtilities::toSnakeCase));
    }

    public ObjectToJSONMapperImpl(FieldNameConverter<Field> nameConverter) {
        this.nameConverter = nameConverter;
    }

    private TypeHandler<?> findMostSpecificHandler(Class<?> type) {
        return ClassHierarchyTraverseUtilities.findMostSpecificMatch(typeHandlers, type).orElse(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeHandler<T> getTypeHandler(Class<T> type) {
        synchronized (this) {
            return (TypeHandler<T>) handlerCache.computeIfAbsent(type, this::findMostSpecificHandler);
        }
    }

    @Override
    public <T> ObjectToJSONMapper withTypeHandler(Class<T> type, TypeHandler<? super T> typeHandler) {
        this.typeHandlers.put(type, typeHandler);
        return this;
    }

    @Override
    public ObjectToJSONMapper withTypeHandlers(Map<Class<?>, TypeHandler<?>> typeHandlers) {
        this.typeHandlers.putAll(typeHandlers);
        return this;
    }

    @Override
    public ObjectToJSONMapper clearTypeHandlers() {
        this.typeHandlers.clear();
        return this;
    }

    @Override
    public JSONObject toJSONObject(@Nullable Object object, MappingContext context) {
        if (object == null) return null;

        return (JSONObject) toJSONObjectRecursive(object, context);
    }

    @SuppressWarnings("unchecked")
    private Object toJSONObjectRecursive(@Nullable Object object, MappingContext context) {
        if (object == null) return null;
        if (context.containsReference(object)) {
            if (getTypeHandler(object.getClass()) == null) {
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
        if ((typeHandler = getTypeHandler(object.getClass())) != null) {
            return ((TypeHandler<Object>) typeHandler).apply(object);
        }

        List<Field> fields = Fields.getCached(object.getClass());

        for (Field field : fields) {
            field.setAccessible(true);

            String name = nameConverter.convert(field);

            Object value;
            try {
                value = field.getValue(object);
            } catch (Exception e) {
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
    public FieldNameConverter<Field> getNameConverter() {
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

        TypeHandler<?> typeHandler = getTypeHandler(field.getType());
        if (typeHandler != null) {
            return ((TypeHandler<Object>) typeHandler).apply(fieldValue);
        }

        JSONType actualType = field.getAnnotationAsOptional(JSONField.class)
                .map(JSONField::type)
                .orElse(JSONType.UNSPECIFIED);

        if (actualType == JSONType.UNSPECIFIED) {
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

                        object.put(key, toJSONObjectRecursive(entry.getValue(), context));
                    }

                    yield object;
                }

                yield toJSONObjectRecursive(fieldValue, context);
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
                    array.add(toJSONObjectRecursive(element, context));
                }

            } else {
                Object[] objects = (Object[]) fieldValue;
                for (Object item : objects) {
                    array.add(toJSONObjectRecursive(item, context));
                }
            }
            return array;
        }

        if (Iterable.class.isAssignableFrom(fieldType)) {
            Iterable<?> iterable = (Iterable<?>) fieldValue;
            for (Object item : iterable) {
                array.add(toJSONObjectRecursive(item, context));
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
