package org.genedb.db.dao;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

public class BaseDao extends HibernateDaoSupport {

    private PlatformTransactionManager platformTransactionManager;

    /**
     * Save the object to the database (at the end of the current transaction, or depending upon 
     * flush mode). This method is defined in all the DAOs. It's recommended to call it through
     * an appropriate one eg SequenceDaoI for FeatureI 
     * 
     * @param o The object to store
     */
    public void persist(Object o) {
    	getHibernateTemplate().persist(o);
    }
    
    /**
     * Merge (update) an already persistent object back to the database (at the end of 
     * the current transaction, or depending upon flush mode). This method is defined in 
     * all the DAOs. It's recommended to call it through an appropriate one eg SequenceDaoI
     *  for FeatureI 
     * 
     * @param o The object to merge
     */
    public void merge(Object o) {
        getHibernateTemplate().merge(o);
    }
    
    /**
     * Remove the object from the database (at the end of the current transaction, or depending upon 
     * flush mode). This method is defined in all the DAOs. It's recommended to call it through
     * an appropriate one eg SequenceDaoI for FeatureI 
     * 
     * @param o The object to delete
     */
    public void delete(Object o) {
        getHibernateTemplate().delete(o);
    }
    
    public PlatformTransactionManager getPlatformTransactionManager() {
        return platformTransactionManager;
    }

    public void setPlatformTransactionManager(
            PlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
    }
    
    protected <T> T firstFromList(List<T> list, Object... args) {
        if (list == null) {
            logger.warn("Got called with null list");
            return null;
        }
        if (list.size() == 0) {
            //logger.warn("Got called with zero-length list");
            return null;
        }
        if (list.size()>1) {
            StringBuilder sb = new StringBuilder();
            boolean varName = true;
            boolean first = true;
            if (args != null && args.length > 0) {
                for (Object object : args) {
                    if (!first && varName) {
                        sb.append("', ");
                    }
                    sb.append(object);
                    if (varName) {
                        sb.append("='");
                    }
                    varName = !varName;
                    first = false;
                }
                sb.append("'");
            }
            logger.warn("Expected one result, but got '"+list.size()+"' results in list ('"+sb+"')");
        }
        return list.get(0);
    }
    
}
