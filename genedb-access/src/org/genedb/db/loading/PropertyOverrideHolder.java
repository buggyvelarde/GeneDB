package org.genedb.db.loading;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertyOverrideHolder {
    
    private static Map<String,Properties> mapping = new HashMap<String,Properties>();

    public static Properties getProperties(String key) {
        return PropertyOverrideHolder.mapping.get(key);
    }

    public static void setProperties(String key, Properties properties) {
        PropertyOverrideHolder.mapping.put(key, properties);
    }
    

}
