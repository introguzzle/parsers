package ru.introguzzle.parser.json.visitor;

import ru.introguzzle.parser.json.entity.JSONArray;

public interface JSONArrayVisitor {
    void visit(JSONArray array);
}
