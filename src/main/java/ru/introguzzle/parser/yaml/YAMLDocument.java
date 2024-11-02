package ru.introguzzle.parser.yaml;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parser.common.UntypedMap;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class YAMLDocument extends UntypedMap {
    public YAMLDocument() {
        super();
    }

    public YAMLDocument(Map<? extends String, ?> map) {
        super(map);
    }

    @Override
    public Object put(String key, Object value) {
        String[] keys = key.split("\\.");
        if (keys.length == 1) {
            return super.put(key, value);
        }

        UntypedMap parent = this;
        for (int i = 0; i < keys.length - 1; i++) {
            Object child = parent.get(keys[i]);
            if (!(child instanceof UntypedMap)) {
                child = new YAMLDocument();
                parent.put(keys[i], child);
            }

            parent = (UntypedMap) child;
        }

        return parent.put(keys[keys.length - 1], value);
    }

    @Override
    public boolean containsValue(Object value) {
        return containsValueRecursive(this, value);
    }

    @SuppressWarnings("unchecked")
    private boolean containsValueRecursive(Map<? extends String, ?> map, Object value) {
        for (Object val : map.values()) {
            if (val.equals(value)) {
                return true;
            } else if (val instanceof Map) {
                if (containsValueRecursive((Map<? extends String, ?>) val, value)) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public void putAll(@NotNull Map<? extends String, ?> map) {
        if (map instanceof YAMLDocument) {
            putAllFromYMLDocument((YAMLDocument) map);
        } else {
            super.putAll(map);
        }
    }

    private void putAllFromYMLDocument(YAMLDocument document) {
        for (var entry : document.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Object get(Object key) {
        String[] keys = ((String) key).split("\\.");
        if (keys.length == 1) {
            return super.get(key);
        }

        Object value = super.get(keys[0]);
        for (int i = 1; i < keys.length; i++) {
            if (value instanceof UntypedMap) {
                value = ((UntypedMap) value).get(keys[i]);
            } else {
                return value;
            }
        }

        return value;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public Object putIfAbsent(String key, Object value) {
        return containsKey(key) ? null : super.putIfAbsent(key, value);
    }

    @Override
    public Object remove(Object key) {
        String[] keys = ((String) key).split("\\.");
        if (keys.length == 1) {
            return super.remove(key);
        }

        UntypedMap parent = this;
        for (int i = 0; i < keys.length - 1; i++) {
            Object value = parent.get(keys[i]);
            if (value instanceof UntypedMap) {
                parent = (UntypedMap) value;
            } else {
                return null;
            }
        }

        return parent.remove(keys[keys.length - 1]);
    }

    @Override
    public Object computeIfAbsent(String key, Function<? super String, ?> mappingFunction) {
        Object value = get(key);
        if (value == null) {
            Object newValue = mappingFunction.apply(key);
            put(key, newValue);
            return newValue;
        }

        return null;
    }

    @Override
    public Object computeIfPresent(String key,
                                   BiFunction<? super String, ? super Object, ?> remappingFunction) {
        Object value = get(key);

        if (value != null) {
            Object newValue = remappingFunction.apply(key, value);
            put(key, newValue);
            return newValue;
        }

        return null;
    }

    @Override
    public Object replace(String key, Object value) {
        return containsKey(key) ? put(key, value) : null;
    }

    @Override
    public Object compute(String key,
                          BiFunction<? super String, ? super Object, ?> remappingFunction) {
        Object currentValue = get(key);
        Object newValue = remappingFunction.apply(key, currentValue);

        if (newValue == null) {
            remove(key);
            return null;
        } else {
            put(key, newValue);
            return newValue;
        }
    }

    @Override
    public Object merge(String key,
                        Object value,
                        BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        Object oldValue = get(key);
        Object newValue;

        if (oldValue == null) {
            if (value != null) {
                put(key, value);
                return value;
            } else {
                return null;
            }

        } else {
            newValue = remappingFunction.apply(oldValue, value);
            if (newValue == null) {
                remove(key);
                return null;
            } else {
                put(key, newValue);
                return newValue;
            }
        }
    }

    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        Object value = get(key);

        return value == null ? defaultValue : value;
    }
}
