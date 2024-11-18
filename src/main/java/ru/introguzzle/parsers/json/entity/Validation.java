package ru.introguzzle.parsers.json.entity;

import ru.introguzzle.parsers.config.Configuration;
import ru.introguzzle.parsers.json.mapping.reference.StandardCircularReferenceStrategies.CircularReference;

import java.util.Map;
import java.util.Set;

class Validation {
    private static final Configuration CONFIGURATION;
    private static final boolean VALIDATION_ENABLED;

    /**
     * Permitted classes to be stored in {@link JSONArray} and {@link JSONObject}
     */
    public static final Set<Class<?>> PERMITTED_CLASSES;

    static {
        CONFIGURATION = Configuration.instance();
        VALIDATION_ENABLED = CONFIGURATION.getEntityValidationEnabled().getValue();
        PERMITTED_CLASSES = Set.of(
                Number.class, String.class, JSONObject.class,
                JSONArray.class, Boolean.class, CircularReference.class);
    }

    private static boolean isPermitted(Class<?> type) {
        return PERMITTED_CLASSES.contains(type)
                || PERMITTED_CLASSES.stream().anyMatch(c -> c.isAssignableFrom(type));
    }

    /**
     * Checks if the specified type is permitted to be stored in the {@link JSONArray} or {@link JSONObject}
     *
     * @param object the object to check
     */
    public static <T> T requirePermittedType(T object, EntityUnion union) {
        if (!VALIDATION_ENABLED) {
            return object;
        }

        if (object == null) {
            return null;
        }

        Class<?> type = object.getClass();
        if (!isPermitted(type)) {
            throw new IllegalArgumentException(type + " is not permitted in " + union);
        }

        return object;
    }

    public static <T extends Iterable<?>> T requirePermittedType(T iterable) {
        if (!VALIDATION_ENABLED) {
            return iterable;
        }

        for (Object object : iterable) {
            if (object == null) {
                continue;
            }

            requirePermittedType(object, EntityUnion.ARRAY);
        }

        return iterable;
    }

    public static Map<? extends String, ?> requirePermittedType(Map<? extends String, ?> map) {
        if (!VALIDATION_ENABLED) {
            return map;
        }

        for (Map.Entry<? extends String, ?> entry : map.entrySet()) {
            if (entry.getKey() == null) {
                throw new IllegalArgumentException("Null key is not permitted in JSONObject");
            }

            if (entry.getValue() == null) {
                continue;
            }

            requirePermittedType(entry.getValue(), EntityUnion.OBJECT);
        }

        return map;
    }
}
