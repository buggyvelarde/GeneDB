package org.genedb.db.audit;

import org.gmod.schema.cfg.ChadoAnnotationConfiguration;
import org.gmod.schema.cfg.ChadoSessionFactoryBean;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;

public class HibernateChangeTracker implements ChangeTracker {
    private static final Logger logger = Logger.getLogger(HibernateChangeTracker.class);

    @Resource(name="sessionFactory")
    private SessionFactory sessionFactory;

    @Resource(name="&sessionFactory")
    private ChadoSessionFactoryBean sessionFactoryBean;

    @Override
    public HibernateChangeSet changes(String key) throws SQLException {
        Session session = SessionFactoryUtils.getSession(sessionFactory, false);

        Integer checkpointAuditIdInteger = (Integer) session.createSQLQuery(
            "select audit_id from audit.checkpoint where key = :key"
        ).setString("key", key)
        .uniqueResult();
        int checkpointAuditId = checkpointAuditIdInteger == null ? 0 : checkpointAuditIdInteger;

        int currentAuditId = (Integer) session.createSQLQuery(
            "select nextval('audit.audit_seq' :: regclass)"
        ).uniqueResult();

        List<?> list = session.createSQLQuery(
            "select audit_id, feature_id, type, uniquename, type_id" +
            " from audit.feature" +
            " where audit_id > :checkpoint and audit_id < currval('audit.audit_seq' :: regclass)" +
            " order by audit_id"
        ).setInteger("checkpoint", checkpointAuditId)
        .list();

        HibernateChangeSet changeSet = new HibernateChangeSet(session, key, currentAuditId);
        Configuration configuration = sessionFactoryBean.getConfiguration();
        if (!(configuration instanceof ChadoAnnotationConfiguration)) {
            throw new RuntimeException();
        }
        changeSet.setChadoAnnotationConfiguration((ChadoAnnotationConfiguration) configuration);

        for (Object o: list) {
            Object[] a = (Object[]) o;

            int    auditId    = (Integer) a[0];
            int    featureId  = (Integer) a[1];
            String type       = (String)  a[2];
            String uniqueName = (String)  a[3];
            int    typeId     = (Integer) a[4];

            logger.trace(String.format("%s of feature '%s' (ID=%d)",
                type, uniqueName, featureId));

            if (type.equals("INSERT")) {
                changeSet.insertedFeature(auditId, featureId, typeId);
            } else if (type.equals("UPDATE")) {
                changeSet.updatedFeature(auditId, featureId, typeId);
            } else if (type.equals("DELETE")) {
                changeSet.deletedFeature(auditId, featureId, typeId);
            }
        }

        return changeSet;
    }

}
