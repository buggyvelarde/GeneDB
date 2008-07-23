package org.genedb.db.loading;

import static org.genedb.db.loading.EmblQualifiers.QUAL_TOP_LEVEL;

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
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.FeatureProp;
import org.gmod.schema.mapped.FeatureRelationship;
import org.gmod.schema.mapped.FeatureSynonym;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.mapped.Pub;
import org.gmod.schema.mapped.PubDbXRef;
import org.gmod.schema.mapped.Synonym;

import org.apache.log4j.Logger;
import org.biojava.bio.seq.StrandedFeature;
import org.springframework.beans.factory.InitializingBean;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
    private Cv so;
    protected CvTerm GENEDB_TOP_LEVEL;
    private Pub DUMMY_PUB;

    protected static final Pattern PUBMED_PATTERN = Pattern.compile("PMID:|PUBMED:", Pattern.CASE_INSENSITIVE);

//    public Feature createFeature(String typeName, String uniqueName, Organism organism) {
//        List<CvTerm> cvTerms = cvDao.getCvTermByNameInCv(typeName, so);
//        if (cvTerms.size() == 0) {
//            System.err.println("Unable to find name '" + typeName + "' in ontology '"
//                    + so.getName() + "'");
//            throw new ExpectedLookupMissing("Unable to find name '" + typeName + "' in ontology '"
//                    + so.getName() + "'");
//        }
//        CvTerm type = cvTerms.get(0);
//        // System.err.println("Got cvterm type:"+type);
//        Date now = new Date();
//        Timestamp ts = new Timestamp(now.getTime());
//        Feature feature = null;
//        feature = new Feature(organism, type, uniqueName, false, false, ts, ts);
//        return feature;
//    }

    public static void dumpFeature(org.biojava.bio.seq.Feature f, String msg) {
        System.err.print("--- ");
        if (msg != null) {
            System.err.print(msg);
        }
        System.err.println();
        System.err.println("Type=" + f.getType());
        System.err.print("Location=" + f.getLocation().getMin() + ".." + f.getLocation().getMax()
                + "  ");
        if (f instanceof StrandedFeature) {
            System.err.print(((StrandedFeature) f).getStrand().getToken());
        }
        System.err.println();
        // Annotation
        Map<?,?> annotationMap = f.getAnnotation().asMap();
        for (Map.Entry<?,?> entry: annotationMap.entrySet()) {
            System.err.println("   " + entry.getKey() + "=" + entry.getValue());
        }
    }

    /**
     * Create a simple FeatureLocation object, tying an object to one parent,
     * with rank 0, no fuzzy ends
     *
     * @param parent The feature this is located to
     * @param child The feature to locate
     * @param min The minimum position on the parent
     * @param max The maximum position on the parent
     * @param strand The strand-edness of the feature relative to the parent
     * @return the newly constructed FeatureLocation, not persisted
     */
    public FeatureLoc createLocation(Feature parent, Feature child, int min, int max, short strand) {
        return new FeatureLoc(parent, child, min, false, max, false, strand, null, 0, 0);
    }

    public FeatureRelationship createRelationship(Feature subject, Feature object, CvTerm relType,
            int rank) {
        FeatureRelationship fr = new FeatureRelationship(subject, object, relType, rank);

        object.getFeatureRelationshipsForObjectId().add(fr);
        subject.getFeatureRelationshipsForSubjectId().add(fr);

        return fr;
    }

    @SuppressWarnings("unchecked")
    public void createSynonym(CvTerm type, String name, Feature gene, boolean isCurrent) {
        Synonym synonym = null;
        Synonym match = sequenceDao.getSynonymByNameAndCvTerm(name, type);
        if (match == null) {
            synonym = new Synonym(type, name, name);
            sequenceDao.persist(synonym);
        } else {
            synonym = match;
        }

        FeatureSynonym fs = null;
        List<FeatureSynonym> matches2 = sequenceDao.getFeatureSynonymsByFeatureAndSynonym(gene,
            synonym);
        if (matches2.size() == 0) {
            fs = new FeatureSynonym(synonym, gene, this.DUMMY_PUB, isCurrent, false);
            sequenceDao.persist(fs);
        } else {
            fs = matches2.get(0);
        }
        // daoFactory.persist(fs);
        gene.getFeatureSynonyms().add(fs);
    }

    public void createSynonyms(CvTerm type, List<String> names, Feature feature, boolean isCurrent) {

        for (String name : names) {
            this.createSynonym(type, name, feature, isCurrent);
        }
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

    private Cv CV_GENEDB;
    private CvTerm GO_KEY_EVIDENCE, GO_KEY_QUALIFIER, GENEDB_AUTOCOMMENT;
    public void afterPropertiesSet() {
        so = cvDao.getCvByName("sequence");
        CV_GENEDB = cvDao.getCvByName("genedb_misc");
        GENEDB_TOP_LEVEL = cvDao.getCvTermByNameInCv(QUAL_TOP_LEVEL, CV_GENEDB).get(0);
        DUMMY_PUB = pubDao.getPubByUniqueName("null");
        GO_KEY_EVIDENCE = cvDao.getCvTermByNameInCv("evidence", CV_GENEDB).get(0);
        GO_KEY_QUALIFIER = cvDao.getCvTermByNameInCv("qualifier", CV_GENEDB).get(0);
        GENEDB_AUTOCOMMENT = cvDao.getCvTermByNameInCv("autocomment", CV_GENEDB).get(0);
    }

    public void markTopLevelFeature(org.gmod.schema.mapped.Feature topLevel) {
        sequenceDao.persist(new FeatureProp(topLevel, GENEDB_TOP_LEVEL, "true", 0));
    }

    public void setDummyPub(Pub dummyPub) {
        DUMMY_PUB = dummyPub;
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

    /**
     * Take a polypeptide feature and GoInstance object to create GO entries
     *
     * @param polypeptide the polypeptide Feature to which GO entries are to be
     *                attached
     * @param go a GoInstance object
     *
     */
    public void createGoEntries(Feature polypeptide, GoInstance go, String comment) {
        List<DbXRef> withFromDbXRefs = new ArrayList<DbXRef>();

        String xref = go.getWithFrom();
        if (xref != null) {
            if (!xref.contains(":"))
                logger.error(String.format("Can't parse dbxref '%s'", xref));
            else {
                List<DbXRef> dbXRefs = findOrCreateDbXRefsFromString(xref);
                for (DbXRef dbXRef : dbXRefs) {
                    if (dbXRef != null) {
                        withFromDbXRefs.add(dbXRef);
                    }
                }
            }
        }
        createGoEntries(polypeptide, go, comment, withFromDbXRefs);
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
            // FeatureCvTermPub fctp = new FeatureCvTermPub(refPub, fct);
            // sequenceDao.persist(fctp);
        }

        // logger.warn("pub is '"+pub+"'");

        boolean not = go.getQualifierList().contains("not"); // FIXME - Working?
        List<FeatureCvTerm> fcts = sequenceDao.getFeatureCvTermsByFeatureAndCvTermAndNot(
            polypeptide, cvTerm, not);
        int rank = 0;
        if (fcts.size() != 0) {
            rank = RankableUtils.getNextRank(fcts);
        }
        // logger.warn("fcts size is '"+fcts.size()+"' and rank is '"+rank+"'");
        FeatureCvTerm fct = new FeatureCvTerm(cvTerm, polypeptide, refPub, not, rank);
        sequenceDao.persist(fct);

        // Reference
        // Pub refPub = null;
        // if (ref != null && ref.startsWith("PMID:")) {
        // // The reference is a pubmed id - usual case
        // refPub = findOrCreatePubFromPMID(ref);
        // FeatureCvTermPub fctp = new FeatureCvTermPub(refPub, fct);
        // sequenceDao.persist(fctp);
        // }
        // GO_KEY_DATE = cvDao.getCvTermByNameInCv("unixdate",
        // CV_FEATURE_PROPERTY).get(0);

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

        // logger.info("Persisting new FeatureCvTerm for
        // '"+polypeptide.getUniquename()+"' with a cvterm of
        // '"+cvTerm.getName()+"'");
    }

    public void setGeneralDao(GeneralDao generalDao) {
        this.generalDao = generalDao;
    }
}
