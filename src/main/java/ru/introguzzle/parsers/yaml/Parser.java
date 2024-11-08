package ru.introguzzle.parsers.yaml;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.parse.BaseParser;

public interface Parser extends BaseParser<YAMLDocument> {
    @NotNull YAMLDocument parse(@NotNull String data);
}
