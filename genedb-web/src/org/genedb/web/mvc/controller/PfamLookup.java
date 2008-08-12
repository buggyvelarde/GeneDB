package org.genedb.web.mvc.controller;

import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

/**
 * <code>MultiActionController</code> that handles all non-form URL's.
 *
 * @author Ken Krebs
 */
public class PfamLookup implements InitializingBean {

    private DataSource ds;
    private List<String> lookups;
    
    public PfamLookup() {
        lookups = new ArrayList<String>();
        lookups.add("Susan");
        lookups.add("Sarah-Jane Smith");
        lookups.add("Perpergilliam Brown");
        lookups.add("Leela");
        lookups.add("Jo Grant");
        lookups.add("Tegan Jovanka");
    }
    
    public void afterPropertiesSet() throws Exception {
//      if (ds == null) {
//          throw new ApplicationContextException("Must set ds bean property on " + getClass());
//      }
    }

    public List<String> getPossibleMatches(String search) {
        List<String> ret = new ArrayList<String>();
        for (String check : lookups) {
            if (check.indexOf(search) != -1) {
                ret.add(check);
            }
        }
        return ret;
    }

}