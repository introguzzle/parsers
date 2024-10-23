package ru.introguzzle.parser.xml;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parser.common.Converter;
import ru.introguzzle.parser.common.ConverterFactory;
import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.json.mapping.JSONObjectConvertable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public record XMLDocument(@NotNull Version version,
                          @NotNull Encoding encoding,
                          @NotNull XMLElement root)
        implements Serializable, JSONObjectConvertable, XMLStringConvertable {
    public static final Converter<XMLDocument, JSONObject> CONVERTER
            = ConverterFactory.getDefaultFactory().getXMLDocumentToJSONConverter();

    @Serial
    private static final long serialVersionUID = -8753510048552424614L;

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
        return toXMLString();
    }

    @Override
    public JSONObject toJSONObject() {
        return CONVERTER.convert(this);
    }

    public JSONObject toJSONObjectWithMetadata() {
        return CONVERTER.convertWithMetadata(this);
    }

    public @NotNull Version getVersion() {
        return version;
    }

    public @NotNull Encoding getEncoding() {
        return encoding;
    }

    public @NotNull XMLElement getRoot() {
        return root;
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
}
