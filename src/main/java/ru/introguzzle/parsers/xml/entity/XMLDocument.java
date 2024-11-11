package ru.introguzzle.parsers.xml.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.convert.ConverterFactory;
import ru.introguzzle.parsers.common.convert.Converter;
import ru.introguzzle.parsers.common.visit.Visitable;
import ru.introguzzle.parsers.common.visit.Visitor;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.json.mapping.JSONObjectConvertable;
import ru.introguzzle.parsers.xml.mapping.XMLMappingException;
import ru.introguzzle.parsers.xml.mapping.deserialization.ObjectMapper;
import ru.introguzzle.parsers.xml.meta.Encoding;
import ru.introguzzle.parsers.xml.meta.Version;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public class XMLDocument implements Serializable, JSONObjectConvertable, XMLDocumentConvertable,
        XMLStringConvertable, Visitable<XMLDocument, Visitor<XMLDocument>> {
    public static final @NotNull ConverterFactory FACTORY;
    public static final Converter<XMLDocument, JSONObject> CONVERTER;

    static {
        FACTORY = ConverterFactory.getFactory();
        CONVERTER = FACTORY.getXMLDocumentToJSONConverter();
    }

    private static final Map<Class<?>, ObjectMapper> MAPPERS = new HashMap<>();

    @Serial
    private static final long serialVersionUID = -8753510048552424614L;

    private final Version version;
    private final Encoding encoding;
    private final XMLElement root;

    @Override
    public String toXMLString() {
        return "<?xml version=\"" + version.getValue() + "\" encoding=\"" + encoding.getValue() + "\"?>\n" + root.toXMLString();
    }

    @Override
    public String toXMLStringCompact() {
        return "<?xml version=\"" + version.getValue() + "\" encoding=\"" + encoding.getValue() + "\"?>" + root.toXMLStringCompact();
    }

    @Override
    public String toString() {
        return toXMLStringCompact();
    }

    @Override
    public @NotNull JSONObject toJSONObject() {
        return CONVERTER.convert(this);
    }

    public JSONObject toJSONObjectWithMetadata() {
        return CONVERTER.convertWithMetadata(this);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof XMLDocument document)) return false;
        return version == document.version && encoding == document.encoding
                && Objects.equals(root, document.root);

    }

    @Override
    public int hashCode() {
        return Objects.hash(version, encoding, root);
    }

    @Override
    public XMLDocument toXMLDocument() {
        return this;
    }

    public static void bindTo(Class<?> type, ObjectMapper mapper) {
        MAPPERS.put(type, mapper);
    }

    public static void bindTo(Class<?>[] types, ObjectMapper mapper) {
        for (Class<?> type : types) {
            bindTo(type, mapper);
        }
    }

    public static void bindTo(Set<Class<?>> types, ObjectMapper mapper) {
        for (Class<?> type : types) {
            bindTo(type, mapper);
        }
    }

    public static void unbind(Class<?> type) {
        MAPPERS.remove(type);
    }

    public static void unbindAll() {
        MAPPERS.clear();
    }

    public <T> T toObject(Class<T> type) {
        ObjectMapper associate = MAPPERS.get(type);
        if (associate == null) {
            throw new XMLMappingException("No mapper present for " + type);
        }

        return associate.toObject(this, type);
    }

    @Override
    public XMLDocument toXMLDocumentWithMetadata() {
        return null;
    }
}
