package org.gmod.schema.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Static utility class for miscellaneous collection handling
 *
 * @author art
 */
public class CollectionUtils {

    /**
     * Returns its argument if not null, or a new empty collection (in fact a HashSet<T>)
     * if the given one is null.
     *
     * @param <T>
     * @param collection
     * @return the original collection, or an empty collection
     */
    public static <T> Collection<T> safeGetter(Collection<T> collection) {
        if (collection != null) {
            return collection;
        }
        return new HashSet<T>();
    }


    /**
     * Returns its argument if not null, or a new empty collection (in fact a HashSet<T>)
     * if the given one is null.
     *
     * @param <T>
     * @param collection
     * @return the original collection, or an empty collection
     */
    public static <T> Set<T> safeGetter(Set<T> collection) {
        if (collection != null) {
            return collection;
        }
        return new HashSet<T>();
    }

    /**
     * Returns its argument if not null, or a new empty collection (in fact an ArrayList<T>)
     * if the given one is null.
     *
     * @param <T>
     * @param collection
     * @return the original collection, or an empty collection
     */
   public static <T> List<T> safeGetter(List<T> list) {
        if (list != null) {
            return list;
        }
        return new ArrayList<T>();
    }
}
