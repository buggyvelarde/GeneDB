package org.genedb.db.dao;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.List;

public class BaseDao extends HibernateDaoSupport {

    private static Logger logger = Logger.getLogger(org.genedb.db.dao.BaseDao.class);

    /**
     * Save the object to the database (at the end of the current transaction,
     * or depending upon flush mode). This method is defined in all the DAOs.
     * It's recommended to call it through an appropriate one eg SequenceDao
     * for Feature
     *
     * @param o The object to store
     */
    public void persist(Object o) {
        getHibernateTemplate().persist(o);
    }

    /**
     * Merge (update) an already persistent object back to the database (at the
     * end of the current transaction, or depending upon flush mode). This
     * method is defined in all the DAOs. It's recommended to call it through an
     * appropriate one eg SequenceDao for Feature
     *
     * @param o The object to merge
     */
    public void merge(Object o) {
        getHibernateTemplate().merge(o);
    }

    /**
     * Remove the object from the database (at the end of the current
     * transaction, or depending upon flush mode). This method is defined in all
     * the DAOs. It's recommended to call it through an appropriate one eg
     * SequenceDao for Feature
     *
     * @param o The object to delete
     */
    public void delete(Object o) {
        getHibernateTemplate().delete(o);
    }

    /**
     * Flush the session.
     */
    public void flush() {
        getHibernateTemplate().flush();
    }

    /**
     * Returns the first element of the given list, or null if the list is
     * empty. If the list has more than one element, logs a warning before
     * returning.
     *
     * The intention is that this should be called by data access methods that
     * expect a single result. The args parameters should consist alternately of
     * names and values, indicating the query parameters used.
     *
     * @param <T>
     * @param list The result list
     * @param args The query parameters (included in the log message if list
     *                contains more than one element)
     * @return
     */
    protected <T> T firstFromList(List<T> list, Object... args) {
        if (list == null) {
            logger.warn("Got called with null list");
            return null;
        }
        if (list.size() == 0) {
            // logger.warn("Got called with zero-length list");
            return null;
        }
        if (list.size() > 1) {
            // Log warning
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
            logger.warn("Expected one result, but got '" + list.size() + "' results in list ('"
                    + sb + "')");
        }
        return list.get(0);
    }

}
