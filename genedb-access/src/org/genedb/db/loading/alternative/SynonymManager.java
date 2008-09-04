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
 * With this single change, the loader went from being massively CPU-bound
 * to being database-bound (as one would hope it would be), so do not change
 * it without an excellent reason!
 * <p>
 * During the loading life-cycle, {@link EmblLoader} creates a new SynonymManager
 * instance for each EMBL file loaded. Therefore it is no problem for a synonym
 * to be used several times within a particular file.
 *
 * However we are implicitly assuming that synonyms are never
 * shared across chromosomes or top-level (super)contigs: if we come across one that is,
 * it will cause a constraint violation error when loaded. If that is ever a problem
 * in practice, we will need to adopt a more sophisticated strategy. Note that simply
 * extending the lifetime of this object will not work as one might hope: if a cached
 * synonym is found that was created in a different session (i.e. is from a different
 * EMBL file, since we create a new session for each EMBL file), Hibernate will not be
 * able to use it unless it's run through <code>Session.merge</code>.
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

    public void startSession(Session session) {
        logger.debug("Starting new session");
        detachedSynonymsByType.putAll(synonymsByType);
        synonymsByType.clear();
        this.session = session;
    }


    private TwoKeyMap<String,String,Synonym> detachedSynonymsByType = new TwoKeyMap<String,String,Synonym>();
    private TwoKeyMap<String,String,Synonym> synonymsByType = new TwoKeyMap<String,String,Synonym>();

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
