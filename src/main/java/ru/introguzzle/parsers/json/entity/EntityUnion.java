package ru.introguzzle.parsers.json.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
enum EntityUnion {
    OBJECT(JSONObject.class.getSimpleName()),
    ARRAY(JSONArray.class.getSimpleName()),
    ;

    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
