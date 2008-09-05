package org.genedb.db.loading.alternative;

import org.genedb.util.TwoKeyMap;

import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Synonym;
import org.gmod.schema.utils.ObjectManager;

import org.apache.log4j.Logger;
import org.hibernate.Session;

/**
 * Manage the creation of synonyms during organism loading.
 *
 * A SynonymManager object contains a cache of synonyms that it personally
 * has created, and will return a cached object in preference to creating a
 * new one. At no time do we check whether the synonym already exists in the
 * database: if we did not create it, we simply make a new one.
 * <p>
 * This saves the overhead of checking the database every time a synonym
 * is required, which (according to JProfiler, on the S. mansoni load)
 * is what the loader spent the majority of its time doing when
 * {@link ObjectManager#getSynonym(String,String)} was used to create synonyms.
 * When this was changed, the loader went from being massively CPU-bound
 * to being database-bound (as one would hope it would be), so do not change
 * it without an excellent reason!
 * <p>
 * During the loading life-cycle, each {@link EmblLoader} has a single SynonymManager
 * instance. For each EMBL file loaded, a new Hibernate session is created and
 * {@link #startSession(Session)} is called. Therefore it is no problem for a synonym
 * to be used several times within a particular organism.
 *
 * However we are implicitly assuming that synonyms are never
 * shared across organisms: if we come across one that is, it will cause a constraint
 * violation error when loaded. If that is ever a problem in practice, we will need to
 * preload the existing synonyms as well.
 *
 * @author rh11
 *
 */
class SynonymManager {
    private ObjectManager objectManager;
    private Session session;
    private static final Logger logger = Logger.getLogger(SynonymManager.class);

    SynonymManager() {
        // empty
    }

    SynonymManager(ObjectManager objectManager) {
        this.objectManager = objectManager;
    }

    public void setObjectManager(ObjectManager objectManager) {
        this.objectManager = objectManager;
    }

    /**
     * Must be called when a new Hibernate session is started.
     * @param session the newly-started session
     */
    public void startSession(Session session) {
        logger.debug("Starting new session");
        detachedSynonymsByType.putAll(synonymsByType);
        synonymsByType.clear();
        this.session = session;
    }


    private TwoKeyMap<String,String,Synonym> detachedSynonymsByType = new TwoKeyMap<String,String,Synonym>();
    private TwoKeyMap<String,String,Synonym> synonymsByType = new TwoKeyMap<String,String,Synonym>();

    /**
     * Fetch a synonym from the local cache. Failing that, create a new one.
     *
     * @param synonymType the synonym type (should be a term in the <code>genedb_synonym_type</code> CV).
     * @param synonymString the actual synonym
     * @return the cached or newly-created synonym object
     */
    public synchronized Synonym getSynonym(String synonymType, String synonymString) {
        logger.debug(String.format("Looking for synonym '%s:%s'", synonymType, synonymString));
        if (session == null) {
            throw new IllegalStateException("getSynonym called with no session");
        }

        if (synonymsByType.containsKey(synonymType, synonymString)) {
            logger.debug("Synonym found in cache");
            return synonymsByType.get(synonymType, synonymString);
        }

        if (detachedSynonymsByType.containsKey(synonymType, synonymString)) {
            logger.debug("Synonym found in detached cache. Merging");
            Synonym mergedSynonym = (Synonym) session.merge(detachedSynonymsByType.get(synonymType, synonymString));
            detachedSynonymsByType.remove(synonymType, synonymString);
            synonymsByType.put(synonymType, synonymString, mergedSynonym);
            return mergedSynonym;
        }

        logger.debug(String.format("Synonym '%s:%s' not found in cache", synonymType, synonymString));
        CvTerm synonymTypeCvTerm = (CvTerm) session.load(CvTerm.class, objectManager.getIdOfExistingCvTerm("genedb_synonym_type", synonymType));
        Synonym synonym = new Synonym(synonymTypeCvTerm, synonymString, synonymString);
        synonymsByType.put(synonymType, synonymString, synonym);
        return synonym;
    }
}
