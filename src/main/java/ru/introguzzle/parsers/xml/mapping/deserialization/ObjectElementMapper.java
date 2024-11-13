package ru.introguzzle.parsers.xml.mapping.deserialization;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.mapping.WritingMapper;
import ru.introguzzle.parsers.common.mapping.deserialization.InstanceSupplier;
import ru.introguzzle.parsers.xml.entity.XMLElement;

public interface ObjectElementMapper extends WritingMapper<ObjectElementMapper> {
    <T> @NotNull T toObject(@NotNull XMLElement root, @NotNull Class<T> type);
    @NotNull InstanceSupplier<XMLElement> getInstanceSupplier();
}
