package org.genedb.db.audit;

import org.gmod.schema.cfg.ChadoAnnotationConfiguration;
import org.gmod.schema.mapped.Feature;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class HibernateChangeSet implements ChangeSet {
    private static final Logger logger = Logger.getLogger(HibernateChangeSet.class);

    private Session session;
    private String key;
    private long ceilingAuditId;

    public HibernateChangeSet(Session session, String key, long ceilingAuditId) {
        this.session = session;
        this.ceilingAuditId = ceilingAuditId;
        this.key = key;
    }

    private ChadoAnnotationConfiguration chadoAnnotationConfiguration;
    public void setChadoAnnotationConfiguration(
            ChadoAnnotationConfiguration chadoAnnotationConfiguration) {
        this.chadoAnnotationConfiguration = chadoAnnotationConfiguration;
    }

    static class ChangeRecord implements Comparable<ChangeRecord> {
        private enum Type {INSERT, UPDATE, DELETE}
        Type type;
        int auditId;
        int featureId;
        int featureTypeId;
        ChangeRecord(Type type, int auditId, int featureId, int featureTypeId) {
            this.type = type;
            this.auditId = auditId;
            this.featureId = featureId;
            this.featureTypeId = featureTypeId;
        }
        @Override
        public int compareTo(ChangeRecord other) {
            return this.auditId - other.auditId;
        }
    }

    private Map<Integer, ChangeRecord> inserts = new TreeMap<Integer, ChangeRecord>();
    private Map<Integer, ChangeRecord> updates = new TreeMap<Integer, ChangeRecord>();
    private Map<Integer, ChangeRecord> deletes = new TreeMap<Integer, ChangeRecord>();

    void insertedFeature(int auditId, int featureId, int typeId) {
        ChangeRecord cr = new ChangeRecord(ChangeRecord.Type.INSERT,
            auditId, featureId, typeId);

        assert !inserts.containsKey(featureId);
        assert !updates.containsKey(featureId);
        assert !deletes.containsKey(featureId);

        inserts.put(featureId, cr);
}

    void updatedFeature(int auditId, int featureId, int typeId) {
        ChangeRecord cr = new ChangeRecord(ChangeRecord.Type.UPDATE,
            auditId, featureId, typeId);

        inserts.remove(featureId);
        updates.put(featureId, cr);
        deletes.remove(featureId);
    }

    void deletedFeature(int auditId, int featureId, int typeId) {
        if (inserts.containsKey(featureId)) {
            // If a feature has been inserted then deleted in the same
            // change set, remove all record of it.
            inserts.remove(featureId);
            updates.remove(featureId);
            assert !deletes.containsKey(featureId);
            return;
        }

        ChangeRecord cr = new ChangeRecord(ChangeRecord.Type.DELETE,
            auditId, featureId, typeId);

        updates.remove(featureId);
        deletes.put(featureId, cr);
    }

    @Override
    public void commit() throws SQLException {
        int n = session.createSQLQuery(
            "update audit.checkpoint set audit_id = :ceiling where key = :key"
        ).setLong("ceiling", ceilingAuditId)
        .setString("key", key)
        .executeUpdate();

        if (n < 1) {
            logger.info(String.format("No existing audit.checkpoint record for key '%s'", key));
            session.createSQLQuery(
                "insert into audit.checkpoint (key, audit_id) values (:key, :ceiling)"
            ).setLong("ceiling", ceilingAuditId)
            .setString("key", key)
            .executeUpdate();
        }
    }

    private Collection<Integer> featureIdsFromChangeRecordsFilteredByClass(Iterable<ChangeRecord> crs,
            Class<? extends Feature> featureClass) {

        Collection<Integer> typeIds = chadoAnnotationConfiguration.getTypeIdsByClass(featureClass);
        Set<Integer> newFeatureIds = new HashSet<Integer>();

        for (ChangeRecord cr: crs) {
            if (typeIds.contains(cr.featureTypeId)) {
                newFeatureIds.add(cr.featureId);
            }
        }

        return newFeatureIds;
    }

    @Override
    public Collection<Integer> newFeatureIds(Class<? extends Feature> featureClass) {
        return featureIdsFromChangeRecordsFilteredByClass(inserts.values(), featureClass);
    }

    @Override
    public Collection<Integer> changedFeatureIds(Class<? extends Feature> featureClass) {
        return featureIdsFromChangeRecordsFilteredByClass(updates.values(), featureClass);
    }

    @Override
    public Collection<Integer> deletedFeatureIds(Class<? extends Feature> featureClass) {
        return featureIdsFromChangeRecordsFilteredByClass(deletes.values(), featureClass);
    }
}
