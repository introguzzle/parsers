package ru.introguzzle.parser.xml;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class XMLElement implements Serializable, JSONObjectConvertable,
        XMLStringConvertable, Consumer<XMLElementVisitor> {

    public static final @NotNull ConverterFactory FACTORY = ConverterFactory.getFactory();
    public static final Converter<XMLElement, JSONObject> CONVERTER =
            FACTORY.getXMLElementToJSONConverter();

    @Serial
    private static final long serialVersionUID = 5578976260620682139L;
    private static final int INITIAL_LEVEL = 0;
    public static final String TAB = "\t";

    private final String name;
    private final Map<String, XMLAttribute> attributes = new LinkedHashMap<>();
    private final Map<String, XMLElement> children = new LinkedHashMap<>();

    @Setter
    private String text;
    @Setter
    private String characterData;

    public void addAttribute(XMLAttribute attribute) {
        this.attributes.put(attribute.name(), attribute);
    }

    public void addAttributes(List<XMLAttribute> attributes) {
        attributes.forEach(this::addAttribute);
    }

    public void addChild(XMLElement child) {
        children.put(child.getName(), child);
    }

    public XMLElement getChild(String name) {
        return children.get(name);
    }

    @Override
    public String toString() {
        return toXMLStringCompact();
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

        for (XMLAttribute attribute : getAttributes()) {
            xml.append(" ").append(attribute);
        }

        boolean hasContent = (text != null && !text.isEmpty()) ||
                (characterData != null && !characterData.isEmpty()) ||
                !children.isEmpty();

        if (!hasContent) {
            xml.append("/>").append(newLine);
        } else {
            xml.append(">").append(newLine);

            // Добавляем текст, если он не пустой
            if (text != null && !text.isEmpty()) {
                xml.append(indent).append(TAB).append(text).append(newLine);
            }

            // Добавляем characterData, если он не пустой
            if (characterData != null && !characterData.isEmpty()) {
                xml.append(indent).append(CharacterDataToken.HEAD)
                        .append(newLine)
                        .append(indent).append(TAB).append(characterData)
                        .append(newLine)
                        .append(indent).append(CharacterDataToken.TAIL)
                        .append(newLine);
            }

            // Добавляем дочерние элементы
            for (XMLElement child : getChildren()) {
                xml.append(child.toXMLString(level + 1, compact));
            }

            // Закрывающий тег для элемента с содержимым
            xml.append(indent).append("</").append(name).append(">").append(newLine);
        }

        return xml.toString();
    }

    public Collection<XMLAttribute> getAttributes() {
        return attributes.values();
    }

    public Collection<XMLElement> getChildren() {
        return children.values();
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
