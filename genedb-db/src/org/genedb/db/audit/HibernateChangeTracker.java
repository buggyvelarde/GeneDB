package org.genedb.db.audit;

import org.gmod.schema.cfg.ChadoAnnotationConfiguration;
import org.gmod.schema.cfg.ChadoSessionFactoryBean;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;

public class HibernateChangeTracker implements ChangeTracker {
    private static final Logger logger = Logger.getLogger(HibernateChangeTracker.class);

    @Resource(name="sessionFactory")
    private SessionFactory sessionFactory;

    @Resource(name="&sessionFactory")
    private ChadoSessionFactoryBean sessionFactoryBean;

    private long currentAuditId;

    @Override
    @Transactional
    public HibernateChangeSet changes(String key) throws SQLException {
        Session session = SessionFactoryUtils.getSession(sessionFactory, false);

        BigInteger checkpointAuditIdInteger = (BigInteger) session.createSQLQuery(
            "select audit_id from audit.checkpoint where key = :key"
        ).setString("key", key)
        .uniqueResult();
        int checkpointAuditId = checkpointAuditIdInteger == null ? 0 : checkpointAuditIdInteger.intValue();
        logger.debug("CheckPointAuditId: '" + checkpointAuditId + "'");

        Configuration configuration = sessionFactoryBean.getConfiguration();
        if (!(configuration instanceof ChadoAnnotationConfiguration)) {
            throw new RuntimeException();
        }

        currentAuditId = ((BigInteger) session.createSQLQuery(
            Dialect.getDialect(configuration.getProperties()).getSequenceNextValString("audit.audit_seq")
        ).uniqueResult()).longValue();

        HibernateChangeSet changeSet = new HibernateChangeSet(session, key, currentAuditId);
        changeSet.setChadoAnnotationConfiguration((ChadoAnnotationConfiguration) configuration);

        processFeatureAuditRecords(checkpointAuditId, changeSet);
        processFeatureRelationshipAuditRecords(checkpointAuditId, changeSet);
        processFeatureLocAuditRecords(checkpointAuditId, changeSet);


        return changeSet;
    }

    /**
     * @param session
     * @param checkpointAuditId
     * @param changeSet
     */
    private void processFeatureAuditRecords(int checkpointAuditId,
            HibernateChangeSet changeSet) {

        Session session = SessionFactoryUtils.getSession(sessionFactory, false);

        List<?> list = session.createSQLQuery(
            "select audit_id, feature_id, type, uniquename, type_id" +
            " from audit.feature" +
            " where audit_id > :checkpoint and audit_id < :currentAuditId" +
            " order by audit_id"
        ).addScalar("audit_id", Hibernate.INTEGER)
        .addScalar("feature_id", Hibernate.INTEGER)
        .addScalar("type", Hibernate.STRING)
        .addScalar("uniquename", Hibernate.STRING)
        .addScalar("type_id", Hibernate.INTEGER)
        .setInteger("checkpoint", checkpointAuditId)
        .setLong("currentAuditId", currentAuditId)
        .list();

        logger.trace("Feature Audit Records List size: " + list.size());
        for (Object o: list) {
            Object[] a = (Object[]) o;

            int    auditId    = (Integer) a[0];
            int    featureId  = (Integer) a[1];
            String type       = (String)  a[2];
            String uniqueName = (String)  a[3];
            int    typeId     = (Integer) a[4];

            logger.trace(String.format("[%d] %s of feature '%s' (ID=%d)",
                auditId, type, uniqueName, featureId));

            if (type.equals("INSERT")) {
                changeSet.insertedFeature(auditId, featureId, typeId);
            } else if (type.equals("UPDATE")) {
                changeSet.updatedFeature(auditId, featureId, typeId);
            } else if (type.equals("DELETE")) {
                changeSet.deletedFeature(auditId, featureId, typeId);
            }
        }
    }

    private void processFeatureRelationshipAuditRecords(int checkpointAuditId,
            HibernateChangeSet changeSet) {

        Session session = SessionFactoryUtils.getSession(sessionFactory, false);

        List<?> list = session.createSQLQuery(
            "select feature_relationship.audit_id" +
            "     , feature_relationship.type" +
            "     , feature_relationship.feature_relationship_id" +
            "     , feature_relationship.object_id" +
            "     , feature.type_id" +
            " from audit.feature_relationship" +
            " join public.feature on feature.feature_id = feature_relationship.object_id" +
            " where audit_id > :checkpoint and audit_id < :currentAuditId" +
            " order by audit_id"
        ).setInteger("checkpoint", checkpointAuditId)
        .setLong("currentAuditId", currentAuditId)
        .list();

        for (Object o: list) {
            Object[] a = (Object[]) o;

            int    auditId            = (Integer) a[0];
            String type               = (String)  a[1];
            int featureRelationshipId = (Integer) a[2];
            int    featureId          = (Integer) a[3];
            int    typeId             = (Integer) a[4];

            logger.trace(String.format("[%d] %s of feature_relationship ID=%d, " +
                    "counts as update of object feature ID=%d (type ID=%d)",
                auditId, type, featureRelationshipId, featureId, typeId));

            if (type.equals("INSERT") || type.equals("DELETE")) {
                changeSet.updatedFeature(auditId, featureId, typeId);
            }
        }
    }

    private void processFeatureLocAuditRecords(int checkpointAuditId,
            HibernateChangeSet changeSet) {

        Session session = SessionFactoryUtils.getSession(sessionFactory, false);

        List<?> list = session.createSQLQuery(
            "select featureloc.audit_id" +
            "     , featureloc.type" +
            "     , featureloc.featureloc_id" +
            "     , featureloc.srcfeature_id" +
            "     , feature.type_id" +
            " from audit.featureloc" +
            " join public.feature on feature.feature_id = featureloc.srcfeature_id" +
            " where audit_id > :checkpoint and audit_id < :currentAuditId" +
            " order by audit_id"
        ).setInteger("checkpoint", checkpointAuditId)
        .setLong("currentAuditId", currentAuditId)
        .list();

        for (Object o: list) {
            Object[] a = (Object[]) o;

            int    auditId      = (Integer) a[0];
            String type         = (String)  a[1];
            int    featureLocId = (Integer) a[2];
            int    featureId    = (Integer) a[3];
            int    typeId       = (Integer) a[4];

            logger.trace(String.format("[%d] %s of featureloc ID=%d, " +
                    "counts as update of source feature ID=%d (type ID=%d)",
                auditId, type, featureLocId, featureId, typeId));

            if (type.equals("INSERT") || type.equals("DELETE")) {
                changeSet.updatedFeature(auditId, featureId, typeId);
            }
        }
    }


    public long getCurrentAuditId() {
        return currentAuditId;
    }
}
