package ru.introguzzle.parsers.common.function;

import java.util.Objects;
import java.util.function.Consumer;

public interface ThrowingConsumer<T> {
    void accept(T t) throws Throwable;

    default Consumer<T> toConsumer() {
        return t -> {
            try {
                accept(t);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    default Consumer<T> toConsumer(Transformer<? extends RuntimeException> handler) {
        return t -> {
            try {
                accept(t);
            } catch (Throwable e) {
                throw handler.apply(e);
            }
        };
    }

    default Consumer<T> toConsumer(Handler handler) {
        return t -> {
            try {
                accept(t);
            } catch (Throwable e) {
                handler.accept(e);
            }
        };
    }

    default ThrowingConsumer<T> andThen(ThrowingConsumer<? super T> after) {
        return t -> {
            accept(t);
            Objects.requireNonNull(after).accept(t);
        };
    }
}
