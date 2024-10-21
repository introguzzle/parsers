package ru.introguzzle.parser.json.mapping.type;

import ru.introguzzle.parser.json.mapping.Mapper;
import ru.introguzzle.parser.json.mapping.context.MappingContext;

public interface TypeHandler {
    Object handle(Mapper mapper, Object value, MappingContext context);
}
