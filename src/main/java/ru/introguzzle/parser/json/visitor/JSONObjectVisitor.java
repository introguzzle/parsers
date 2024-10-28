package ru.introguzzle.parser.json.visitor;

import ru.introguzzle.parser.json.entity.JSONObject;

public interface JSONObjectVisitor {
    void visit(JSONObject object);
}
