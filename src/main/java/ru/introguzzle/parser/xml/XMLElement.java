package ru.introguzzle.parser.xml;

import java.util.ArrayList;
import java.util.List;

public final class XMLElement {
    private final String name;
    private final List<XMLAttribute> attributes = new ArrayList<>();
    private final List<XMLElement> children = new ArrayList<>();
    private String text;

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

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return toXMLString();
    }

    public String toXMLString() {
        StringBuilder xml = new StringBuilder();
        xml.append("<").append(name);
        for (XMLAttribute attribute : attributes) {
            xml.append(" ").append(attribute);
        }

        xml.append(">");

        if (text != null) {
            xml.append(text);
        }

        for (XMLElement child : children) {
            xml.append(child.toXMLString());
        }

        xml.append("</").append(name).append(">");
        return xml.toString();
    }
}
