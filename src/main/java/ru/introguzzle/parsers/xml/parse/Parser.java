package ru.introguzzle.parsers.xml.parse;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.parse.BaseParser;
import ru.introguzzle.parsers.xml.entity.XMLDocument;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Abstract base class for XML parsers.
 * This class provides a common structure for parsers that utilize a {@link Tokenizer}
 * to convert XML data into structured tokens. It also supports asynchronous parsing using an
 * {@link Executor}.
 */
public abstract class Parser implements Serializable, BaseParser<XMLDocument> {

    @Serial
    private static final long serialVersionUID = 6386722082362042387L;

    public static Parser newParser() {
        return new XMLParser();
    }

    /**
     * The executor responsible for running asynchronous parsing tasks.
     * Default is a fixed thread pool based on the number of available processors.
     */
    protected Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * The tokenizer that breaks down XML data into tokens.
     * By default, it uses the {@link Tokenizer} class.
     */
    protected Tokenizer tokenizer = new Tokenizer();

    /**
     * Sets a executor for handling asynchronous tasks.
     *
     * @param executor the custom {@link Executor} to be used.
     * @return the current {@link Parser} instance for chaining.
     */
    public final Parser setExecutor(@NotNull Executor executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Sets a tokenizer for parsing XML data.
     *
     * @param tokenizer the {@link Tokenizer} to be used for parsing.
     * @return the current {@link Parser} instance for chaining.
     */
    public final Parser setTokenizer(@NotNull Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        return this;
    }

    /**
     * Returns the current tokenizer used by this parser.
     *
     * @return the {@link Tokenizer} instance.
     */
    public final @NotNull Tokenizer getTokenizer() {
        return tokenizer;
    }

    /**
     * Synchronously parses the given XML data into an {@link XMLDocument}.
     * This method is to be implemented by subclasses, as the parsing logic depends on the
     * specific XML format.
     *
     * @param data the XML data as a {@link String}, or {@code null} if no data is provided.
     * @return an {@link XMLDocument} representing the parsed XML, or {@code null} if parsing fails.
     */
    @Override
    public abstract @NotNull XMLDocument parse(@NotNull String data);

    /**
     * Asynchronously parses the given XML data into an {@link XMLDocument}.
     * This method runs the {@link #parse(String)} method asynchronously using the provided
     * executor.
     *
     * @param data the XML data as a {@link String}, or {@code null} if no data is provided.
     * @return a {@link CompletableFuture} that will contain the parsed {@link XMLDocument}.
     */
    public final @NotNull CompletableFuture<XMLDocument> parseAsync(@NotNull String data) {
        return CompletableFuture.supplyAsync(() -> parse(data), executor);
    }
}
