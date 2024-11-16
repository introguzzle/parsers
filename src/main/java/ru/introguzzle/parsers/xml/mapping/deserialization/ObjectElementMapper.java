package ru.introguzzle.parsers.xml.mapping.deserialization;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.mapping.WritingMapper;
import ru.introguzzle.parsers.common.mapping.deserialization.InstanceSupplier;
import ru.introguzzle.parsers.xml.entity.XMLElement;

import java.lang.reflect.Type;

public interface ObjectElementMapper extends WritingMapper<ObjectElementMapper> {
    @NotNull Object toObject(@NotNull XMLElement root, @NotNull Type type);
    @NotNull InstanceSupplier<XMLElement> getInstanceSupplier();
}
