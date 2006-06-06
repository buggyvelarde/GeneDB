package org.genedb.db.loading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CollectionUtils {

    public static void addItemToMultiValuedMap(String key, GoInstance value, Map<String, List<GoInstance>> map) {
	List<GoInstance> list;
	if (map.containsKey(key)) {
	    list = map.get(key);
	} else {
	    list = new ArrayList<GoInstance>();
	    map.put(key, list);
	}
	list.add(value);
    }
    

}
