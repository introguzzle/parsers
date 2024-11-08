package ru.introguzzle.parsers.common.inject;

public interface Binder<S, T> extends MethodInjector<S, T> {
    String getMethod();

    @Override
    void inject(String name, Class<? extends T> target) throws InjectException;

    @SuppressWarnings("UnusedReturnValue")
    default Binder<S, T> inject(Class<? extends T> type) throws InjectException {
        inject(getMethod(), type);
        return this;
    }

    @Override
    S getSource();
}
