package org.genedb.web.mvc.controller;

import java.util.List;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.mapped.CvTerm;

import org.springframework.beans.factory.InitializingBean;

public class BrowseBeanName implements InitializingBean {

    private static final int DEFAULT_LIMIT = 15;

    private SequenceDao sequenceDao;

    private CvDao cvDao;

    private String term;

    private CvTerm cvTerm;

    private int limit = DEFAULT_LIMIT;

    public void afterPropertiesSet() throws Exception {
        cvTerm = cvDao.getCvTermByNameAndCvName(term, "sequence");
    }

    public List<String> getPossibleMatches(String search) {
        return sequenceDao.getPossibleMatches(search, cvTerm, limit);
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public void setCvTerm(String cvTerm) {
        this.term = cvTerm;
    }

    public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
