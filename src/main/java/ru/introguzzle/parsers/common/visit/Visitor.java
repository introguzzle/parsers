package ru.introguzzle.parsers.common.visit;

public interface Visitor<T> {
    void visit(T element);
}
