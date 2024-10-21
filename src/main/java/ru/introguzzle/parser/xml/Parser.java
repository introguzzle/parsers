package ru.introguzzle.parser.xml;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class Parser {
    protected Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    protected Tokenizer tokenizer = new Tokenizer();

    public final Parser setExecutor(Executor executor) {
        this.executor = executor;
        return this;
    }

    public final Parser setTokenizer(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        return this;
    }

    public final Tokenizer getTokenizer() {
        return tokenizer;
    }

    public abstract @Nullable XMLDocument parse(@Nullable String data);
    public final @NotNull CompletableFuture<XMLDocument> parseAsync(@Nullable String data) {
        return CompletableFuture.supplyAsync(() -> parse(data), executor);
    }
}
