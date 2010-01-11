package org.genedb.db.loading;

import org.genedb.util.TwoKeyMap;

import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Synonym;
import org.gmod.schema.utils.ObjectManager;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.util.List;

/**
 * Manage the creation of synonyms during organism loading.
 *
 * A SynonymManager object contains a cache of all existing synonyms,
 * and will return a cached object in preference to creating a  new one.
 * If the cached synonym was not previously requested in the current session,
 * it is merged before returning. (Thus it is essential to call
 * {@link #startSession(Session)} whenever a session is started in which
 * {@link #getSynonym(String,String)} may be called.)
 * <p>
 * This mechanism saves the overhead of checking the database every time a synonym
 * is required, which - according to JProfiler, on the S. mansoni load -
 * is what the loader spent the majority of its time doing when
 * {@link ObjectManager#getSynonym(String,String)} was used to create synonyms.
 * <p>
 * During the loading life-cycle, each {@link EmblLoader} has a single SynonymManager
 * instance. For each EMBL file loaded, a new Hibernate session is created and
 * {@link #startSession(Session)} is called.
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

    private boolean preloaded = false;

    /**
     * Must be called when a new Hibernate session is started.
     * @param session the newly-started session
     */
    public void startSession(Session session) {
        logger.debug("Starting new session");
        this.session = session;
        if (!preloaded) {
            preload();
            preloaded = true;
        }
        detachedSynonymsByType.putAll(synonymsByType);
        synonymsByType.clear();
    }

    private void preload() {
        logger.debug("Preloading synonyms");
        @SuppressWarnings("unchecked")
        List<Object[]> list = session.createQuery(
            "select s.type.name, s.name, s from Synonym s"
        ).list();
        for (Object[] array: list) {
            synonymsByType.put( (String)array[0], (String)array[1], (Synonym)array[2]);
        }
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
