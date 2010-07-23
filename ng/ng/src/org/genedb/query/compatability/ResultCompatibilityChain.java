package org.genedb.query.compatability;

import org.genedb.query.Result;
import org.genedb.query.ResultCompatibility;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to wrap multiple ResultDataSetCompatibility. Each one is tested in turn
 * until one returns true, or returns false finally.
 * 
 * @author art
 */
public class ResultCompatibilityChain implements
        ResultCompatibility {

    private List<ResultCompatibility> checkList = new ArrayList<ResultCompatibility>();
    
    public boolean areCompatible(Result one, Result two) {
        for (ResultCompatibility rdsc : checkList) {
            if (rdsc.areCompatible(one, two)) {
                return true;
            }
        }
        return false;
    }

}
