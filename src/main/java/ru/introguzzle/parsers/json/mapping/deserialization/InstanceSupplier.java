package ru.introguzzle.parsers.json.mapping.deserialization;

import ru.introguzzle.parsers.json.entity.JSONObject;

public interface InstanceSupplier {


    <T> T get(JSONObject object, Class<T> type);
}
