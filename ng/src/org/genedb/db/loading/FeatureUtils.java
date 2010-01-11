package org.genedb.db.loading;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.GeneralDao;
import org.genedb.db.dao.PubDao;
import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Db;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureCvTerm;
import org.gmod.schema.mapped.FeatureCvTermDbXRef;
import org.gmod.schema.mapped.FeatureCvTermProp;
import org.gmod.schema.mapped.Pub;
import org.gmod.schema.mapped.PubDbXRef;
import org.gmod.schema.utils.ObjectManager;
import org.gmod.schema.utils.Rankable;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Transactional
public class FeatureUtils implements InitializingBean {
    private static final Logger logger = Logger.getLogger(FeatureUtils.class);

    private CvDao cvDao;
    private PubDao pubDao;
    private SequenceDao sequenceDao;
    private GeneralDao generalDao;
    private ObjectManager objectManager;
    private SessionFactory sessionFactory;

    private static final Pattern PUBMED_PATTERN = Pattern.compile("PMID:|PUBMED:", Pattern.CASE_INSENSITIVE);

    private CvTerm GO_KEY_EVIDENCE, GO_KEY_QUALIFIER, GO_KEY_ATTRIBUTION,
                   GO_KEY_RESIDUE, GO_KEY_DATE, GENEDB_AUTOCOMMENT;
    private CvTerm PUB_TYPE_UNFETCHED;
    private Db PUBMED_DB;
    private int NULL_PUB_ID;

    public void afterPropertiesSet() {
        logger.trace("Initialising FeatureUtils");
        objectManager.setDaos(generalDao, pubDao, cvDao);
        PUBMED_DB = objectManager.getExistingDbByName("PUBMED");

        GO_KEY_EVIDENCE = cvDao.getExistingCvTermByNameAndCvName("evidence", "genedb_misc");
        GO_KEY_ATTRIBUTION = cvDao.getExistingCvTermByNameAndCvName("attribution", "genedb_misc");
        GO_KEY_RESIDUE = cvDao.getExistingCvTermByNameAndCvName("residue", "genedb_misc");
        GO_KEY_DATE = cvDao.getExistingCvTermByNameAndCvName("date", "feature_property");       
        GO_KEY_QUALIFIER = cvDao.getExistingCvTermByNameAndCvName("qualifier", "genedb_misc");
        GENEDB_AUTOCOMMENT = cvDao.getExistingCvTermByNameAndCvName("autocomment", "genedb_misc");
        PUB_TYPE_UNFETCHED = cvDao.getExistingCvTermByNameAndCvName("unfetched", "genedb_literature");       

        Pub NULL_PUB = pubDao.getPubByUniqueName("null");
        if (NULL_PUB == null) {
            throw new RuntimeException("Could not find Pub with uniqueName 'null'");
        }
        NULL_PUB_ID = NULL_PUB.getPubId();
    }

    /**
     * Create or lookup a Pub object from a PMID:acc style input, although the
     * prefix is ignored.
     *
     * @param ref the reference
     * @return the Pub object
     */
    private Pub findOrCreatePubFromPMID(String ref) {
        String accession = ref.substring(1 + ref.indexOf(':')); // Text after first colon, or whole string if no colon
        logger.trace(String.format("Looking for Pub object with accession number '%s'", accession));
        DbXRef dbXRef = generalDao.getDbXRefByDbAndAcc(PUBMED_DB, accession); //objectManager.getDbXRef("PUBMED", accession);
        Pub pub;
        if (dbXRef == null) {
            logger.trace(String.format("Could not find DbXRef for PUBMED:%s; creating new DbXRef", accession));
            dbXRef = new DbXRef(PUBMED_DB, accession);
            generalDao.persist(dbXRef);
            pub = pubDao.getPubByUniqueName("PMID:" + accession);
            if (pub == null) {
                logger.trace("Pub not found in database");
                pub = new Pub("PMID:" + accession, PUB_TYPE_UNFETCHED);
                pubDao.persist(pub);
            } else {
                logger.trace("Pub already in database (even though the DbXRef was not)");
            }
            PubDbXRef pubDbXRef = new PubDbXRef(pub, dbXRef, true);
            generalDao.persist(pubDbXRef);
        } else {
            pub = pubDao.getPubByDbXRef(dbXRef);
            logger.trace(String.format("Found dbxref '%s'; corresponding Pub is '%s'", dbXRef, pub));
        }
        return pub;
    }

    /**
     * Does a string look like a PubMed reference?
     *
     * @param xref The string to examine
     * @return true if it looks like a PubMed reference
     */
    private boolean looksLikePub(String xref) {
        return PUBMED_PATTERN.matcher(xref).lookingAt();
    }

