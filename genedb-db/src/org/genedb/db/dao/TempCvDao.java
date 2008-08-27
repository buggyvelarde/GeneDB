package org.genedb.db.dao;

import org.apache.log4j.Logger;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Feature;
import org.hibernate.NonUniqueResultException;
import org.hibernate.SessionFactory;

import java.util.HashMap;
import java.util.Map;

public class TempCvDao {
    protected SessionFactory sessionFactory;
    protected static final Logger log = Logger.getLogger(TempCvDao.class);

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    Map<String, String> conventionalLocation = new HashMap<String, String>();

    public Feature findFeature(String uniqueName) {
        try {
            Feature feature = (Feature) sessionFactory.getCurrentSession().createQuery(
                "select f from Feature f where f.uniqueName = :uniqueName").setString(
                "uniqueName", uniqueName).uniqueResult();
            return feature;
        } catch (NonUniqueResultException exp) {
            log.error(String.format(
                "Got more than 1 result when should have had one for an uniquename of '%s'",
                uniqueName), exp);
            return null;
        }
    }

    public CvTerm findCvTermByCvAndName(String cvName, String termName) {
        try {
            CvTerm type = (CvTerm) sessionFactory.getCurrentSession().createQuery(
                "from CvTerm cvt where cvt.name = :termName and cvt.cv.name = :cvName").setString(
                "cvName", cvName).setString("termName", termName).uniqueResult();
            return type;
        } catch (NonUniqueResultException exp) {
            log.error(String.format(
                "Got more than 1 result when should have had one for an cvterm of '%s'/'%s'",
                cvName, termName), exp);
            return null;
        }
    }
}