package org.genedb.web.mvc.controller.binder;


import org.gmod.schema.cv.Cv;
import org.gmod.schema.dao.CvDaoI;
import org.gmod.schema.dao.SequenceDaoI;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.utils.CountedName;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContextException;

import java.util.List;


public class BrowseBean implements InitializingBean {
    
    private static final int DEFAULT_LIMIT = 15;
    
    private Cv cv;
    
    private CvDaoI cvDao;
    
    private SequenceDaoI sequenceDao;
    
    private String cvName;
    
    private int limit = DEFAULT_LIMIT;
	
	
	public void afterPropertiesSet() throws Exception {
        List<Cv> cvs = cvDao.getCvByName(cvName);
        
        if (cvs.size() == 0) {
            throw new ApplicationContextException("No cv of name '"+cvName+"' found when configuring " + getClass());
        }
		if (cvs.size() > 1) {
			throw new ApplicationContextException("Too many ('"+cvs.size()+"') cv of name '"+cvName+"' found when configuring " + getClass());
		}
        cv = cvs.get(0);
	}

	public List<String> getPossibleMatches(String search) {
        return cvDao.getPossibleMatches(search, cv, limit);
//        List<String> results = new ArrayList<String>();
//        results.add("a");
//        results.add("b");
//        results.add("c");
//        return results;
	}
    
    public List<CountedName> getAllTerms() {
        return cvDao.getAllTermsInCvWithCount(cv);
    }
    
    public List<Feature> getFeaturesForCvTerm(String cvTermName) {
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

    @Required
    public void setCvName(String cvName) {
        this.cvName = cvName;
    }

    public void setLimit(int limit) {    
        this.limit = limit;
    }

    @Required
    public void setSequenceDaoI(SequenceDaoI sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

}