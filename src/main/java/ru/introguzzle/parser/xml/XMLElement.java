package ru.introguzzle.parser.xml;

import ru.introguzzle.parser.common.Converter;
import ru.introguzzle.parser.common.XMLDocumentToJSONConverter;
import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.json.mapping.JSONObjectConvertable;
import ru.introguzzle.parser.xml.token.CDataToken;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class XMLElement implements Serializable, JSONObjectConvertable, XMLStringConvertable {
    public static final Converter<XMLElement, JSONObject> CONVERTER;
    static {
        CONVERTER = XMLDocument.CONVERTER instanceof XMLDocumentToJSONConverter
                ? ((XMLDocumentToJSONConverter) XMLDocument.CONVERTER).getElementConverter()
                : null;

        assert CONVERTER != null;
    }

    @Serial
    private static final long serialVersionUID = 5578976260620682139L;
    private static final int INITIAL_LEVEL = 0;
    public static final String TAB = "\t";

    private String name;
    private final List<XMLAttribute> attributes = new ArrayList<>();
    private final List<XMLElement> children = new ArrayList<>();
    private String text;
    private String cData;

    public XMLElement() {

    }

    public XMLElement(String name) {
        this.name = name;
    }

    public void addAttribute(XMLAttribute attribute) {
        attributes.add(attribute);
    }

    public void addAttributes(List<XMLAttribute> attributes) {
        this.attributes.addAll(attributes);
    }

    public void addChild(XMLElement child) {
        children.add(child);
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCData(String cData) {
        this.cData = cData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<XMLAttribute> getAttributes() {
        return attributes;
    }

    public List<XMLElement> getChildren() {
        return children;
    }

    public String getText() {
        return text;
    }

    public String getCData() {
        return cData;
    }

    @Override
    public String toString() {
        return toXMLString();
    }

    @Override
    public String toXMLString() {
        return toXMLString(INITIAL_LEVEL, false);
    }

    @Override
    public String toXMLStringCompact() {
        return toXMLString(INITIAL_LEVEL, true);
    }

    private String toXMLString(int level, boolean compact) {
        StringBuilder xml = new StringBuilder();
        String newLine = compact ? "" : System.lineSeparator();
        String indent = compact ? "" : TAB.repeat(level);

        xml.append(indent).append("<").append(name);
        for (XMLAttribute attribute : attributes) {
            xml.append(" ").append(attribute);
        }

        xml.append(">").append(newLine);

        if (text != null && !text.isEmpty()) {
            xml.append(indent).append(TAB).append(text).append(newLine);
        }

        if (cData != null && !cData.isEmpty()) {
            xml.append(indent).append(CDataToken.HEAD)
                    .append(newLine)
                    .append(indent).append(TAB).append(cData)
                    .append(newLine)
                    .append(indent).append(CDataToken.TAIL)
                    .append(newLine);
        }

        for (XMLElement child : children) {
            xml.append(child.toXMLString(level + 1, compact));
        }

        xml.append(indent).append("</").append(name).append(">").append(newLine);
        return xml.toString();
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
        if (!(object instanceof XMLElement element)) return false;
        return Objects.equals(name, element.name)
                && Objects.equals(attributes, element.attributes)
                && Objects.equals(children, element.children)
                && Objects.equals(text, element.text)
                && Objects.equals(cData, element.cData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, attributes, children, text, cData);
    }
}
