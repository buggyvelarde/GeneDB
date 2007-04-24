package org.genedb.web.mvc.controller;


import org.gmod.schema.cv.Cv;
import org.gmod.schema.dao.CvDaoI;
import org.gmod.schema.dao.SequenceDaoI;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.utils.CountedName;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContextException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;


public class BrowseBean implements InitializingBean {
    
    private static final int DEFAULT_LIMIT = 15;
    
    private Cv cv;
    
    private CvDaoI cvDao;
    
    private SequenceDaoI sequenceDao;
    
    private List<String> cvNames;
    
    private int limit = DEFAULT_LIMIT;
	
	private Map<String,Cv> cvs = new HashMap<String, Cv>();
	
	public void afterPropertiesSet() throws Exception {
       
		for (String cvName : cvNames) {			
        	List<Cv> temp = cvDao.getCvByName(cvName);
        
        	if (temp.size() == 0) {
        		throw new ApplicationContextException("No cv of name '"+cvName+"' found when configuring " + getClass());
        	}
        	if (temp.size() > 1) {
        		throw new ApplicationContextException("Too many ('"+cvs.size()+"') cv of name '"+cvName+"' found when configuring " + getClass());
        	}
        	
        	cvs.put(cvName,temp.get(0));
        }
	}

	public List<String> getPossibleMatches(String search,String cvName) {
		return cvDao.getPossibleMatches(search, cvs.get(cvName), limit);
//        List<String> results = new ArrayList<String>();
//        results.add("a");
//        results.add("b");
//        results.add("c");
//        return results;
	}
    
    public List<CountedName> getAllTerms() {
        return cvDao.getAllTermsInCvWithCount(cv);
    }
    
    public List<Feature> getFeaturesForCvTerm(String cvTermName, String cvName) {
        List<Feature> features = sequenceDao.getFeaturesByCvTermNameAndCvName(cvTermName, cvName);
        for (Feature feature : features) {
            // TODO Replace protein etc with genes
            
        }
        return features;
    }

    @Required
    public void setCvDaoI(CvDaoI cvDao) {
        this.cvDao = cvDao;
    }



    public void setLimit(int limit) {    
        this.limit = limit;
    }

    @Required
    public void setSequenceDaoI(SequenceDaoI sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

	public void setCvNames(List<String> cvName) {
		this.cvNames = cvName;
	}

}