package ru.introguzzle.parser.xml.visitor;

import ru.introguzzle.parser.xml.XMLElement;

public interface XMLElementVisitor {
    void visit(XMLElement element);
}
