package org.genedb.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A map with two keys.
 *
 * @author rh11
 *
 */
public class TwoKeyMap<S,T,V> {
    private Map<S,Map<T,V>> map = new HashMap<S,Map<T,V>>();

    public V get(S key1, T key2) {
        if (!map.containsKey(key1)) {
            return null;
        }
        return map.get(key1).get(key2);
    }

    public V put(S key1, T key2, V value) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap<T,V>());
        }
        return map.get(key1).put(key2, value);
    }

    public void putAll(TwoKeyMap<S,T,V> m) {
        for(Map.Entry<S,Map<T,V>> e: m.map.entrySet()) {
            S key1 = e.getKey();
            Map<T,V> secondLevelMap = e.getValue();

            if (!map.containsKey(key1)) {
                map.put(key1, new HashMap<T,V>());
            }
            map.get(key1).putAll(secondLevelMap);
        }
    }

    public boolean containsKey(S key1, T key2) {
        return map.containsKey(key1) && map.get(key1).containsKey(key2);
    }

    public void clear() {
        map.clear();
    }

    public V remove(S key1, T key2) {
        if (!map.containsKey(key1)) {
            return null;
        }
        return map.get(key1).remove(key2);
    }

    public Set<S> firstKeySet() {
        return map.keySet();
    }
}
