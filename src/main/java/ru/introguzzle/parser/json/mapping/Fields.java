package ru.introguzzle.parser.json.mapping;

import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;
import ru.introguzzle.parser.common.utilities.ReflectionUtilities;
import ru.introguzzle.parser.json.entity.annotation.JSONEntity;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@UtilityClass
@ExtensionMethod({ReflectionUtilities.class, Fields.Extensions.class})
public final class Fields {
    @UtilityClass
    @SuppressWarnings("unused")
    public static final class Extensions {
        public static boolean isTransient(Field field) {
            return Modifier.isTransient(field.getModifiers());
        }

        public static boolean isVolatile(Field field) {
            return Modifier.isVolatile(field.getModifiers());
        }

        public static boolean isStatic(Field field) {
            return Modifier.isStatic(field.getModifiers());
        }

        public static Object getValue(Field field, Object instance) {
            try {
                return field.get(instance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public static void setValue(Field field, Object instance, Object value) {
            try {
                field.set(instance, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public static <T extends Annotation> Optional<T> getAnnotationAsOptional(Field field, Class<T> annotationType) {
            return Optional.ofNullable(field.getAnnotation(annotationType));
        }
    }

    private static final Map<Class<?>, List<Field>> CLASS_FIELDS_CACHE =
            new ConcurrentHashMap<>();

    public static List<Field> getCached(Class<?> type) {
        synchronized (Fields.class) {
            return CLASS_FIELDS_CACHE.computeIfAbsent(type, Fields::get);
        }
    }

    private static List<Field> get(Class<?> type) {
        JSONEntity entityAnnotation = type.getAnnotation(JSONEntity.class);
        List<Field> fields = type.getAllFields();
        // Remove excluded fields
        if (entityAnnotation != null) {
            String[] excludedFields = entityAnnotation.excluded();
            List<String> excludedFieldList = Arrays.asList(excludedFields);
            fields = fields.stream()
                    .filter(field -> !excludedFieldList.contains(field.getName()))
                    .toList();
        }

        // Skip non-serializable fields
        fields = fields.stream()
                .filter(f -> !f.isTransient())
                .filter(f -> !f.isStatic())
                .filter(f -> !f.isVolatile())
                .toList();

        return fields;
    }
}
