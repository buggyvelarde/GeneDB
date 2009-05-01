package org.genedb.db.audit;

import org.gmod.schema.cfg.ChadoAnnotationConfiguration;
import org.gmod.schema.cfg.ChadoSessionFactoryBean;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
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

    private int currentAuditId;

    @Override
    @Transactional
    public HibernateChangeSet changes(String key) throws SQLException {
        Session session = SessionFactoryUtils.getSession(sessionFactory, false);

        BigInteger checkpointAuditIdInteger = (BigInteger) session.createSQLQuery(
            "select audit_id from audit.checkpoint where key = :key"
        ).setString("key", key)
        .uniqueResult();
        int checkpointAuditId = checkpointAuditIdInteger == null ? 0 : checkpointAuditIdInteger.intValue();

        currentAuditId = ((BigInteger) session.createSQLQuery(
            "select nextval('audit.audit_seq')"
        ).uniqueResult()).intValue();

        HibernateChangeSet changeSet = new HibernateChangeSet(session, key, currentAuditId);
        Configuration configuration = sessionFactoryBean.getConfiguration();
        if (!(configuration instanceof ChadoAnnotationConfiguration)) {
            throw new RuntimeException();
        }
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
        ).setInteger("checkpoint", checkpointAuditId)
        .setInteger("currentAuditId", currentAuditId)
        .list();

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
            "select audit.feature_relationship.audit_id" +
            "     , audit.feature_relationship.type" +
            "     , audit.feature_relationship.feature_relationship_id" +
            "     , audit.feature_relationship.object_id" +
            "     , public.feature.type_id" +
            " from audit.feature_relationship" +
            " join public.feature on public.feature.feature_id = audit.feature_relationship.object_id" +
            " where audit_id > :checkpoint and audit_id < :currentAuditId" +
            " order by audit_id"
        ).setInteger("checkpoint", checkpointAuditId)
        .setInteger("currentAuditId", currentAuditId)
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
            "select audit.featureloc.audit_id" +
            "     , audit.featureloc.type" +
            "     , audit.featureloc.featureloc_id" +
            "     , audit.featureloc.srcfeature_id" +
            "     , public.feature.type_id" +
            " from audit.featureloc" +
            " join public.feature on public.feature.feature_id = audit.featureloc.srcfeature_id" +
            " where audit_id > :checkpoint and audit_id < currval('audit.audit_seq')" +
            " order by audit_id"
        ).setInteger("checkpoint", checkpointAuditId)
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

}
