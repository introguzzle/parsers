package ru.introguzzle.jsonparser.mapping;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.jsonparser.entity.JSONObject;
import ru.introguzzle.jsonparser.mapping.context.MappingContext;

public interface Mapper {
    JSONObject toJSONObject(@NotNull Object object, MappingContext context);
    Object map(@Nullable Object object, MappingContext context);
}
