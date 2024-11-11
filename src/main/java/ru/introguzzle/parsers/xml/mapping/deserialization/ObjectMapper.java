package ru.introguzzle.parsers.xml.mapping.deserialization;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.mapping.WritingMapper;
import ru.introguzzle.parsers.xml.entity.XMLDocument;

public interface ObjectMapper extends WritingMapper<ObjectMapper> {
    <T> @NotNull T toObject(@NotNull XMLDocument document, @NotNull Class<T> type);
}