    /*
     * Pre-caching the name -> id mapping is a big win compared with
     * doing a new query every time, when doing a data load. It uses a
     * chunk of memory though, and perhaps a less aggressive strategy
     * would give a better time/space tradeoff.
     */

    private volatile Map<String, Integer> goTermIdsByAcc = null;
    private CvTerm getGoTerm(String id) {
        if (goTermIdsByAcc == null) {
            // Double-checked locking of a volatile variable is
            // JMM-compliant since JVM 1.5.
            synchronized (this) {
                if (goTermIdsByAcc == null) {
                    goTermIdsByAcc = cvDao.getGoTermIdsByAcc();
                }
            }
        }
        if (!goTermIdsByAcc.containsKey(id)) {
            return null;
        }
        return cvDao.getCvTermById(goTermIdsByAcc.get(id));
    }

    public void createGoEntries(Feature polypeptide, GoInstance go, String comment,
            DbXRef withFromDbXRef) throws DataError {
        if (withFromDbXRef == null)
            createGoEntries(polypeptide, go, comment, Collections.<DbXRef>emptyList());
        else
            createGoEntries(polypeptide, go, comment, Collections.singletonList(withFromDbXRef));
    }

    public void createGoEntries(Feature polypeptide, GoInstance go, String comment,
            List<DbXRef> withFromDbXRefs) throws DataError {
        Session session = SessionFactoryUtils.getSession(sessionFactory, false);

        CvTerm cvTerm = getGoTerm(go.getId());
        if (cvTerm == null) {
            throw new DataError("Unable to find a CvTerm for the GO id of '" + go.getId() + "'.");
        }

        // Reference
        String ref = go.getRef();
        Pub refPub = (Pub) session.load(Pub.class, NULL_PUB_ID);
        if (ref != null && looksLikePub(ref)) {
            // The reference is a pubmed id - usual case
            refPub = findOrCreatePubFromPMID(ref);
        } else {
            logger.warn(String.format("Ignoring db_xref '%s' from GO entry", ref));
        }

        boolean not = go.getQualifierList().contains("not"); // FIXME - Working?
        List<FeatureCvTerm> fcts = sequenceDao.getFeatureCvTermsByFeatureAndCvTermAndNot(
            polypeptide, cvTerm, not);
        int rank = 0;
        if (fcts.size() != 0) {
            rank = getNextRank(fcts);
        }
        logger.trace(String.format("Creating new FeatureCvTerm for GO entry (%s, %s, %s, %b, %d)",
            cvTerm, polypeptide, refPub, not, rank));
        FeatureCvTerm fct = new FeatureCvTerm(cvTerm, polypeptide, refPub, not, rank);
        sequenceDao.persist(fct);

        sequenceDao.persist(new FeatureCvTermProp(GENEDB_AUTOCOMMENT, fct, comment, 0));

        // Evidence
        FeatureCvTermProp evidenceProp = new FeatureCvTermProp(GO_KEY_EVIDENCE, fct,
                go.getEvidence().getDescription(), 0);
        sequenceDao.persist(evidenceProp);

        // Date
        FeatureCvTermProp dateProp = new FeatureCvTermProp(GO_KEY_DATE, fct,
                go.getDate(), 0);
        sequenceDao.persist(dateProp);
        
        // Attribution
        String attribution = go.getAttribution();
        if (attribution != null) {
            sequenceDao.persist(new FeatureCvTermProp(GO_KEY_ATTRIBUTION, fct, attribution, 0));
        }

        // Residue
        String residue = go.getResidue();
        if (residue != null) {
            sequenceDao.persist(new FeatureCvTermProp(GO_KEY_RESIDUE, fct, residue, 0));
        }

        // Qualifiers
        int qualifierRank = 0;
        List<String> qualifiers = go.getQualifierList();
        for (String qualifier : qualifiers) {
            FeatureCvTermProp qualifierProp
                = new FeatureCvTermProp(GO_KEY_QUALIFIER, fct, qualifier, qualifierRank);
            qualifierRank++;
            sequenceDao.persist(qualifierProp);
        }

        // With/From
        for (DbXRef withFromDbXRef: withFromDbXRefs) {
            sequenceDao.persist(new FeatureCvTermDbXRef(fct, withFromDbXRef));
        }
    }

    <T extends Rankable> int getNextRank(List<T> list) {
        BitSet bs = new BitSet(list.size() + 1);
        for (Rankable r : list) {
            bs.set(r.getRank());
        }
        return bs.nextClearBit(0);
    }

    public void setObjectManager(ObjectManager objectManager) {
        this.objectManager = objectManager;
    }

    public void setPubDao(PubDao pubDao) {
        this.pubDao = pubDao;
    }

    public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public void setGeneralDao(GeneralDao generalDao) {
        this.generalDao = generalDao;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
