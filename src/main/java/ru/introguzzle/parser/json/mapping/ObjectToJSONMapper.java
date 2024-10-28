package ru.introguzzle.parser.json.mapping;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parser.common.convert.NameConverter;
import ru.introguzzle.parser.common.utilities.NamingUtilities;
import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.json.mapping.context.MappingContext;

/**
 * Class for mapping POJO into {@code JSONObject}
 * @see JSONObject
 */
public interface ObjectToJSONMapper {
    /**
     *
     * @param object POJO
     * @param context mapping context
     * @return JSON object
     */
    JSONObject toJSONObject(@NotNull Object object, MappingContext context);

    /**
     *
     * @param object POJO
     * @param context mapping context
     * @return JSON object
     *
     * Note: not a part of public API
     */
    Object toJSONObjectRecursive(@Nullable Object object, MappingContext context);

    default NameConverter getNameConverter() {
        return NamingUtilities::toSnakeCase;
    }

    default String convert(String name) {
        return getNameConverter().apply(name);
    }
}
