package org.gmod.schema.utils;

import java.util.Collection;
import java.util.HashSet;

public class CollectionUtils {

    public static <T> Collection<T> safeGetter(Collection<T> collection) {
        if (collection != null) {
            return collection;
        }
        return new HashSet<T>(0);
    }
    
}
