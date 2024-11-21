package ru.introguzzle.parsers.common.visit;

public interface StatefulVisitor<T, R> extends Visitor<T> {
    @Override
    void visit(T element);

    R result();
}
