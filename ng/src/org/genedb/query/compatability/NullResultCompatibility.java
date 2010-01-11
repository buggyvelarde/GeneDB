/**
 * 
 */
package org.genedb.query.compatability;

import org.genedb.query.Result;
import org.genedb.query.ResultCompatibility;


/**
 * "Null object" pattern for ResultCompatability 
 *
 * @author art
 */
public class NullResultCompatibility implements ResultCompatibility {

    /**
     * Singleton
     */
    public static final ResultCompatibility INSTANCE = new NullResultCompatibility(); 
    
    private NullResultCompatibility() {
        // Deliberately empty
    }
    
    /**
     * Always returns true
     * 
     * @see org.genedb.zoe.query.ResultCompatibility#areCompatible(org.genedb.zoe.query.Result, org.genedb.zoe.query.Result)
     */
    public boolean areCompatible(Result one, Result two) {
        // TODO Auto-generated method stub
        return true;
    }

}
