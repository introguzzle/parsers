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
import ru.introguzzle.parsers.xml.meta.Encoding;
import ru.introguzzle.parsers.xml.meta.Version;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

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

    @Serial
    private static final long serialVersionUID = -8753510048552424614L;

    private final Version version;
    private final Encoding encoding;
    private final XMLElement root;

    @Override
    public String toXMLString() {
        return "<?xml version=\"" +
                version.getValue() +
                "\" encoding=\"" +
                encoding.getValue() +
                "\"?>\n" +
                root.toXMLString();
    }

    @Override
    public String toXMLStringCompact() {
        return "<?xml version=\"" +
                version.getValue() +
                "\" encoding=\"" +
                encoding.getValue() +
                "\"?>" +
                root.toXMLStringCompact();
    }

    @Override
    public String toString() {
        return toXMLStringCompact();
    }

    @Override
    public JSONObject toJSONObject() {
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

    @Override
    public XMLDocument toXMLDocumentWithMetadata() {
        return null;
    }
}
