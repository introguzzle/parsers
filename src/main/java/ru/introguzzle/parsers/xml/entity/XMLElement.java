package ru.introguzzle.parsers.xml.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parsers.common.convert.ConverterFactory;
import ru.introguzzle.parsers.common.convert.Converter;
import ru.introguzzle.parsers.common.util.Nullability;
import ru.introguzzle.parsers.common.visit.Visitable;
import ru.introguzzle.parsers.common.visit.Visitor;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.json.mapping.JSONObjectConvertable;
import ru.introguzzle.parsers.xml.token.CharacterDataToken;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class representing an XML element.
 * <p>
 * Contains a name, attributes, child elements, text, and character data (CDATA).
 * Provides methods for adding attributes and child elements,
 * as well as converting to an XML string and to a {@link JSONObject}.
 * </p>
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class XMLElement implements Serializable, JSONObjectConvertable, XMLElementConvertable,
        XMLStringConvertable, Visitable<XMLElement, Visitor<XMLElement>> {

    /**
     * Converter factory for transforming XML elements.
     */
    public static final @NotNull ConverterFactory FACTORY;

    /**
     * Converter for transforming {@code XMLElement} into {@code JSONObject}.
     */
    public static final Converter<XMLElement, JSONObject> CONVERTER;

    static {
        FACTORY = ConverterFactory.getFactory();
        CONVERTER = FACTORY.getXMLElementToJSONConverter();
    }

    @Serial
    private static final long serialVersionUID = 5578976260620682139L;

    /**
     * The name of the XML element.
     */
    private final String name;

    /**
     * List of attributes of the XML element.
     */
    private final List<XMLAttribute> attributes = new ArrayList<>();

    /**
     * List of child elements of the XML element.
     */
    private final List<XMLElement> children = new ArrayList<>();

    /**
     * Text content of the element.
     */
    @Setter
    private String text;

    /**
     * Character data (CDATA) of the element.
     */
    @Setter
    private String characterData;

    /**
     * Adds an attribute to the element.
     *
     * @param attribute the attribute to add
     */
    public void addAttribute(XMLAttribute attribute) {
        attributes.add(attribute);
    }

    /**
     * Adds a list of attributes to the element.
     *
     * @param attributes the list of attributes to add
     */
    public void addAttributes(List<XMLAttribute> attributes) {
        attributes.forEach(this::addAttribute);
    }

    /**
     * Adds a child element.
     *
     * @param child the child element to add
     */
    public void addChild(XMLElement child) {
        children.add(child);
    }

    /**
     * Checks if the element contains repeating child elements with the same name.
     *
     * @return {@code true} if the element is iterable; {@code false} otherwise
     */
    public boolean isIterable() {
        if (getChildren().isEmpty()) {
            return false;
        }

        XMLElement first = getChildren().getFirst();

        return getChildren()
                .stream()
                .anyMatch(e -> e.name.equals(first.name));
    }

    /**
     * Retrieves a value by name from child elements, attributes, or character data.
     *
     * @param name the name to search for
     * @return the found value or {@code null} if not found
     */
    public @Nullable Object get(@NotNull String name) {
        return Nullability.selectAny(getChild(name), getAttribute(name), getCharacterData());
    }

    /**
     * Retrieves a child element by name and index.
     *
     * @param name  the name of the child element
     * @param index the index of the child element
     * @return the found child element with specified {@code name} and {@code index} or {@code null} if not found
     */
    public @Nullable XMLElement getChild(@NotNull String name, int index) {
        int size = children.size();
        Iterator<XMLElement> iterator = children.iterator();

        for (int i = 0; i < size; i++) {
            XMLElement child = children.get(i);
            if (index == i && child.name.equals(name)) {
                return child;
            }
        }

        return null;
    }

    /**
     * Retrieves the first child element by name.
     *
     * @param name the name of the child element
     * @return the found child element or {@code null} if not found
     */
    public @Nullable XMLElement getChild(@NotNull String name) {
        return getChildren()
                .stream()
                .filter(e -> e.name.equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Retrieves an attribute by name.
     *
     * @param name the name of the attribute
     * @return the found attribute or {@code null} if not found
     */
    public @Nullable XMLAttribute getAttribute(@NotNull String name) {
        for (XMLAttribute attribute : attributes) {
            if (attribute.name().equals(name)) {
                return attribute;
            }
        }
        return null;
    }

    /**
     * Converts the element to a {@link JSONObject} including metadata.
     *
     * @return a {@code JSONObject} representing the XML element with metadata
     */
    public JSONObject toJSONObjectWithMetadata() {
        return CONVERTER.convertWithMetadata(this);
    }

    /**
     * Converts the implementing object into a {@link JSONObject}.
     *
     * @return a {@link JSONObject} representation of the object.
     */
    @Override
    public @NotNull JSONObject toJSONObject() {
        return CONVERTER.convert(this);
    }

    /**
     * Converts this object to an {@link XMLElement}.
     *
     * @return itself
     */
    @Override
    public XMLElement toXMLElement() {
        return this;
    }

    @Override
    public @NotNull String toXMLString() {
        return toXMLString(INITIAL_LEVEL, false);
    }

    @Override
    public @NotNull String toXMLStringCompact() {
        return toXMLString(INITIAL_LEVEL, true);
    }


    /**
     * Initial indentation level when generating XML string.
     */
    private static final int INITIAL_LEVEL = 0;

    /**
     * Tab character for indentation in XML.
     */
    private static final String TAB = "\t";

    /**
     * Converts the element to an XML string with the specified indentation level and compact option.
     *
     * @param level   the indentation level
     * @param compact if {@code true}, output without indentation and line breaks
     * @return the string representation of the element in XML format
     */
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

            if (text != null && !text.isEmpty()) {
                String textIndent = compact ? "" : TAB.repeat(level + 1);
                xml.append(textIndent).append(text).append(newLine);
            }

            if (characterData != null && !characterData.isEmpty()) {
                xml.append(indent).append(CharacterDataToken.HEAD)
                        .append(newLine)
                        .append(indent).append(indent).append(characterData)
                        .append(newLine)
                        .append(indent).append(CharacterDataToken.TAIL)
                        .append(newLine);
            }

            for (XMLElement child : getChildren()) {
                xml.append(child.toXMLString(level + 1, compact));
            }

            xml.append(indent).append("</").append(name).append(">").append(newLine);
        }

        return xml.toString();
    }
}
