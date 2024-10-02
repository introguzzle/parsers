package ru.introguzzle.jsonparser.mapping.type;

import ru.introguzzle.jsonparser.mapping.Mapper;
import ru.introguzzle.jsonparser.mapping.context.MappingContext;

public interface TypeHandler {
    Object handle(Mapper mapper, Object value, MappingContext context);
}
