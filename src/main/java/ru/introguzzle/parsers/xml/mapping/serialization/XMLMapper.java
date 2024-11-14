package ru.introguzzle.parsers.xml.mapping.serialization;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.convert.NameConverter;
import ru.introguzzle.parsers.common.mapping.ReadingMapper;
import ru.introguzzle.parsers.common.util.NamingUtilities;
import ru.introguzzle.parsers.xml.entity.XMLDocument;
import ru.introguzzle.parsers.xml.mapping.XMLFieldNameConverter;

public interface XMLMapper extends ReadingMapper<XMLMapper, Bindable> {
    @NotNull XMLDocument toXMLDocument(@NotNull Object object);
    @NotNull XMLElementMapper getElementMapper();

    static XMLMapper newMapper() {
        return newMapper(NamingUtilities::toCamelCase);
    }

    static XMLMapper newMapper(NameConverter nameConverter) {
        return new XMLMapperImpl(new XMLFieldNameConverter(nameConverter));
    }
}
