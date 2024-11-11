package ru.introguzzle.parsers.common;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public final class Maps {
    public static <K, V> Map<K, V> of() {
        return new HashMap<>();
    }

    @SafeVarargs
    public static <K, V> Map<K, V> of(Map<? extends K, ? extends V>... maps) {
        Map<K, V> map = new HashMap<>();
        for (var m : maps) {
            map.putAll(m);
        }

        return map;
    }
}
