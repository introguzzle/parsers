package ru.introguzzle.parsers.json.mapping;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.json.entity.JSONObject;

public interface JSONObjectConvertable {
    @NotNull JSONObject toJSONObject();
}
