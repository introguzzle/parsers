package ru.introguzzle.parsers.env;

import ru.introguzzle.parsers.common.util.DelegatingMap;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class that represents .env file as {@code Map<String, String>}.
 * Can be created by {@link Parser} by parsing file with lines of pairs of keys and values separated by '='
 * @see Parser
 */
public class Environment extends DelegatingMap<String, String> {
    /**
     * Constructs a new empty Environment with {@link LinkedHashMap} as delegate
     */
    Environment() {
        super();
    }

    /**
     * Constructs a new Environment with specified {@code m} as map to delegate
     * @param m delegate
     */
    Environment(Map<? extends String, ? extends String> m) {
        super(m);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Environment{");
        forEach((k, v) -> builder.append("\t").append(k).append("=").append(v).append("\n"));

        return builder.append("}").toString();
    }
}
