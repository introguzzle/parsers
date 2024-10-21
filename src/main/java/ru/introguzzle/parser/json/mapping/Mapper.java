package ru.introguzzle.parser.json.mapping;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.json.mapping.context.MappingContext;

public interface Mapper {
    JSONObject toJSONObject(@NotNull Object object, MappingContext context);
    Object map(@Nullable Object object, MappingContext context);
}
