package org.genedb.web.mvc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.mapped.Cv;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.utils.CountedName;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

public class BrowseBean implements InitializingBean {

    private static final int DEFAULT_LIMIT = 15;

    private Cv cv;

    private CvDao cvDao;

    private SequenceDao sequenceDao;

    private List<String> cvNames;

    private int limit = DEFAULT_LIMIT;

    private Map<String, Cv> cvs = new HashMap<String, Cv>();

    public void afterPropertiesSet() throws Exception {
        for (String cvName : cvNames)
            cvs.put(cvName, cvDao.getCvByName(cvName));
    }

    public List<String> getPossibleMatches(String search, String cvName) {
        return cvDao.getPossibleMatches(search, cvs.get(cvName), limit);
        // List<String> results = new ArrayList<String>();
        // results.add("a");
        // results.add("b");
        // results.add("c");
        // return results;
    }

    public List<CountedName> getAllTerms() {
        return cvDao.getAllTermsInCvWithCount(cv);
    }

    public List<Feature> getFeaturesForCvTerm(String cvTermName, String cvName) {
        List<Feature> features = sequenceDao.getFeaturesByCvTermNameAndCvName(cvTermName, cvName);
        return features;
    }

    @Required
    public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    @Required
    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public void setCvNames(List<String> cvName) {
        this.cvNames = cvName;
    }

}