package org.genedb.db.loading.alternative;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.GeneralDao;
import org.genedb.db.dao.OrganismDao;

import org.gmod.schema.feature.AbstractExon;
import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Contig;
import org.gmod.schema.feature.DirectRepeatRegion;
import org.gmod.schema.feature.FivePrimeUTR;
import org.gmod.schema.feature.Gap;
import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.MRNA;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.Pseudogene;
import org.gmod.schema.feature.PseudogenicTranscript;
import org.gmod.schema.feature.RepeatRegion;
import org.gmod.schema.feature.Supercontig;
import org.gmod.schema.feature.TRNA;
import org.gmod.schema.feature.ThreePrimeUTR;
import org.gmod.schema.feature.TopLevelFeature;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.feature.UTR;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.mapped.Synonym;
import org.gmod.schema.utils.ObjectManager;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

/**
 * Deals with loading an organism from an EMBL file into a Chado database.
 * It's expected to be configured as a singleton Spring bean. The main
 * calling point (and only public method, apart from the property setters)
 * is {@link #load(EmblFile)}.
 *
 * @author rh11
 *
 */
@Transactional(rollbackFor=DataError.class)
class EmblLoader {
    private static final Logger logger = Logger.getLogger(EmblLoader.class);

    // Injected beans
    private CvDao cvDao;
    private GeneralDao generalDao;
    private OrganismDao organismDao;
    private Organism organism;
    private ObjectManager objectManager;     // See #afterPropertiesSet()
    private SessionFactory sessionFactory;

    private SynonymManager synonymManager = new SynonymManager();

    public void setOrganismCommonName(String organismCommonName) {
        this.organism = organismDao.getOrganismByCommonName(organismCommonName);
        if (organism == null) {
            throw new IllegalArgumentException(String.format("Organism '%s' not found", organismCommonName));
        }
    }

    private Session session;

    /**
     * The main calling point for this class. Takes a parsed EMBL file, and loads
     * it into the database. Each call to this method constitutes a separate
     * Hibernate transaction. Even though the EMBL file has been parsed before
     * this method is called, its internal consistency has not been verified.
     * If we encounter a problem, a <code>DataError</code> is thrown and the
     * transaction is rolled back.
     *
     * @param emblFile the parsed EMBL file
     * @throws DataError if a data problem is discovered
     */
    public void load(EmblFile emblFile) throws DataError {
        /*
         * Thanks to the @Transactional annotation on this class,
         * Spring will automatically initiate a transaction when
         * we're called, which will be committed on successful
         * return or rolled back if we throw an exception.
         */

        this.session = SessionFactoryUtils.doGetSession(sessionFactory, false);
        boolean alreadyThere = session.createCriteria(Feature.class)
            .add(Restrictions.eq("organism", organism))
            .add(Restrictions.eq("uniqueName", emblFile.getAccession()))
            .uniqueResult() != null;

        if (!alreadyThere) {
            synonymManager.startSession(session);

            logger.info("Creating supercontig: " + emblFile.getAccession());
            Supercontig supercontig = TopLevelFeature.make(Supercontig.class, emblFile.getAccession(), organism);
            supercontig.markAsTopLevelFeature();
            supercontig.setResidues(emblFile.getSequence());
            session.persist(supercontig);

            init(supercontig);
            loadContigsAndGaps(emblFile.getContigLocations());
            loadFeatures(emblFile.getFeatureTable());
        } else {
            logger.error(String.format("The organism '%s' already has feature '%s'",
                organism.getCommonName(), emblFile.getAccession()));
        }
    }

    private Supercontig supercontig;
    private Map<String,AbstractGene> genesByUniqueName;
    private Map<String,Transcript> transcriptsByUniqueName;
    private NavigableMap<Integer,Contig> contigsByStart;

    private void init(Supercontig supercontig) {
        this.supercontig = supercontig;
        this.genesByUniqueName = new HashMap<String,AbstractGene>();
        this.transcriptsByUniqueName = new HashMap<String,Transcript>();
        this.contigsByStart = new TreeMap<Integer,Contig>();
    }

