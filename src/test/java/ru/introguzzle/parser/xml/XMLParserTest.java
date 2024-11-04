package ru.introguzzle.parser.xml;

import org.junit.Test;
import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.xml.entity.XMLDocument;
import ru.introguzzle.parser.xml.entity.XMLElement;
import ru.introguzzle.parser.xml.parse.Parser;
import ru.introguzzle.parser.xml.parse.XMLParseException;
import ru.introguzzle.parser.xml.parse.XMLParser;

import static org.junit.Assert.*;

public class XMLParserTest {
    private final Parser parser = new XMLParser();

    @Test
    public void test1() {
        String string = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!-- Это комментарий в XML -->\n" +
                "<library xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"library-schema.xsd\">\n" +
                "\n" +
                "    <!-- Пример простой пустой самозакрывающейся тега -->\n" +
                "    <metadata/>\n" +
                "\n" +
                "    <!-- Пример элементов с атрибутами -->\n" +
                "    <book id=\"1\" genre=\"fiction\" availability=\"available\">\n" +
                "        <title>War and Peace</title>\n" +
                "        <author>Leo Tolstoy</author>\n" +
                "        <published>\n" +
                "            <year>1869</year>\n" +
                "            <publisher>Penguin Books</publisher>\n" +
                "        </published>\n" +
                "        <price currency=\"USD\">39.99</price>\n" +
                "\n" +
                "        <!-- Пример вложенных элементов с CDATA -->\n" +
                "        <description>\n" +
                "            <![CDATA[ This is a famous novel about Napoleon's invasion of Russia. ]]>\n" +
                "        </description>\n" +
                "    </book>\n" +
                "\n" +
                "    <!-- Пример списка элементов -->\n" +
                "    <book id=\"2\" genre=\"science\" availability=\"out_of_stock\">\n" +
                "        <title>A Brief History of Time</title>\n" +
                "        <author>Stephen Hawking</author>\n" +
                "        <published>\n" +
                "            <year>1988</year>\n" +
                "            <publisher>Bantam Books</publisher>\n" +
                "        </published>\n" +
                "        <price currency=\"GBP\">15.50</price>\n" +
                "    </book>\n" +
                "\n" +
                "    <!-- Пример элементов с смешанным содержимым -->\n" +
                "    <magazine id=\"3\" issue=\"42\">\n" +
                "        <title>National Geographic</title>\n" +
                "        <date>2024-01-01</date>\n" +
                "        <summary>\n" +
                "            This issue covers topics on\n" +
                "            <highlight>climate change</highlight>,\n" +
                "            <highlight>wildlife conservation</highlight>,\n" +
                "            and\n" +
                "            <highlight>space exploration</highlight>.\n" +
                "        </summary>\n" +
                "    </magazine>\n" +
                "\n" +
                "    <!-- Пример элемента с пространствами имен -->\n" +
                "    <xsi:book xmlns:xsi=\"http://www.example.com/xsi\" id=\"4\" genre=\"fantasy\">\n" +
                "        <title>The Hobbit</title>\n" +
                "        <author>J.R.R. Tolkien</author>\n" +
                "        <published>\n" +
                "            <year>1937</year>\n" +
                "            <publisher>George Allen & Unwin</publisher>\n" +
                "        </published>\n" +
                "        <price currency=\"USD\">25.00</price>\n" +
                "    </xsi:book>\n" +
                "\n" +
                "</library>\n";

        System.out.println("########## ORIGINAL XML ###############\n");
        System.out.println(string);

        XMLDocument document = parser.parse(string);
        assertNotNull(document);
        JSONObject json = document.toJSONObject();
        assertNotNull(json);

        System.out.println("########### CONVERTED TO JSON #############\n");
        System.out.println(json.toJSONString());

        System.out.println("########### CONVERTED TO JSON AND FLATTENED ##############\n");
        JSONObject flattened = json.flatten();
        System.out.println(flattened.toJSONString());

        System.out.println("########### JSON TO XML ###############\n");
        XMLDocument fromJson = json.toXMLDocument();
        System.out.println(fromJson.toXMLString());

        System.out.println("########### FLATTENED JSON CONVERTED TO XML ###############\n");
        XMLDocument fromFlattened = flattened.toXMLDocument();
        System.out.println(fromFlattened.toXMLString());
    }

    @Test
    public void test2() {
        String string = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <object></object>";
        XMLDocument document = parser.parse(string);
        assertNotNull(document);
        XMLElement root = document.getRoot();

        assertEquals(root.getChildren().size(), 0);
        assertEquals(root.getName(), "object");
        assertEquals(root.getCharacterData(), "");
        assertEquals(root.getText(), "");

        System.out.println(document.toXMLString());
    }

    @Test
    public void test3() {
        String string = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<object attr=\"attrValue\"> someText" +
                    "<![CDATA[ <shouldBeIgnored> </shouldBeIgnored> ]]>" +
                "</object>";

        XMLDocument document = parser.parse(string);
        assertNotNull(document);
        JSONObject json = document.toJSONObjectWithMetadata();
        XMLDocument fromJson = json.toXMLDocumentWithMetadata();

        System.out.println("############ ORIGINAL #############\n");
        System.out.println(document.toXMLString());

        System.out.println("############ CONVERTED WITH METADATA TO JSON #############\n");
        System.out.println(json.toJSONString());

        System.out.println("############ CONVERTED WITH METADATA FROM JSON TO XML #############\n");
        System.out.println(fromJson.toXMLString());

        assertEquals(document, fromJson);
    }

    @Test
    public void test4() {
        String string = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<object attr=\"attrValue\"> someText" +
                "<![CDATA[ <shouldBeIgnored> </shouldBeIgnored> ]]>" +
                "</object>";

        XMLDocument document = parser.parse(string);
        assertNotNull(document);
        JSONObject json = document.toJSONObject();
        XMLDocument fromJson = json.toXMLDocument();

        System.out.println("############ ORIGINAL #############\n");
        System.out.println(document.toXMLString());

        System.out.println("############ CONVERTED TO JSON #############\n");
        System.out.println(json.toJSONString());

        System.out.println("############ CONVERTED FROM JSON TO XML #############\n");
        System.out.println(fromJson.toXMLString());
    }

    @Test
    public void test5() {
        String string = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<integer> " +
                "5" +
                "<inner> 10 </inner>" +
                "</integer>";

        XMLDocument document = parser.parse(string);
        assertNotNull(document);

        System.out.println(document.toJSONObjectWithMetadata().toJSONString());
        System.out.println("\n");
        System.out.println(document.toJSONObjectWithMetadata()
                .toXMLDocumentWithMetadata()
                .toXMLString());

        System.out.println("FLATTEN\n");
        System.out.println(document.toJSONObjectWithMetadata().flatten("@text").toJSONString());
    }

    @Test(expected = XMLParseException.class)
    public void test_missing_declaration() {
        String string = "<library xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"library-schema.xsd\">";
        parser.parse(string);
    }
}