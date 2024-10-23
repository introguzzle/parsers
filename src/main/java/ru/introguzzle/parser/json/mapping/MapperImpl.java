package ru.introguzzle.parser.json.mapping;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parser.json.mapping.context.MappingContext;
import ru.introguzzle.parser.json.mapping.type.JSONType;
import ru.introguzzle.parser.json.mapping.type.TypeHandlers;
import ru.introguzzle.parser.common.utilities.NamingUtilities;
import ru.introguzzle.parser.common.utilities.ReflectionUtilities;
import ru.introguzzle.parser.json.entity.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Converts POJO into {@code JSONObject}
 * @see JSONObject
 */
public class MapperImpl implements Mapper {
    private static final Set<Class<?>> EXCLUDED = Set.of(
        String.class, Boolean.class, Number.class, Character.class, Date.class
    );

    private final Set<Object> references = new HashSet<>();

    @Override
    public JSONObject toJSONObject(@NotNull Object object, MappingContext context) {
        JSONObject result = (JSONObject) map(object, context);
        references.clear();
        return result;
    }

    @Override
    public Object map(@Nullable Object object, MappingContext context) {
        if (object == null) return null;
        if (references.contains(object)) {
            if (!EXCLUDED.contains(object.getClass())) {
                return context.circularReferenceStrategy.handle(object);
            }

            return object;
        }

        references.add(object);

        if (object instanceof JSONObjectConvertable convertable) {
            return convertable.toJSONObject();
        }

        JSONObject result = new JSONObject();
        Class<?> objectClass = object.getClass();

        for (Class<?> excluded : EXCLUDED)
            if (objectClass == excluded || excluded.isAssignableFrom(objectClass))
                return object;

        if (objectClass == Date.class) {
            return object.toString();
        }

        JSONEntity entityAnnotation = objectClass.getAnnotation(JSONEntity.class);
        List<Field> fields = ReflectionUtilities.getAllFields(objectClass);
        if (entityAnnotation != null) {
            String[] excludedFields = entityAnnotation.excluded();
            List<String> excludedFieldList = Arrays.asList(excludedFields);
            fields = fields.stream()
                    .filter(field -> !excludedFieldList.contains(field.getName()))
                    .toList();
        }

        fields = fields.stream()
                .filter(field -> !Modifier.isTransient(field.getModifiers()))
                .toList();

        for (Field field : fields) {
            field.setAccessible(true);

            JSONField annotation = field.getAnnotation(JSONField.class);
            String name = annotation == null
                    ? NamingUtilities.toSnakeCase(field.getName())
                    : annotation.name().isEmpty()
                        ? field.getName()
                        : annotation.name();

            Object value;

            try {
                value = field.get(object);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            if (annotation != null) {
                @NotNull JSONType type = annotation.type();

                if (value == null) {
                    result.put(name, null);
                    continue;
                }

                result.put(name, type == JSONType.UNSPECIFIED
                        ? handle(field.getType(), value, context)
                        : type.handle(this, value, context)
                );

                continue;
            }

            result.put(name, handle(field.getType(), value, context));
        }

        return result;
    }

    private Object handle(Class<?> fieldType, Object value, MappingContext context) {
        return TypeHandlers.getTypeHandler(fieldType).handle(this, value, context);
    }
}
