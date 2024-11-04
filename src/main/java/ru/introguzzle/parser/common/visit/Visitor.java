package ru.introguzzle.parser.common.visit;

public interface Visitor<T> {
    void visit(T element);
}
