package org.genedb.query.params;

import java.util.ArrayList;
import java.util.List;

/**
 * Class, designed to be used as a delegate by a Param, which maintains 
 * a DI-set list of acceptable values for the Param.
 * 
 * @author art
 */
public class SimpleListConstraintDelegate implements ListConstraint {

    private List<String> allowedValues;
        
    public void setAllowedValues(List<String> allowedValues) {
        this.allowedValues = allowedValues;
    }

    public List<String> getAllAcceptableValues() {
        return allowedValues;
    }

    public List<String> getAcceptableValues(String partName, boolean mustBePrefix) {
        List<String> ret = new ArrayList<String>();
        for (String test : allowedValues) {
        int index = test.indexOf(partName);
        if (index == -1) {
            continue;
        }
        if (mustBePrefix && index != 0) {
            continue;
        }
        ret.add(test);
        }
        return ret;
    }


    public boolean isValid(String value) {
        return allowedValues.contains(value);
    }

}