    private void loadContigsAndGaps(EmblLocation.Join locations) throws DataError {
        int pos = 0; // Position (interbase) on supercontig
        for(EmblLocation location: locations.locations) {
            if (location instanceof EmblLocation.External) {
                EmblLocation.External externalLocation = (EmblLocation.External) location;
                int contigLength = externalLocation.simple.getLength();

                logger.debug(String.format("Creating contig '%s' at %d-%d", externalLocation.accession, pos, pos + contigLength));
                Contig contig = TopLevelFeature.make(Contig.class, externalLocation.accession, organism);
                contig.setResidues(supercontig.getResidues(pos, pos + contigLength));
                session.persist(contig);
                supercontig.addLocatedChild(contig, pos, pos + contigLength, (short) 0, 0);

                contigsByStart.put(pos, contig);

                pos += contigLength;
            } else if (location instanceof EmblLocation.Gap) {
                EmblLocation.Gap gapLocation = (EmblLocation.Gap) location;
                int gapLength = gapLocation.getLength();

                logger.debug(String.format("Creating gap at %d-%d", pos, pos + gapLength));
                Gap gap = supercontig.addGap(pos, pos + gapLength);
                session.persist(gap);

                pos += gapLength;
            } else {
                throw new DataError("The CO section should contain only external references and gaps");
            }
        }
    }

    private void locate(Feature feature, EmblLocation location) {
        locate(feature, location.getFmin(), location.getFmax(), (short) location.getStrand(), 0);
    }

    private void locate(Feature feature, int fmin, int fmax, short strand, int phase) {
        supercontig.addLocatedChild(feature, fmin, fmax, strand, phase);
        Contig contig = contigsByStart.floorEntry(fmin).getValue();
        if (contig == null || fmax > contig.getFmax()) {
            logger.debug(String.format("The feature '%s' (%s) is not contained in a contig",
                feature.getUniqueName(), feature.getName()));
            return;
        }
        logger.debug(String.format("The feature '%s' lies on contig '%s'", feature.getUniqueName(), contig.getUniqueName()));
        contig.addLocatedChild(feature, fmin - contig.getFmin(), fmax - contig.getFmin(), strand, phase, 1);
    }

    private void loadFeatures(FeatureTable featureTable) throws DataError {
        List<FeatureTable.Feature> utrs = new ArrayList<FeatureTable.Feature>();

        for (FeatureTable.Feature feature: featureTable.getFeatures()) {
            try {
                loadFeature(utrs, feature);
            }
            catch (DataError e) {
                e.setLineNumber(feature.lineNumber);
                throw e;
            }
        }

        for (FeatureTable.Feature utr: utrs) {
            try {
                loadUTR(utr);
            }
            catch (DataError e) {
                e.setLineNumber(utr.lineNumber);
                throw e;
            }
        }
    }

    private void loadFeature(List<FeatureTable.Feature> utrs,
            FeatureTable.Feature feature) throws DataError {
        String featureType = feature.type;

        if (featureType.equals("repeat_region")) {
            loadRepeatRegion(feature);
        }
        else if (featureType.equals("CDS")) {
            loadCDS((FeatureTable.CDSFeature) feature);
        }
        else if (featureType.equals("tRNA")) {
            loadTRNA(feature);
        }
        else if (featureType.equals("3'UTR") || featureType.equals("5'UTR")) {
            utrs.add(feature);
        }
        else {
            logger.warn(String.format("Ignoring '%s' feature on line %d", featureType, feature.lineNumber));
        }
    }

    private void loadRepeatRegion(FeatureTable.Feature repeatRegionFeature) throws DataError {
        String repeatRegionName = repeatRegionFeature.getQualifierValue("FEAT_NAME");
        EmblLocation repeatRegionLocation = repeatRegionFeature.location;
        int fmin = repeatRegionLocation.getFmin();
        int fmax = repeatRegionLocation.getFmax();

        logger.debug(String.format("Creating repeat region '%s' at %d-%d", repeatRegionName, fmin, fmax));
        String repeatType = repeatRegionFeature.getQualifierValue("rpt_type");
        if (repeatType == null) {
            throw new DataError("A repeat feature must have a /rpt_type qualifier");
        }
        if (!repeatType.equals("direct")) {
            throw new DataError(String.format("Unknown repeat type '%s'", repeatType));
        }
        Class<? extends RepeatRegion> repeatRegionClass = DirectRepeatRegion.class;

        RepeatRegion repeatRegion = RepeatRegion.make(repeatRegionClass,
            organism, String.format("%s:repeat:%d-%d", supercontig.getUniqueName(), fmin, fmax), repeatRegionName);
        String note = repeatRegionFeature.getQualifierValue("note");
        if (note != null) {
            repeatRegion.addFeatureProp(note, "feature_property", "comment");
        }
        session.persist(repeatRegion);
        locate(repeatRegion, fmin, fmax, (short)0, 0);
    }

