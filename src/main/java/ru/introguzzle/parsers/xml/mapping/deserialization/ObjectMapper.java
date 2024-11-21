package ru.introguzzle.parsers.xml.mapping.deserialization;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.convert.NameConverter;
import ru.introguzzle.parsers.common.mapping.WritingMapper;
import ru.introguzzle.parsers.common.mapping.deserialization.InstanceSupplier;
import ru.introguzzle.parsers.common.util.NamingUtilities;
import ru.introguzzle.parsers.xml.entity.XMLDocument;
import ru.introguzzle.parsers.xml.entity.XMLElement;
import ru.introguzzle.parsers.xml.mapping.XMLFieldNameConverter;

import java.lang.reflect.Type;

public interface ObjectMapper extends WritingMapper<ObjectMapper> {
    @NotNull Object toObject(@NotNull XMLDocument document, @NotNull Type type);

    @NotNull
    @SuppressWarnings("unchecked")
    default <T> T toObject(@NotNull XMLDocument document, @NotNull Class<? extends T> type) {
        return (T) toObject(document, (Type) type);
    }

    @NotNull InstanceSupplier<XMLElement> getInstanceSupplier();

    static ObjectMapper newMapper() {
        return newMapper(NamingUtilities::toCamelCase);
    }

    static ObjectMapper newMapper(NameConverter nameConverter) {
        return new ObjectMapperImpl(new XMLFieldNameConverter(nameConverter));
    }
}
