package ru.introguzzle.parser.json.mapping.deserialization;

import ru.introguzzle.parser.json.entity.JSONObject;

public interface InstanceSupplier {
    <T> T get(JSONObject object, Class<T> type);
}
