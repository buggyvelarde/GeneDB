package org.gmod.schema.utils;

import org.genedb.db.dao.GeneralDao;

import org.gmod.schema.mapped.Db;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.Synonym;

import org.apache.log4j.Logger;
import org.hibernate.EmptyInterceptor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Maintains a cache of objects that have been created but
 * not yet flushed, so we can look them up even though they're
 * not in the database yet. If you register your instance
 * of this class as a Hibernate interceptor, the cache is cleared
 * whenever Hibernate flushes.
 * <p>
 * This mechanism is available to {@link org.genedb.db.loading.auxiliary.Loader} subclasses via
 * the protected Loader field <code>objectManager</code>. At the time of
 * writing, it's only used by {@link org.genedb.db.loading.auxiliary.InterProLoader}.
 *
 * @author rh11
 */
public class ObjectManager extends EmptyInterceptor {
    private static final Logger logger = Logger.getLogger(ObjectManager.class);
    private GeneralDao generalDao;
    private Map<String,Map<String,DbXRef>> dbxrefsByAccByDb
        = new HashMap<String,Map<String,DbXRef>>();
    private Map<String,Map<String,Synonym>> synonymsByNameByType
        = new HashMap<String,Map<String,Synonym>>();

    @Override
    @SuppressWarnings("unchecked")
    public void postFlush(@SuppressWarnings("unused") Iterator entities) {
        logger.debug("Flushing dbxrefs");
        dbxrefsByAccByDb.clear();
        logger.debug("Flushing synonyms");
        synonymsByNameByType.clear();
    }

    /**
     * Get or create a DbXRef. Works even if the DbXRef has been
     * created but not yet flushed, as long as it was created using
     * this object.
     *
     * @param identifier A string of the form <code>db:accession</code>
     * @return the existing or newly-created DbXRef
     */
    public DbXRef getDbXRef(String identifier) {
        int colonIndex = identifier.indexOf(':');
        if (colonIndex == -1)
            throw new IllegalArgumentException(String.format(
                "Failed to parse dbxref identifier '%s'", identifier));
        return getDbXRef(identifier.substring(0, colonIndex), identifier.substring(colonIndex + 1));
    }

    /**
     * Get or create a DbXRef. Works even if the DbXRef has been
     * created but not yet flushed, as long as it was created using
     * this object.
     *
     * @param dbName The database name
     * @param accession The database-specific identifier
     * @return the existing or newly-created DbXRef
     */
    public DbXRef getDbXRef(String dbName, String accession) {
        logger.debug(String.format("Getting DbXRef '%s'/'%s'", dbName, accession));

        if (dbxrefsByAccByDb.containsKey(dbName)
                && dbxrefsByAccByDb.get(dbName).containsKey(accession))
            return dbxrefsByAccByDb.get(dbName).get(accession);

        DbXRef dbxref = findOrCreateDbXRefFromDbAndAccession(dbName, accession);

        /* The above statement can trigger a flush, which is why we
         * need the following check afterwards rather than before.
         */
        if (!dbxrefsByAccByDb.containsKey(dbName))
            dbxrefsByAccByDb.put(dbName, new HashMap<String,DbXRef>());

        dbxrefsByAccByDb.get(dbName).put(accession, dbxref);
        return dbxref;
    }

    /**
     * Get or create a DbXRef, and set the description if it's not already set.
     * Works even if the DbXRef has been created but not yet flushed, as long
     * as it was created using this object.
     *
     * @param dbName The database name
     * @param accession The database-specific identifier
     * @param description The description to use
     * @return the existing or newly-created DbXRef
     */
    public DbXRef getDbXRef(String dbName, String accession, String description) {
        DbXRef dbxref = getDbXRef(dbName, accession);
        if (dbxref.getDescription() == null)
            dbxref.setDescription(description);
        return dbxref;
    }

    /**
     * Get or create a synonym.
     *
     * @param synonymType
     * @param synonymString
     * @return
     */
    public Synonym getSynonym(String synonymType, String synonymString) {
        logger.trace(String.format("Looking for synonym '%s' of type '%s'", synonymString, synonymType));
        if (synonymsByNameByType.containsKey(synonymType)) {
            Map<String,Synonym> synonymsByName = synonymsByNameByType.get(synonymType);
            if (synonymsByName.containsKey(synonymString)) {
                logger.trace("Found synonym locally");
                return synonymsByName.get(synonymString);
            }
        }
        logger.trace("Synonym not found locally. Falling back to GeneralDao");
        Synonym synonym = generalDao.getOrCreateSynonym(synonymType, synonymString);
        if (!synonymsByNameByType.containsKey(synonymType)) {
            synonymsByNameByType.put(synonymType, new HashMap<String,Synonym>());
        }
        synonymsByNameByType.get(synonymType).put(synonymString, synonym);
        return synonym;
    }


    private DbXRef findOrCreateDbXRefFromDbAndAccession(String dbName, String accession) {
        Db db = generalDao.getDbByName(dbName);
        if (db == null) {
            return null;
        }
        DbXRef dbXRef = generalDao.getDbXRefByDbAndAcc(db, accession);
        if (dbXRef == null) {
            logger.debug(String.format("Creating new dbxref '%s:%s'", dbName, accession));
            dbXRef = new DbXRef(db, accession);
            generalDao.persist(dbXRef);
        }
        return dbXRef;
    }

    public void setGeneralDao(GeneralDao generalDao) {
        this.generalDao = generalDao;
    }
}