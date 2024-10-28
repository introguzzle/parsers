package ru.introguzzle.parser.xml.visitor;

import ru.introguzzle.parser.xml.XMLDocument;

public interface XMLDocumentVisitor {
    void visit(XMLDocument document);
}
