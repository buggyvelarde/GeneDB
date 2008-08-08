package org.genedb.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * General purpose utilities for java.util.Map
 *
 * @author art
 */
public class MapUtils {

    /**
     * Utility routine to add a key/value pair to a map, where the key is
     * multi-valued ie a List
     *
     *
     * @param map Expected to be Map<String, List>
     * @param key the key to look up
     * @param value the value to add
     */
    public static <K,V> void addEntryAsPartOfList(Map<K, List<V>> map, K key, V value) {
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList<V>());
        }
        map.get(key).add(value);
    }

}
