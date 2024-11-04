package ru.introguzzle.parser.common.inject;

public interface MethodInjector<S, T> {
    S getSource();
    void inject(String name, Class<? extends T> target) throws InjectException;
    void uninject(Class<? extends T> targetType) throws InjectException;
}