    /**
     * Abstract superclass for gene loaders.
     * <p>
     * It is the responsibility of each implementing class to set at least
     * the fields <code>geneUniqueName</code>, <code>transcriptUniqueName</code>
     * and <code>geneName</code> in its constructor.
     *
     * @author rh11
     *
     */
    abstract class GeneLoader {
        protected FeatureTable.Feature feature;
        protected EmblLocation location;
        protected boolean isPseudo = false;
        protected boolean singlySpliced = true;
        protected String geneUniqueName = null;
        protected String transcriptUniqueName = null;
        protected String geneName;
        protected Transcript transcript;

        public GeneLoader(FeatureTable.Feature feature) {
            this.feature  = feature;
            this.location = feature.location;
        }

        protected Class<? extends AbstractGene> getGeneClass() {
            return isPseudo ? Pseudogene.class : Gene.class;
        }

        protected abstract void processTranscriptQualifiers();
        protected abstract Class<? extends Transcript> getTranscriptClass();

        /**
         * The main entry point to a gene loader.
         */
        public void load() {
            if (geneUniqueName == null) {
                throw new RuntimeException("Cannot load a gene with no uniqueName");
            }
            if (transcriptUniqueName == null) {
                throw new RuntimeException("Cannot load a transcript with no uniqueName");
            }
            loadTranscript(loadOrFetchGene());
        }

        private AbstractGene loadOrFetchGene() {
            if (singlySpliced) {
                AbstractGene gene = createSinglySplicedGene();
                genesByUniqueName.put(geneUniqueName, gene);
                return gene;
            } else {
                if (genesByUniqueName.containsKey(geneUniqueName)) {
                    logger.debug(String.format("Gene for shared ID '%s' already exists", geneUniqueName));
                    return genesByUniqueName.get(geneUniqueName);
                } else {
                    // This is the first transcript, so create the gene
                    AbstractGene gene = createGene();
                    genesByUniqueName.put(geneUniqueName, gene);
                    return gene;
                }
            }
        }

        private AbstractGene createSinglySplicedGene() {
            if (transcriptUniqueName.contains(".")) {
                logger.warn(String.format(
                    "The transcript '%s' is not alternately spliced, yet its systematic name contains a dot",
                    transcriptUniqueName));
            }
            return createGene();
        }

        private AbstractGene createGene() {
            logger.debug(String.format("Creating gene '%s' (%s)", geneUniqueName, geneName));
            AbstractGene gene = AbstractGene.make(getGeneClass(), organism, geneUniqueName, geneName);
            locate(gene, location);
            session.persist(gene);
            return gene;
        }

        private void loadTranscript(AbstractGene gene) {
            logger.debug(String.format("Creating transcript '%s' for gene '%s'", transcriptUniqueName, gene.getUniqueName()));
            this.transcript = gene.makeTranscript(getTranscriptClass(), transcriptUniqueName);

            transcriptsByUniqueName.put(transcriptUniqueName, transcript);
            processTranscriptQualifiers();

            loadExons();
        }

