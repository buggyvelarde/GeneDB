package org.genedb.db.loading;

import java.util.ArrayList;
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
    

}
