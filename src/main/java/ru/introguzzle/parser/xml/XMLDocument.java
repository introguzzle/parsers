package ru.introguzzle.parser.xml;

public record XMLDocument(Version version,
                          Encoding encoding,
                          XMLElement root) {
    public String toXMLString() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"")
                .append(version.getValue())
                .append("\" encoding=\"")
                .append(encoding.getValue())
                .append("\"?>\n");

        return xml.append(root.toXMLString()).toString();
    }

    @Override
    public String toString() {
        return toXMLString();
    }
}
