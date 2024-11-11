package ru.introguzzle.parsers.xml.mapping.serialization;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.mapping.ReadingMapper;
import ru.introguzzle.parsers.xml.entity.XMLElement;

public interface XMLElementMapper extends ReadingMapper<XMLElementMapper, Bindable> {
    @NotNull XMLElement toElement(@NotNull String name, @NotNull Object object);
}