        /**
         * For each <code>/&lt;qualifierName%gt;</code> qualifier, add a synonym of type
         * <code>synonymType</code> to the transcript.
         *
         * @param cdsFeature the CDS feature
         * @param transcript the transcript to which the synonyms should be added
         * @param qualifierName the name of the qualifer
         * @param synonymType the type of synonym. Should be a term in the <code>genedb_synonym_type</code> CV
         * @param isCurrent whether the synonym is current or not
         */
        protected void processSynonymQualifiers(String qualifierName, String synonymType, boolean isCurrent) {
            Set<String> synonyms = new HashSet<String>();
            for (String synonymString: feature.getQualifierValues(qualifierName)) {
                if (synonyms.contains(synonymString)) {
                    logger.error(String.format("The qualifier /%s=\"%s\" is repeated on transcript '%s'",
                        qualifierName, synonymString, transcriptUniqueName));
                    continue;
                }
                synonyms.add(synonymString);

                logger.debug(String.format("Adding %s '%s' for transcript", synonymType, synonymString));
                Synonym synonym = synonymManager.getSynonym(synonymType, synonymString);
                transcript.addSynonym(synonym, isCurrent, /*isInternal:*/ false);
            }
        }

        /**
         * For each <code>/&lt;qualifierName%gt;</code> qualifier, add a property of type
         * <code>synonymType</code> to the polypeptide, if there is one, or else to the
         * transcript.
         *
         * @param cdsFeature the CDS feature
         * @param transcript the transcript
         * @param qualifierName the qualifier name
         * @param propertyCvName the name of the CV to which the property term belongs.
         *          Should be either <code>feature_property</code> for built-in Chado
         *          properties, or <code>genedb_misc</code> for local additions.
         * @param propertyTermName the term name corresponding to the property to add.
         *          If it belongs to the <code>genedb_misc</code> CV, it should be a child of
         *          the term <code>genedb_misc:feature_props</code>.
         */
        protected void processPropertyQualifiers(String qualifierName, String propertyCvName, String propertyTermName) {

            for(String qualifierValue: feature.getQualifierValues(qualifierName)) {
                logger.debug(String.format("Adding %s:%s '%s' for transcript",
                    propertyCvName, propertyTermName, qualifierValue));
                if (transcript instanceof ProductiveTranscript) {
                    ((ProductiveTranscript) transcript).getProtein()
                        .addFeatureProp(qualifierValue, propertyCvName, propertyTermName);
                } else {
                    transcript.addFeatureProp(qualifierValue, propertyCvName, propertyTermName);
                }
            }
        }

        protected void processProductQualifiers() {
            if (transcript instanceof ProductiveTranscript) {
                ProductiveTranscript productiveTranscript = (ProductiveTranscript) transcript;
                Polypeptide polypeptide = productiveTranscript.getProtein();
                List<String> products = feature.getQualifierValues("product");

                if (polypeptide != null) {
                    for (String product: products) {
                        polypeptide.addProduct(product);
                    }
                }
                else {
                    for (String product: products) {
                        transcript.addCvTerm("genedb_products", product);
                    }
                }
            }
        }

        private void loadExons() {
            int exonIndex = 0;
            for (EmblLocation exonLocation: location.getParts()) {
                String exonUniqueName = String.format("%s:exon:%d", transcript.getUniqueName(), ++exonIndex);
                logger.debug(String.format("Creating exon '%s' at %d-%d", exonUniqueName, exonLocation.getFmin(), exonLocation.getFmax()));
                AbstractExon exon = transcript.createExon(exonUniqueName, exonLocation.getFmin(), exonLocation.getFmax());
                session.persist(exon);
            }
        }
    }

    class CDSLoader extends GeneLoader {
        public CDSLoader(FeatureTable.CDSFeature cdsFeature) throws DataError {
            super(cdsFeature);

            isPseudo = cdsFeature.isPseudo();
            geneUniqueName = cdsFeature.getSharedId();
            transcriptUniqueName = cdsFeature.getUniqueName();
            geneName = cdsFeature.getGeneName();

            singlySpliced = false;
            if (geneUniqueName == null) {
                singlySpliced = true;
                geneUniqueName = transcriptUniqueName;
            }
        }

        /**
         * Use the qualifiers of the CDS feature to add various bits of annotation
         * to the transcript (or to the polypeptide, if there is one). Specifically,
         * add synonyms, properties and products.
         *
         * @param cdsFeature
         * @param transcript
         */
        @Override
        protected void processTranscriptQualifiers() {

            processSynonymQualifiers("synonym", "synonym", true);
            processSynonymQualifiers("previous_systematic_id", "systematic_id", false);

            processPropertyQualifiers("note",   "feature_property", "comment");
            processPropertyQualifiers("method", "genedb_misc",      "method");
            processPropertyQualifiers("colour", "genedb_misc",      "colour");
            processPropertyQualifiers("status", "genedb_misc",      "status");

            processProductQualifiers();
        }

