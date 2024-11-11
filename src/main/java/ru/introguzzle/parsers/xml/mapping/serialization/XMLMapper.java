package ru.introguzzle.parsers.xml.mapping.serialization;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.mapping.ReadingMapper;
import ru.introguzzle.parsers.xml.entity.XMLDocument;

public interface XMLMapper extends ReadingMapper<XMLMapper, Bindable> {
    @NotNull XMLDocument toXMLDocument(@NotNull Object object);
    @NotNull XMLElementMapper getElementMapper();
}
