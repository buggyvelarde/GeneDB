package org.genedb.db.helpers;

import org.hibernate.search.bridge.StringBridge;

/**
 * A simple StringBridge for use with feature locations (fmin and fmax).
 * Expects an Integer field, and converts it to a string that is padded
 * with zeros to a width of 9 digits. This makes it possible to issue
 * range queries against feature locations. 
 * 
 * @author rh11
 */
public class LocationBridge implements StringBridge {

    public String objectToString(Object object) {
       return String.format("%09d", (Integer) object);
    }

}