        @Override
        protected Class<? extends Transcript> getTranscriptClass() {
            return isPseudo ? PseudogenicTranscript.class : MRNA.class;
        }
    }

    private void loadCDS(FeatureTable.CDSFeature cdsFeature) throws DataError {
        new CDSLoader(cdsFeature).load();
    }

    private class TRNALoader extends GeneLoader {

        public TRNALoader(FeatureTable.Feature feature) throws DataError {
            super(feature);

            String name = feature.getQualifierValue("FEAT_NAME");
            if (name == null) {
                throw new DataError("tRNA feature has no /FEAT_NAME qualifier");
            }

            geneUniqueName = transcriptUniqueName = name;
        }

        @Override
        protected void processTranscriptQualifiers() {
            processPropertyQualifiers("note",      "feature_property", "comment");
            processPropertyQualifiers("anticodon", "feature_property", "anticodon");
            processProductQualifiers();
        }

        @Override
        protected Class<? extends Transcript> getTranscriptClass() {
            return TRNA.class;
        }

    }

    private void loadTRNA(FeatureTable.Feature feature) throws DataError {
        new TRNALoader(feature).load();
    }

    private void loadUTR(FeatureTable.Feature utrFeature) throws DataError {
        String utrType = utrFeature.type;
        EmblLocation utrLocation = utrFeature.location;
        String uniqueName = utrFeature.getUniqueName();

        logger.debug(String.format("Loading %s for '%s' at %s", utrType, uniqueName, utrLocation));

        Transcript transcript = transcriptsByUniqueName.get(uniqueName);
        if (transcript == null) {
            throw new DataError(String.format("Could not find transcript '%s' for %s", uniqueName, utrType));
        }

        Class<? extends UTR> utrClass;
        if (utrType.equals("3'UTR")) {
            utrClass = ThreePrimeUTR.class;
        } else if (utrType.equals("5'UTR")) {
            utrClass = FivePrimeUTR.class;
        } else {
            throw new RuntimeException(String.format("Unrecognised UTR feature type '%s'", utrType));
        }

        int part = 0;
        for (EmblLocation utrPartLocation: utrLocation.getParts()) {
            String utrUniqueName = String.format("%s:%dutr", uniqueName, utrClass == ThreePrimeUTR.class ? 3 : 5);
            if (part > 0) {
                utrUniqueName += ":" + part;
            }

            logger.debug(String.format("Creating %s feature '%s' at %d-%d",
                utrType, utrUniqueName, utrPartLocation.getFmin(), utrPartLocation.getFmax()));

            transcript.createUTR(utrClass, utrUniqueName, utrPartLocation.getFmin(), utrPartLocation.getFmax());
            ++ part;
        }
    }

    public void setOrganismDao(OrganismDao organismDao) {
        this.organismDao = organismDao;
    }

    /**
     * Set the ObjectManager. This is expected to be called by Spring.
     * We will inject the GeneralDao object into the ObjectManager ourselves from
     * {@link #afterPropertiesSet}, so this ObjectManager need not have the GeneralDao
     * injected yet. This avoids circularity.
     *
     * @param objectManager
     */
    public void setObjectManager(ObjectManager objectManager) {
        this.objectManager = objectManager;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setGeneralDao(GeneralDao generalDao) {
        this.generalDao = generalDao;
    }

    public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }

    public void afterPropertiesSet() {
        synonymManager.setObjectManager(objectManager);

        /*
         * We cannot set the DAOs of the objectManager
         * directly in Load.xml, because that creates a circular
         * reference that (understandably) causes Spring to
         * throw a tantrum. Thus we inject them into
         * here, and pass them to the ObjectManager after Spring
         * configuration.
         */
        objectManager.setGeneralDao(generalDao);
        objectManager.setCvDao(cvDao);
    }
}
