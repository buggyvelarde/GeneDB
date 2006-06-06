package org.genedb.db.dao;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


public class DaoFactory extends HibernateDaoSupport {
    
    private CvTermDao cvTermDao;
    private FeatureDao featureDao;
    private CvDao cvDao;
    private OrganismDao organismDao;
    private PubDao pubDao;
    
    public void persist(Object transientInstance) {
	getHibernateTemplate().persist(transientInstance);
    }
    
    public PubDao getPubDao() {
        return this.pubDao;
    }
    public void setPubDao(PubDao pubDao) {
        this.pubDao = pubDao;
    }
    public CvDao getCvDao() {
        return this.cvDao;
    }
    public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }
    public CvTermDao getCvTermDao() {
        return this.cvTermDao;
    }
    public void setCvTermDao(CvTermDao cvTermDao) {
        this.cvTermDao = cvTermDao;
    }
    public FeatureDao getFeatureDao() {
        return this.featureDao;
    }
    public void setFeatureDao(FeatureDao featureDao) {
        this.featureDao = featureDao;
    }
    public OrganismDao getOrganismDao() {
        return this.organismDao;
    }
    public void setOrganismDao(OrganismDao organismDao) {
        this.organismDao = organismDao;
    }
    
}
