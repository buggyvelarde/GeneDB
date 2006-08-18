package org.genedb.db.dao;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

public class BaseDao extends HibernateDaoSupport {

    private PlatformTransactionManager platformTransactionManager;

    public void persist(Object o) {
        getHibernateTemplate().persist(o);
    }
    
    public void update(Object o) {
        getHibernateTemplate().update(o);
    }
    
    public PlatformTransactionManager getPlatformTransactionManager() {
        return platformTransactionManager;
    }

    public void setPlatformTransactionManager(
            PlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
    }
    
    protected <T> T firstFromList(List<T> list) {
        if (list == null) {
            return null;
        }
        if (list.size()>1) {
            // TODO Warning
        }
        return list.get(0);
    }
    
}
