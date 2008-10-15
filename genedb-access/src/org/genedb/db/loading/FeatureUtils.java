package org.genedb.db.loading;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.GeneralDao;
import org.genedb.db.dao.PubDao;
import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.mapped.Cv;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Db;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureCvTerm;
import org.gmod.schema.mapped.FeatureCvTermDbXRef;
import org.gmod.schema.mapped.FeatureCvTermProp;
import org.gmod.schema.mapped.FeatureProp;
import org.gmod.schema.mapped.Pub;
import org.gmod.schema.mapped.PubDbXRef;
import org.gmod.schema.utils.Rankable;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class FeatureUtils implements InitializingBean {
    private static final Logger logger = Logger.getLogger(FeatureUtils.class);

    private CvDao cvDao;
    private PubDao pubDao;
    private SequenceDao sequenceDao;
    private GeneralDao generalDao;
    protected CvTerm GENEDB_TOP_LEVEL;

    protected static final Pattern PUBMED_PATTERN = Pattern.compile("PMID:|PUBMED:", Pattern.CASE_INSENSITIVE);

    public void setPubDao(PubDao pubDao) {
        this.pubDao = pubDao;
    }

    public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    private Cv CV_GENEDB;
    private CvTerm GO_KEY_EVIDENCE, GO_KEY_QUALIFIER, GENEDB_AUTOCOMMENT;
    public void afterPropertiesSet() {
        CV_GENEDB = cvDao.getCvByName("genedb_misc");
        GENEDB_TOP_LEVEL = cvDao.getCvTermByNamePatternInCv("top_level_seq", CV_GENEDB).get(0);
        GO_KEY_EVIDENCE = cvDao.getCvTermByNamePatternInCv("evidence", CV_GENEDB).get(0);
        GO_KEY_QUALIFIER = cvDao.getCvTermByNamePatternInCv("qualifier", CV_GENEDB).get(0);
        GENEDB_AUTOCOMMENT = cvDao.getCvTermByNamePatternInCv("autocomment", CV_GENEDB).get(0);
    }

    public void markTopLevelFeature(org.gmod.schema.mapped.Feature topLevel) {
        sequenceDao.persist(new FeatureProp(topLevel, GENEDB_TOP_LEVEL, "true", 0));
    }

    /**
     * Create, or lookup a Pub object from a PMID:acc style input, although the
     * prefix is ignored
     *
     * @param ref the reference
     * @return the Pub object
     */
    public Pub findOrCreatePubFromPMID(String ref) {
        Db DB_PUBMED = generalDao.getDbByName("MEDLINE");
        int colon = ref.indexOf(":");
        String accession = ref;
        if (colon != -1) {
            accession = ref.substring(colon + 1);
        }
        DbXRef dbXRef = generalDao.getDbXRefByDbAndAcc(DB_PUBMED, accession);
        Pub pub;
        if (dbXRef == null) {
            dbXRef = new DbXRef(DB_PUBMED, accession);
            generalDao.persist(dbXRef);
            CvTerm cvTerm = cvDao.getCvTermById(1); // TODO -Hack
            pub = new Pub("PMID:" + accession, cvTerm);
            generalDao.persist(pub);
            PubDbXRef pubDbXRef = new PubDbXRef(pub, dbXRef, true);
            generalDao.persist(pubDbXRef);
        } else {
            pub = pubDao.getPubByDbXRef(dbXRef);
        }
        return pub;
    }

    /**
     * Take a pipe-separated list of dbxref identifers, and find (or
     * create if necessary) the corresponding {@link org.gmod.schema.mapped.DbXRef}s.
     *
     * @param xref A pipe-separated list of dbxref identifiers
     * @return A list of the corresponding DbXref objects
     */
    public List<DbXRef> findOrCreateDbXRefsFromString(String xref) {
        List<DbXRef> ret = new ArrayList<DbXRef>();
        StringTokenizer st = new StringTokenizer(xref, "|");
        while (st.hasMoreTokens()) {
            ret.add(findOrCreateDbXRefFromString(st.nextToken()));
        }
        return ret;
    }

    /**
     * Take a db reference and look it up, or create it if it doesn't exist
     *
     * @param xref the reference ie db:id
     * @return the created or looked-up DbXref
     */
    public DbXRef findOrCreateDbXRefFromString(String xref) {
        int index = xref.indexOf(':');
        if (index == -1) {
            return null;
        }
        String dbName = xref.substring(0, index);
        String accession = xref.substring(index + 1);
        return findOrCreateDbXRefFromDbAndAccession(dbName, accession);
    }

    public DbXRef findOrCreateDbXRefFromDbAndAccession(String dbName, String accession) {
        Db db = generalDao.getDbByName(dbName);
        if (db == null) {
            return null;
        }
        DbXRef dbXRef = generalDao.getDbXRefByDbAndAcc(db, accession);
        if (dbXRef == null) {
            logger.debug(String.format("Creating new dbxref '%s:%s'", dbName, accession));
            dbXRef = new DbXRef(db, accession);
            sequenceDao.persist(dbXRef);
        }
        return dbXRef;
    }

    /**
     * Does a string look likes it's a PubMed reference
     *
     * @param xref The string to examine
     * @return true if it looks like a PubMed reference
     */
    public boolean looksLikePub(String xref) {
        boolean ret = PUBMED_PATTERN.matcher(xref).lookingAt();
        return ret;
    }

    /*
     * Pre-caching the name -> id mapping is a big win compared with
     * doing a new query every time, when doing a data load. It uses a
     * chunk of memory though, and perhaps a less aggressive strategy
     * would give a better time/space tradeoff.
     */

    private Map<String, Integer> goTermIdsByAcc = null;
    private CvTerm getGoTerm(String id) {
        if (goTermIdsByAcc == null)
            goTermIdsByAcc = cvDao.getGoTermIdsByAcc();
        if (!goTermIdsByAcc.containsKey(id))
            return null;
        return cvDao.getCvTermById(goTermIdsByAcc.get(id));
    }

    public void createGoEntries(Feature polypeptide, GoInstance go, String comment,
            DbXRef withFromDbXRef) {
        if (withFromDbXRef == null)
            createGoEntries(polypeptide, go, comment, Collections.<DbXRef>emptyList());
        else
            createGoEntries(polypeptide, go, comment, Collections.singletonList(withFromDbXRef));
    }

    public void createGoEntries(Feature polypeptide, GoInstance go, String comment,
            List<DbXRef> withFromDbXRefs) {
        CvTerm cvTerm = getGoTerm(go.getId());
        if (cvTerm == null) {
            logger.error("Unable to find a CvTerm for the GO id of '" + go.getId() + "'. Skipping");
            return;
        }

        Pub pub = pubDao.getPubByUniqueName("null");
        String ref = go.getRef();
        // Reference
        Pub refPub = pub;
        if (ref != null && looksLikePub(ref)) {
            // The reference is a pubmed id - usual case
            refPub = findOrCreatePubFromPMID(ref);
        }

        boolean not = go.getQualifierList().contains("not"); // FIXME - Working?
        List<FeatureCvTerm> fcts = sequenceDao.getFeatureCvTermsByFeatureAndCvTermAndNot(
            polypeptide, cvTerm, not);
        int rank = 0;
        if (fcts.size() != 0) {
            rank = getNextRank(fcts);
        }
        FeatureCvTerm fct = new FeatureCvTerm(cvTerm, polypeptide, refPub, not, rank);
        sequenceDao.persist(fct);

        sequenceDao.persist(new FeatureCvTermProp(GENEDB_AUTOCOMMENT, fct, comment, 0));

        // Evidence
        FeatureCvTermProp fctp = new FeatureCvTermProp(GO_KEY_EVIDENCE, fct,
                go.getEvidence().getDescription(), 0);
        sequenceDao.persist(fctp);

        // Qualifiers
        int qualifierRank = 0;
        List<String> qualifiers = go.getQualifierList();
        for (String qualifier : qualifiers) {
            fctp = new FeatureCvTermProp(GO_KEY_QUALIFIER, fct, qualifier, qualifierRank);
            qualifierRank++;
            sequenceDao.persist(fctp);
        }

        // With/From
        for (DbXRef withFromDbXRef: withFromDbXRefs) {
            sequenceDao.persist(new FeatureCvTermDbXRef(withFromDbXRef, fct));
        }
    }

    private <T extends Rankable> int getNextRank(List<T> list) {
        BitSet bs = new BitSet(list.size() + 1);
        for (Rankable r : list) {
            bs.set(r.getRank());
        }
        return bs.nextClearBit(0);
    }


    public void setGeneralDao(GeneralDao generalDao) {
        this.generalDao = generalDao;
    }
}
