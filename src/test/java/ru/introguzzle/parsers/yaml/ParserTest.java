package ru.introguzzle.parsers.yaml;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public abstract class ParserTest {
    public abstract Parser getParser();

    @Test
    public void test() {
        String data = """
                simple_value: "1111111"

                converter:
                  factory:
                    class: value1

                naming:
                  xml_converter:
                    class: value2
                  json_converter:
                    class: value3

                xml:
                  attribute_prefix: "@"
                  default_root_name: "root"
                  text_placeholder: "@text"
                  character_data_placeholder: "@cdata"
                """;

        YAMLDocument document = getParser().parse(data);

        assertEquals(document.get("simple_value", String.class), "1111111");
        assertEquals(document.get("converter.factory.class", String.class), "value1");
        assertEquals(document.get("naming.xml_converter.class", String.class), "value2");
        assertEquals(document.get("naming.json_converter.class", String.class), "value3");
        assertEquals(document.get("xml.attribute_prefix", String.class), "@");
        assertEquals(document.get("xml.default_root_name", String.class), "root");
        assertEquals(document.get("xml.text_placeholder", String.class), "@text");
        assertEquals(document.get("xml.character_data_placeholder", String.class), "@cdata");
    }
}