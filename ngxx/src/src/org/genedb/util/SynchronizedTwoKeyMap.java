package org.genedb.util;

import java.util.Map;
import java.util.Set;

/**
 * A concurrency-safe wrapper around TwoKeyMap.
 *
 * @author rh11
 *
 * @param <S>
 * @param <T>
 * @param <V>
 */
public class SynchronizedTwoKeyMap<S,T,V> extends TwoKeyMap<S,T,V> {
    @Override
    public synchronized void clear() {
        super.clear();
    }

    @Override
    public synchronized boolean containsFirstKey(S key1) {
        return super.containsFirstKey(key1);
    }

    @Override
    public synchronized boolean containsKey(S key1, T key2) {
        return super.containsKey(key1, key2);
    }

    @Override
    public synchronized Set<S> firstKeySet() {
        return super.firstKeySet();
    }

    @Override
    public synchronized V get(S key1, T key2) {
        return super.get(key1, key2);
    }

    @Override
    public synchronized Map<T, V> getMap(S firstKey) {
        return super.getMap(firstKey);
    }

    @Override
    public synchronized V put(S key1, T key2, V value) {
        return super.put(key1, key2, value);
    }

    @Override
    public synchronized void putAll(TwoKeyMap<S, T, V> m) {
        super.putAll(m);
    }

    @Override
    public synchronized V remove(S key1, T key2) {
        return super.remove(key1, key2);
    }
}
