package ru.introguzzle.parsers.json.entity;

class DeepCopier {
    public JSONObject createDeepCopy(JSONObject original) {
        JSONObject copy = new JSONObject();
        for (var entry : original.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof JSONObject object) {
                copy.put(key, createDeepCopy(object));
            }

            if (value instanceof JSONArray array) {
                copy.put(key, createDeepCopy(array));
            }

            copy.put(key, value);
        }

        return copy;
    }

    public JSONArray createDeepCopy(JSONArray original) {
        JSONArray copy = new JSONArray();
        for (Object item : original) {
            if (item instanceof JSONObject object) {
                copy.add(createDeepCopy(object));
            }

            if (item instanceof JSONArray array) {
                copy.add(createDeepCopy(array));
            }

            copy.add(item);
        }

        return copy;
    }
}
