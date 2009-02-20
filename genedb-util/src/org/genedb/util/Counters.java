package org.genedb.util;

import java.util.HashMap;
import java.util.Map;

/**
 * A collection of named incrementing counters.
 * Counter names do not need to be specified in advance:
 * a counter is created the first time it's used.
 *
 * @author rh11
 */
public class Counters {
    private Map<String,Integer> nextvalByName = new HashMap<String,Integer>();

    /**
     * Get the next value of the named counter. The first time
     * this method is called for a particular counter it will
     * return <code>1</code>, the second time <code>2</code>,
     * and so on.
     *
     * @param counter the name of the counter
     * @return the next value of the counter
     */
    public synchronized int nextval(String counter) {
        if (!nextvalByName.containsKey(counter)) {
            nextvalByName.put(counter, 2);
            return 1;
        }

        int nextval = nextvalByName.get(counter);
        nextvalByName.put(counter, nextval + 1);
        return nextval;
    }

    /**
     * Reset all counters.
     */
    public synchronized void clear() {
        nextvalByName.clear();
    }
}
