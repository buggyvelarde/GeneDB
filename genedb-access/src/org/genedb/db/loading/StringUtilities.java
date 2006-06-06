package org.genedb.db.loading;

import org.springframework.util.StringUtils;

public class StringUtilities {
    
    public static final String EMPTY_STRING = new String();
    
    private StringUtilities() {
	// Utility class - don't instantiate
    }

    public static String emptyOrValue(String s) {
	if (StringUtils.hasLength(s)) {
	    return s;
	}
	return StringUtilities.EMPTY_STRING;
    }

}
