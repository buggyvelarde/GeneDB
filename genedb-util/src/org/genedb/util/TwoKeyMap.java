package org.genedb.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A map with two keys.
 *
 * @author rh11
 *
 * @param <S> type of the first key
 * @param <T> type of the second key
 * @param <V> type of values
 */
public class TwoKeyMap<S,T,V> {
    private Map<S,Map<T,V>> map = new HashMap<S,Map<T,V>>();

    /**
     * Get the element whose first key is <code>key1</code>
     * and second key is <code>key2</code>.
     *
     * @param key1 the first key
     * @param key2 the second key
     * @return the element with these keys, or <code>null</code> if there is no such element.
     */
    public V get(S key1, T key2) {
        if (!map.containsKey(key1)) {
            return null;
        }
        return map.get(key1).get(key2);
    }

    /**
     * Add a new element to the map, or replace an existing one.
     *
     * @param key1 the first key
     * @param key2 the second key
     * @param value the value to store
     * @return the previous value, if any; otherwise <code>null</code>
     */
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

    /**
     * Does this map have an entry whose first key is <code>key1</code>?
     *
     * @param key1 the first key to look for
     * @return <code>true</code> if there is an entry whose first key is <code>key1</code>,
     *          and <code>false</code> if there isn't
     */
    public boolean containsFirstKey(S key1) {
        return map.containsKey(key1);
    }

    /**
     * Does this map have an entry whose first key is <code>key1</code>
     * and second key is <code>key2</code>?
     *
     * @param key1 the first key
     * @param key2 the second key
     * @return<code>true</code> if there is an entry whose keys are <code>key1</code>
     *          and <code>key2</code>, or <code>false</code> if there isn't
     */
    public boolean containsKey(S key1, T key2) {
        return map.containsKey(key1) && map.get(key1).containsKey(key2);
    }

    /**
     * Delete all entries from this map.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Remove a single entry from the map.
     *
     * @param key1 the first key
     * @param key2 the second key
     * @return the previous value of the entry with keys <code>key1</code> and <code>key2</code>,
     *          or <code>null</code> if there was no such entry
     */
    public V remove(S key1, T key2) {
        if (!map.containsKey(key1)) {
            return null;
        }
        return map.get(key1).remove(key2);
    }

    /**
     * Return a set of all the first keys in use in this map.
     *
     * @return
     */
    public Set<S> firstKeySet() {
        return map.keySet();
    }

    /**
     * For a given first key, return a mapping of second key to values.
     *
     * @param firstKey
     * @return
     */
    public Map<T,V> getMap(S firstKey) {
        return map.get(firstKey);
    }
}
