package org.gmod.schema.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import org.genedb.db.dao.GeneralDao;

import org.gmod.schema.mapped.Db;
import org.gmod.schema.mapped.DbXRef;

import org.hibernate.EmptyInterceptor;

/**
 * Maintains a cache of the DbXRefs that have been created but
 * not yet flushed, so we can look them up even though they're
 * not in the database yet. If you register your instance
 * of this class as a Hibernate interceptor, the cache is cleared
 * whenever Hibernate flushes.
 * <p>
 * This mechanism is available to {@link org.genedb.db.loading.auxiliary.Loader} subclasses via
 * the protected Loader field <code>dbxrefManager</code>. At the time of
 * writing, it's only used by {@link org.genedb.db.loading.auxiliary.InterProLoader}.
 *
 * @author rh11
 */
public class DbXRefManager extends EmptyInterceptor {
    private static final Logger logger = Logger.getLogger(DbXRefManager.class);
    private GeneralDao generalDao;
    private Map<String,Map<String,DbXRef>> dbxrefsByAccByDb
        = new HashMap<String,Map<String,DbXRef>>();

    @Override
    @SuppressWarnings("unchecked")
    public void postFlush(@SuppressWarnings("unused") Iterator entities) {
        logger.debug("Flushing dbxrefs");
        dbxrefsByAccByDb.clear();
    }

    /**
     * Get or create a DbXRef. Works even if the DbXRef has been
     * created but not yet flushed, as long as it was created using
     * this object.
     *
     * @param identifier A string of the form <code>db:accession</code>
     * @return the existing or newly-created DbXRef
     */
    public DbXRef get(String identifier) {
        int colonIndex = identifier.indexOf(':');
        if (colonIndex == -1)
            throw new IllegalArgumentException(String.format(
                "Failed to parse dbxref identifier '%s'", identifier));
        return get(identifier.substring(0, colonIndex), identifier.substring(colonIndex + 1));
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
    public DbXRef get(String dbName, String accession) {
        logger.debug(String.format("Getting DbXRef '%s'/'%s'", dbName, accession));

        if (dbxrefsByAccByDb.containsKey(dbName)
                && dbxrefsByAccByDb.get(dbName).containsKey(accession))
            return dbxrefsByAccByDb.get(dbName).get(accession);

        DbXRef dbxref = findOrCreateDbXRefFromDbAndAccession(dbName, accession);

        /* The above statement can trigger a flush, which is why we
         * need this check afterwards rather than before. That was
         * a hell of a bug to discover at 6am, I can tell you!
         * (Especially when it conspired with a classpath misconfguration
         * that meant the logging didn't work. If the logging had worked
         * it would have been pretty easy to see what was happening, to
         * tell you the truth.) -rh11
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
    public DbXRef get(String dbName, String accession, String description) {
        DbXRef dbxref = get(dbName, accession);
        if (dbxref.getDescription() == null)
            dbxref.setDescription(description);
        return dbxref;
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