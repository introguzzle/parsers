package ru.introguzzle.parsers.env;

import ru.introguzzle.parsers.common.util.DelegatingMap;
import java.util.Map;

public final class Environment extends DelegatingMap<String, String> {
    public Environment() {
        super();
    }

    public Environment(Map<? extends String, ? extends String> m) {
        super(m);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Environment{");
        forEach((k, v) -> builder.append("\t").append(k).append("=").append(v).append("\n"));

        return builder.append("}").toString();
    }
}
