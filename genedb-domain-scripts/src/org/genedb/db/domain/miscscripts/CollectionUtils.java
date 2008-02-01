package org.genedb.db.domain.miscscripts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class CollectionUtils {

    public static <K,V> void addItemToMultiValuedMap(K key, V value, Map<K, List<V>> map) {
        List<V> list;
        if (map.containsKey(key)) {
            list = map.get(key);
        } else {
            list = new ArrayList<V>();
            map.put(key, list);
        }
        list.add(value);
    }
    

    public static <T> Collection<T> safeGetter(Collection<T> collection) {
        if (collection != null) {
            return collection;
        }
        return new HashSet<T>(0);
    }
    
}
