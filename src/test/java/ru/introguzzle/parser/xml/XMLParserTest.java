package ru.introguzzle.parser.xml;

import org.junit.Test;
import static org.junit.Assert.*;

public class XMLParserTest {
    private Parser parser = new XMLParser();

    @Test
    public void test() {
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

        var tokens = parser.getTokenizer().tokenize(string);
        XMLDocument document = parser.parse(string);
        System.out.println();
    }
}