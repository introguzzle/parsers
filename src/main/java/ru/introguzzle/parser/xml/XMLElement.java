package ru.introguzzle.parser.xml;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parser.common.convert.ConverterFactory;
import ru.introguzzle.parser.common.convert.Converter;
import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.json.mapping.JSONObjectConvertable;
import ru.introguzzle.parser.xml.token.CharacterDataToken;
import ru.introguzzle.parser.xml.visitor.XMLElementVisitor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Getter
@EqualsAndHashCode
public final class XMLElement implements Serializable, JSONObjectConvertable,
        XMLStringConvertable, Consumer<XMLElementVisitor> {

    public static final @NotNull ConverterFactory FACTORY = ConverterFactory.getFactory();
    public static final Converter<XMLElement, JSONObject> CONVERTER =
            FACTORY.getXMLElementToJSONConverter();

    @Serial
    private static final long serialVersionUID = 5578976260620682139L;
    private static final int INITIAL_LEVEL = 0;
    public static final String TAB = "\t";

    private final String name;

    private final List<XMLAttribute> attributes = new ArrayList<>();

    private final List<XMLElement> children = new ArrayList<>();

    @Setter
    private String text;
    @Setter
    private String characterData;

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

        if (characterData != null && !characterData.isEmpty()) {
            xml.append(indent).append(CharacterDataToken.HEAD)
                    .append(newLine)
                    .append(indent).append(TAB).append(characterData)
                    .append(newLine)
                    .append(indent).append(CharacterDataToken.TAIL)
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
    public void accept(XMLElementVisitor visitor) {
        visitor.visit(this);
    }
}
