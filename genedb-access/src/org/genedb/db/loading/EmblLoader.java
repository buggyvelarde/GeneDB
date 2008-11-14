package org.genedb.db.loading;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.GeneralDao;
import org.genedb.db.dao.OrganismDao;

import org.gmod.schema.cfg.FeatureTypeUtils;
import org.gmod.schema.feature.AbstractExon;
import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Contig;
import org.gmod.schema.feature.DirectRepeatRegion;
import org.gmod.schema.feature.FivePrimeUTR;
import org.gmod.schema.feature.Gap;
import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.MRNA;
import org.gmod.schema.feature.NcRNA;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.Pseudogene;
import org.gmod.schema.feature.PseudogenicTranscript;
import org.gmod.schema.feature.RRNA;
import org.gmod.schema.feature.RepeatRegion;
import org.gmod.schema.feature.SnRNA;
import org.gmod.schema.feature.SnoRNA;
import org.gmod.schema.feature.Supercontig;
import org.gmod.schema.feature.TRNA;
import org.gmod.schema.feature.ThreePrimeUTR;
import org.gmod.schema.feature.TopLevelFeature;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.feature.UTR;
import org.gmod.schema.mapped.Db;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureCvTerm;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.mapped.Synonym;
import org.gmod.schema.utils.ObjectManager;
import org.gmod.schema.utils.Similarity;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deals with loading an organism from an EMBL file into a Chado database.
 * It's expected to be configured as a singleton Spring bean. The main
 * calling point (and only public method, apart from the property setters)
 * is {@link #load(EmblFile)}.
 *
 * @author rh11
 *
 */
class EmblLoader {
    private static final Logger logger = Logger.getLogger(EmblLoader.class);

    // Constants

    /**
     * Whether or not the transcript type (mRNA, tRNA etc) should be appended
     * to the uniquename of the transcript feature in the database. We must do this
     * because Artemis requires each feature to have a globally unique name, and does
     * not work correctly if a transcript has the same uniquename as its gene.
     * Moreover, this can't be changed without causing serious potential problems
     * elsewhere, because the GFF3 feature format requires feature names to be
     * globally unique, and we want to be able to export our data in GFF3 format.
     */
    private static final boolean APPEND_TYPE_TO_TRANSCRIPT_UNIQUENAME = true;

    // Injected beans
    private CvDao cvDao;
    private GeneralDao generalDao;
    private OrganismDao organismDao;
    private ObjectManager objectManager;     // See #afterPropertiesSet()
    private SessionFactory sessionFactory;
    private FeatureUtils featureUtils;

    private SynonymManager synonymManager = new SynonymManager();

    // Configurable parameters
    private Organism organism;
    private Class<? extends TopLevelFeature> topLevelFeatureClass = Supercontig.class;
    private boolean continueOnError = false;

    public enum OverwriteExisting {YES, NO, MERGE}
    private OverwriteExisting overwriteExisting = OverwriteExisting.NO;

    private boolean sloppyControlledCuration = false;

    /**
     * Set the organism into which to load data.
     *
     * @param organismCommonName the common name of the organism
     */
    public void setOrganismCommonName(String organismCommonName) {
        this.organism = organismDao.getOrganismByCommonName(organismCommonName);
        if (organism == null) {
            throw new IllegalArgumentException(String.format("Organism '%s' not found", organismCommonName));
        }
    }

    /**
     * Set the class of top-level feature that this EMBL file represents.
     * The default, if this method is not called, is <code>Supercontig</code>.
     *
     * @param topLevelFeatureClass
     */
    public void setTopLevelFeatureClass(Class<? extends TopLevelFeature> topLevelFeatureClass) {
        this.topLevelFeatureClass = topLevelFeatureClass;
    }

    /**
     * Whether we should overwrite an existing top-level feature if it has
     * the same name as the one specified in this file. The default, if this
     * method is not called, is <code>NO</code>.
     *
     * If overwriteExisting is <code>NO</code>, the file will be skipped on the
     * grounds that it's already loaded. If it's <code>YES</code>, the previously
     * existing top-level feature, and features located on it, will
     * be deleted first. If it's <code>MERGE</code>, the existing top-level feature
     * and all features located on it will be retained, and any features
     * specified in this file will be loaded in addition.
     *
     * @param overwriteExisting <code>YES</code> if we should overwrite an
     * existing top-level feature, <code>NO</code> if not, or <code>MERGE</code>
     * if we should merge the contents of this file with an existing feature.
     */
    public void setOverwriteExisting(OverwriteExisting overwriteExisting) {
        this.overwriteExisting = overwriteExisting;
    }

    /**
     * Whether to deal with controlled_curation qualifiers that don't have the expected
     * format. The default, if this method is not called, is <code>false</code>.
     *
     * If set to true, we simply parse any dbxref from the /controlled_curation qualifier,
     * and add the complete text as a /curation qualifier.
     *
     * @param sloppyControlledCuration
     */
    public void setSloppyControlledCuration(boolean sloppyControlledCuration) {
        this.sloppyControlledCuration = sloppyControlledCuration;
    }

    /**
     * Whether we should continue if we encounter an error while loading a feature.
     * You should not usually set this option; it can be useful if you need to load
     * a file quickly and don't mind if some features are missing from the result.
     * <p>
     * In particular, you should <b>not</b> use this option when loading production
     * data!
     *
     * @param continueOnError
     */
    public void setContinueOnError(boolean continueOnError) {
        if (continueOnError) {
            logger.warn("We will continue if an error is encountered loading a feature");
        }
        this.continueOnError = continueOnError;
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
        TopLevelFeature topLevelFeature;
        try {
            topLevelFeature = getTopLevelFeature(emblFile.getAccession());
        } catch (TopLevelFeatureException e) {
            logger.error(e.getMessage());
            return;
        }

        doLoad(emblFile, topLevelFeature);
    }


    @Transactional(rollbackFor=DataError.class) // Will also rollback for runtime exceptions, by default
    private void doLoad(EmblFile emblFile, TopLevelFeature topLevelFeature) throws DataError {
        /*
         * Thanks to the @Transactional annotation,
         * Spring will automatically initiate a transaction when
         * we're called, which will be committed on successful
         * return or rolled back if we throw an exception.
         */
        this.session = SessionFactoryUtils.doGetSession(sessionFactory, false);

        synonymManager.startSession(session);
        taxonomicDivision = emblFile.getTaxonomicDivision();
        logger.trace("taxonomicDivision = " + taxonomicDivision);

        if (topLevelFeature == null) {
            logger.info("Creating topLevelFeature: " + emblFile.getAccession());
            topLevelFeature = TopLevelFeature.make(topLevelFeatureClass, emblFile.getAccession(), organism);
            topLevelFeature.markAsTopLevelFeature();
            topLevelFeature.setResidues(emblFile.getSequence());
            session.persist(topLevelFeature);
        } else {
            topLevelFeature = (TopLevelFeature) session.merge(topLevelFeature);
        }

        init(topLevelFeature);
        EmblLocation.Join contigLocations = emblFile.getContigLocations();
        if (contigLocations != null) {
            loadContigsAndGaps(contigLocations);
        }
        loadFeatures(emblFile.getFeatureTable());
    }

    /**
     * This exception is thrown by the <code>getTopLevelFeature</code> method
     *
     * @author rh11
     *
     */
    private static class TopLevelFeatureException extends Exception {
        public TopLevelFeatureException(String message) {
            super(message);
        }
    }

    /**
     * Get the top-level feature onto which we should add our features, based on
     * the <code>overwriteExisting</code> policy. In the commonest case, overwriteExisting
     * will have its default value of <code>NO</code>, and this method will either return
     * </code>null</code> if the feature doesn't already exist or throw an exception if it
     * does.
     *
     * @param uniqueName the unique name of the top-level feature we are loading
     * @return the existing top-level feature to use, or <code>null</code> if we should create a new one.
     * @throws TopLevelFeatureException if there is a problem; i.e. the feature exists when it shouldn't,
     *                  or fails to exist (or isn't a top-level feature) when it should.
     */
    @Transactional(rollbackFor=TopLevelFeatureException.class) // Will also rollback for runtime exceptions, by default
    private TopLevelFeature getTopLevelFeature(String uniqueName)
            throws TopLevelFeatureException {

        Session session = SessionFactoryUtils.doGetSession(sessionFactory, false);
        Feature existingTopLevelFeature = (Feature) session.createCriteria(Feature.class)
            .add(Restrictions.eq("organism", organism))
            .add(Restrictions.eq("uniqueName", uniqueName))
            .uniqueResult();

        if (existingTopLevelFeature != null) {
            switch (overwriteExisting) {
            case YES:
                logger.trace(String.format("Deleting existing feature '%s' (ID=%d)",
                    existingTopLevelFeature.getUniqueName(), existingTopLevelFeature.getFeatureId()));

                existingTopLevelFeature.delete();
                break;
            case NO:
                throw new TopLevelFeatureException(String.format("The organism '%s' already has feature '%s'",
                    organism.getCommonName(), uniqueName));
            case MERGE:
                if (existingTopLevelFeature instanceof TopLevelFeature) {
                    return (TopLevelFeature) existingTopLevelFeature;
                } else {
                    throw new TopLevelFeatureException(String.format("We can't merge onto the feature '%s', because it's not a top-level feature",
                        existingTopLevelFeature.getUniqueName()));
                }
            }
        } else if (overwriteExisting == OverwriteExisting.MERGE) {
            throw new TopLevelFeatureException(String.format("Cannot MERGE because feature '%s' does not exist", uniqueName));
        }
        return null;
    }

    private String taxonomicDivision;
    private TopLevelFeature topLevelFeature;
    private Map<String,AbstractGene> genesByUniqueName;
    private Map<String,Transcript> transcriptsByUniqueName;
    private NavigableMap<Integer,Contig> contigsByStart;

    private void init(TopLevelFeature topLevelFeature) {
        if (topLevelFeature == null) {
            throw new IllegalArgumentException("topLevelFeature cannot be null");
        }
        this.topLevelFeature = topLevelFeature;
        this.genesByUniqueName = new HashMap<String,AbstractGene>();
        this.transcriptsByUniqueName = new HashMap<String,Transcript>();
        this.contigsByStart = new TreeMap<Integer,Contig>();
    }

    private void loadContigsAndGaps(EmblLocation.Join locations) throws DataError {
        int pos = 0; // Position (interbase) on topLevelFeature
        for(EmblLocation location: locations.locations) {
            if (location instanceof EmblLocation.External) {
                EmblLocation.External externalLocation = (EmblLocation.External) location;
                int contigLength = externalLocation.simple.getLength();

                logger.debug(String.format("Creating contig '%s' at %d-%d", externalLocation.accession, pos, pos + contigLength));
                Contig contig = TopLevelFeature.make(Contig.class, externalLocation.accession, organism);
                contig.setResidues(topLevelFeature.getResidues(pos, pos + contigLength));
                session.persist(contig);
                topLevelFeature.addLocatedChild(contig, pos, pos + contigLength, (short) 0, 0);

                contigsByStart.put(pos, contig);

                pos += contigLength;
            } else if (location instanceof EmblLocation.Gap) {
                EmblLocation.Gap gapLocation = (EmblLocation.Gap) location;
                int gapLength = gapLocation.getLength();

                logger.debug(String.format("Creating gap at %d-%d", pos, pos + gapLength));
                Gap gap = topLevelFeature.addGap(pos, pos + gapLength);
                session.persist(gap);

                pos += gapLength;
            } else {
                throw new DataError("The CO section should contain only external references and gaps");
            }
        }
    }

    private void locate(Feature feature, EmblLocation location) {
        locate(feature, location.getFmin(), location.getFmax(), (short) location.getStrand(), null);
    }

    private void locate(Feature feature, int fmin, int fmax, short strand, Integer phase) {
        topLevelFeature.addLocatedChild(feature, fmin, fmax, strand, phase);
        Contig contig = contigsByStart.isEmpty() ? null : contigsByStart.floorEntry(fmin).getValue();
        if (contig == null || fmax > contig.getFmax()) {
            logger.debug(String.format("The feature '%s' (%s) is not contained in a contig",
                feature.getUniqueName(), feature.getName()));
            return;
        }
        logger.debug(String.format("The feature '%s' lies on contig '%s'", feature.getUniqueName(), contig.getUniqueName()));
        contig.addLocatedChild(feature, fmin - contig.getFmin(), fmax - contig.getFmin(), strand, phase, 1, 1);
    }

    private void loadFeatures(FeatureTable featureTable) throws DataError {
        List<FeatureTable.Feature> utrs = new ArrayList<FeatureTable.Feature>();

        if (featureTable == null) {
            logger.error("No feature table found!");
            return;
        }

        for (FeatureTable.Feature feature: featureTable.getFeatures()) {
            try {
                loadFeature(utrs, feature);
            }
            catch (DataError e) {
                e.setLineNumber(feature.lineNumber);
                if (continueOnError) {
                    logger.error("Continuing after error", e);
                } else {
                    throw e;
                }
            }
        }

        for (FeatureTable.Feature utr: utrs) {
            try {
                loadUTR(utr);
            }
            catch (DataError e) {
                logger.debug("Caught DataError while loading UTR. Setting line number to " + utr.lineNumber);
                e.setLineNumber(utr.lineNumber);
                throw e;
            }
        }
    }

    private void loadFeature(List<FeatureTable.Feature> utrs,
            FeatureTable.Feature feature) throws DataError {
        String featureType = feature.type;

        if (feature.location.getFmax() < feature.location.getFmin()) {
            throw new DataError("Location has fmax before fmin");
        }

        if (featureType.equals("repeat_region")) {
            loadRepeatRegion(feature);
        }
        else if (featureType.equals("CDS")) {
            loadCDS((FeatureTable.CDSFeature) feature);
        }
        else if (featureType.equals("tRNA")) {
            loadTRNA(feature);
        }
        else if (featureType.equals("rRNA")) {
            loadRRNA(feature);
        }
        else if (featureType.equals("snRNA")) {
            loadSnRNA(feature);
        }
        else if (featureType.equals("snoRNA")) {
            loadSnoRNA(feature);
        }
        else if (featureType.equals("misc_RNA")) {
            loadNcRNA(feature);
        }
        else if (featureType.equals("3'UTR") || featureType.equals("5'UTR")) {
            utrs.add(feature);
        }
        else if (featureType.equals("LTR")) {
            throw new DataError("Tell Robin he needs to write code for loading LTR features!");
            // TODO
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

        String repeatType = repeatRegionFeature.getQualifierValue("rpt_type");
        final Class<? extends RepeatRegion> repeatRegionClass;
        if (repeatType == null) {
            repeatRegionClass = RepeatRegion.class;
        } else if (repeatType.equals("direct")) {
            repeatRegionClass = DirectRepeatRegion.class;
        } else {
            throw new DataError(String.format("Unknown repeat type '%s'", repeatType));
        }

        logger.debug(String.format("Creating repeat region '%s' of type '%s' at %d-%d",
            repeatRegionName, repeatRegionClass.getSimpleName(), fmin, fmax));
        RepeatRegion repeatRegion = RepeatRegion.make(repeatRegionClass,
            organism, String.format("%s:repeat:%d-%d", topLevelFeature.getUniqueName(), fmin, fmax),
            repeatRegionName);

        int rank = 0;
        for(String note : repeatRegionFeature.getQualifierValues("note")) {
            repeatRegion.addFeatureProp(note, "feature_property", "comment", rank++);
        }

        session.persist(repeatRegion);
        locate(repeatRegion, fmin, fmax, (short)0, 0);
    }


    // Can't define static fields in inner classes, grr.
    private static final Set<String> goQualifiers = new HashSet<String>();
    static {
        Collections.addAll(goQualifiers, "aspect", "GOid", "term", "qualifier", "evidence", "db_xref", "with", "date");
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

        /**
         * The focal feature is the one to which annotation is added.
         * This is the polypeptide where possible, or the transcript if
         * there is no polypeptide.
         */
        protected Feature focalFeature;
        protected Integer phase = null;

        public GeneLoader(FeatureTable.Feature feature) {
            this.feature  = feature;
            this.location = feature.location;
        }

        protected Class<? extends AbstractGene> getGeneClass() {
            return isPseudo ? Pseudogene.class : Gene.class;
        }

        protected abstract Class<? extends Transcript> getTranscriptClass();

        protected String getTranscriptType() {
            /*
             * This assumes that transcript feature classes are annotated with term
             * rather than accession, which is true at the time of writing.
             */
            return FeatureTypeUtils.getFeatureTypeForClass(getTranscriptClass()).term();
        }

        /**
         * The main entry point to a gene loader.
         */
        public void load() throws DataError {
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

        private void loadTranscript(AbstractGene gene) throws DataError {
            logger.debug(String.format("Creating transcript '%s' for gene '%s'", transcriptUniqueName, gene.getUniqueName()));
            if (APPEND_TYPE_TO_TRANSCRIPT_UNIQUENAME) {
                this.transcript = gene.makeTranscript(getTranscriptClass(),
                    String.format("%s:%s", transcriptUniqueName, getTranscriptType()), location.getFmin(), location.getFmax());
            } else {
                this.transcript = gene.makeTranscript(getTranscriptClass(), transcriptUniqueName, location.getFmin(), location.getFmax());
            }

            focalFeature = transcript;
            if (transcript instanceof ProductiveTranscript) {
                Polypeptide polypeptide = ((ProductiveTranscript) transcript).getProtein();
                if (polypeptide != null) {
                    focalFeature = polypeptide;
                }
            }

            transcriptsByUniqueName.put(transcriptUniqueName, transcript);
            loadExons();
            processTranscriptQualifiers();
        }

        /**
         * For each <code>/&lt;qualifierName&gt;</code> qualifier, add a synonym of type
         * <code>synonymType</code> to the transcript.
         *
         * @param qualifierName the name of the qualifer
         * @param synonymType the type of synonym. Should be a term in the <code>genedb_synonym_type</code> CV
         * @param isCurrent whether the synonym is current or not
         */
        protected void addTranscriptSynonymsFromQualifier(String qualifierName, String synonymType, boolean isCurrent) {
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
         * For each <code>/&lt;qualifierName&gt;</code> qualifier, add a property of
         * the specified type to the polypeptide, if there is one, or else to the
         * transcript.
         *
         * @param qualifierName the qualifier name
         * @param propertyCvName the name of the CV to which the property term belongs.
         *          Should be either <code>feature_property</code> for built-in Chado
         *          properties, or <code>genedb_misc</code> for local additions.
         * @param propertyTermName the term name corresponding to the property to add.
         *          If it belongs to the <code>genedb_misc</code> CV, it should be a child of
         *          the term <code>genedb_misc:feature_props</code>.
         * @param isUnique whether this qualifier may appear only once.
         * @return the number of properties that were added
         * @throws DataError
         */
        protected int processPropertyQualifier(String qualifierName, String propertyCvName, String propertyTermName, boolean isUnique) throws DataError {
            return processPropertyQualifier(qualifierName, propertyCvName, propertyTermName, null, isUnique);
        }

        protected int processPropertyQualifier(String qualifierName, String propertyCvName, String propertyTermName) throws DataError {
            return processPropertyQualifier(qualifierName, propertyCvName, propertyTermName, null, false);
        }

        private int processPropertyQualifier(String qualifierName, String propertyCvName, String propertyTermName,
                TermNormaliser normaliser, boolean isUnique) throws DataError {
            Set<String> values = new HashSet<String>();
            int rank = 0;
            for(String qualifierValue: feature.getQualifierValues(qualifierName)) {
                String normalisedValue = qualifierValue;
                if (normaliser != null) {
                    normalisedValue = normaliser.normalise(qualifierValue);
                }
                if (values.contains(normalisedValue)) {
                    logger.warn(String.format("Qualifier /%s=\"%s\" appears more than once on feature at line %d. Ignoring subsequent occurrences.",
                        qualifierName, normalisedValue, this.feature.lineNumber));
                } else {
                    if (isUnique && !values.isEmpty()) {
                        throw new DataError(String.format("More than one /%s qualifier found", qualifierName));
                    }

                    logger.debug(String.format("Adding %s:%s '%s' for transcript",
                                    propertyCvName, propertyTermName, normalisedValue));
                    values.add(normalisedValue);
                    focalFeature.addFeatureProp(normalisedValue, propertyCvName, propertyTermName, rank++);
                }
            }
            return rank;
        }

        protected void processCvTermQualifier(String qualifierName, String cvName, boolean createTerms)
                throws DataError {
            processCvTermQualifier(qualifierName, cvName, createTerms, null);
        }

        private void processCvTermQualifier(String qualifierName, String cvName,
                boolean createTerms, TermNormaliser termNormaliser)
                throws DataError {

            Set<String> terms = new HashSet<String>();
            for (String term: feature.getQualifierValues(qualifierName)) {
                String normalisedTerm = term;
                if (termNormaliser != null) {
                    normalisedTerm = termNormaliser.normalise(term);
                }

                if (terms.contains(normalisedTerm)) {
                    logger.warn(
                        String.format("The qualifier /%s=\"%s\" appears more than once. Ignoring subsequent copies.",
                            qualifierName, term));
                    continue;
                } else {
                    terms.add(normalisedTerm);
                }

                FeatureCvTerm featureCvTerm = focalFeature.addCvTerm(cvName, normalisedTerm, createTerms);
                if (featureCvTerm == null) {
                    throw new DataError(
                        String.format("Failed to find term '%s' in CV '%s'", normalisedTerm, cvName));
                }
                session.persist(featureCvTerm);
            }
        }

        private void loadExons() throws DataError {
            int exonIndex = 0;
            for (EmblLocation exonLocation: location.getParts()) {
                if (exonLocation instanceof EmblLocation.External) {
                    throw new DataError("Found an external exon (trans-splicing). We can't handle that yet.");
                }
                String exonUniqueName = String.format("%s:exon:%d", transcriptUniqueName, ++exonIndex);
                logger.debug(String.format("Creating exon '%s' at %d-%d", exonUniqueName, exonLocation.getFmin(), exonLocation.getFmax()));
                AbstractExon exon = transcript.createExon(exonUniqueName, exonLocation.getFmin(), exonLocation.getFmax(), phase);
                session.persist(exon);
            }
        }

        protected void processGO() throws DataError {
            for (String go: feature.getQualifierValues("GO")) {
                GoInstance goInstance = new GoInstance();
                for (String subqualifier: go.split("; ?")) {
                    subqualifier = subqualifier.trim();
                    if (subqualifier.length() == 0) {
                        continue;
                    }
                    int equalsIndex = subqualifier.indexOf('=');
                    if (equalsIndex == -1) {
                        throw new DataError(String.format("Failed to parse /GO=\"%s\"", go));
                    }

                    String key = subqualifier.substring(0, equalsIndex);
                    String value = subqualifier.substring(equalsIndex + 1);
                    if (!goQualifiers.contains(key)) {
                        throw new DataError(String.format("Failed to parse /GO=\"%s\"; don't know what to do with %s=%s", go, key, value));
                    }
                    if (key.equals("GOid")) {
                        goInstance.setId(value);
                    } else if (key.equals("date")) {
                        goInstance.setDate(value);
                    } else if (key.equals("evidence")) {
                        try {
                            goInstance.setEvidence(GoEvidenceCode.valueOf(value));
                        } catch (IllegalArgumentException e) {
                            throw new DataError(String.format("Failed to parse GO evidence code '%s'", value));
                        }
                    } else if (key.equals("qualifier")) {
                        goInstance.addQualifier(value);
                    } else if (key.equals("with")) {
                        goInstance.setWithFrom(value);
                    }
                }
                featureUtils.createGoEntries(focalFeature, goInstance, "From EMBL file", (DbXRef) null);
            }
        }


        /* Here are some examples of /similarity qualifiers from chromosome 1 of Trypanosoma brucei:

            FT                   /similarity="blastp; SWALL:Q26723 (EMBL:M20871);
            FT                   Trypanosoma brucei brucei; variant-specific antigen;
            FT                   ESAG3; ; id=70%; ; E()=2e-42; score=438; ; ;"

            FT                   /similarity="fasta; SWALL:P26328 (EMBL:56768); Trypanosoma
            FT                   brucei brucei; variant surface glycoprotein ILTat 1.23
            FT                   precursor; ; length 532 aa; id=30.35%; ungapped id=32.34%;
            FT                   E()=1.2e-34; ; 537 aa overlap; query 9-528 aa; subject
            FT                   10-530 aa"

            FT                   /similarity="fasta; SWALL:O97352 (EMBL:AJ012199);
            FT                   Trypanosoma brucei; ILTat 1.61 metacyclic VSG protein; ;
            FT                   length 518 aa; id=29.83%; ungapped id=32.99%; E()=3.6e-31;
            FT                   ; 543 aa overlap; query 2-528 aa; subject 10-516 aa"

            FT                   /similarity="blastp; SWALL:Q8WPR3 (EMBL:AL671259);
            FT                   Trypanosoma brucei; ESAG3; H25N7.29; ; id=74%; ;
            FT                   E()=4e-28; score=310; ; ;"

          And here are some examples from Schistosoma mansoni Smp_scaff000604, to show how minimal
          the provided data can sometimes be:

            FT                   /similarity="blastp; RF:XP_970827.1; ; ; ; ; id=61.0%; ;
            FT                   E()=3.9e-21; ; ; ;"

            FT                   /similarity="blastp; RF:NP_956088.1; ; ; ; ; id=58.4%; ;
            FT                   E()=1.6e-17; ; ; ;"

            FT                   /similarity="blastp; GB:BAD74067.1; ; ; ; ; id=54.4%; ;
            FT                   E()=2.6e-17; ; ; ;"

        And from Leishmania major chromosome 32, to show the use of multiple secondary cross-references:

            FT                   /similarity="fasta; SWALL:Q9BUG7 (EMBL:BC002634,
            FT                   AAH02634); Homo sapiens; hypothetical protein; ; length
            FT                   322 aa; id=40.063%; ungapped id=46.691%; E()=1.2e-32; ;
            FT                   301 aa overlap; query 23-324 aa; subject 12-298 aa"

       I don't know how normal this is, but there's at least one with a line-break in the
       middle of the E() value! (Again from Leishmania major, chromosome 32.)

            FT                   /similarity="fasta; SWALL:EAA26969 (EMBL:AABX01000759,
            FT                   EAA26969); Neurospora crassa; hypothetical protein; ;
            FT                   length 335 aa; id=38.462%; ungapped id=47.273%; E()=6.4e-
            FT                   17; ; 303 aa overlap; query 3-306 aa; subject 13-321 aa"

         */
        private final Pattern similarityPattern = Pattern.compile(
            "(\\w+);" +                                                 // 1.     Algorithm, e.g. fasta, blastp
            "\\s*(\\w+):([\\w.]+)" +                                    // 2,3.   Primary dbxref, e.g. SWALL:Q26723
            "(?:\\s+\\((\\w+):([\\w.]+(?:,\\s*[\\w.]+)*)\\))?;" +       // 4,5.   Optional secondary dbxrefs, e.g. "EMBL:M20871", "EMBL:BC002634, AAH02634"
            "\\s*([^;]+)?;" +                                           // 6.     Organism name
            "\\s*([^;]+)?;" +                                           // 7.     Product name
            "\\s*([^;]+)?;" +                                           // 8.     Gene name
            "\\s*(?:length (\\d+) aa)?;" +                              // 9.     Optional match length
            "\\s*id=(\\d{1,2}(?:\\.\\d{1,3})?)%;" +                     // 10.    Degree of identity (percentage)
            "\\s*(?:ungapped id=(\\d{1,2}(?:\\.\\d{1,3})?)%)?;" +       // 11.    Optional ungapped identity (percentage)
            "\\s*E\\(\\)=(\\d+(?:\\.\\d+)?(?:e[+-]? ?\\d+)?);" +        // 12.    E-value
            "\\s*(?:score=(\\d+))?;" +                                  // 13.    Optional score
            "\\s*(?:(\\d+) aa overlap)?;" +                             // 14.    Optional overlap length (integer)
            "\\s*(?:query (\\d+)-(\\d+) aa)?;" +                        // 15,16. Optional query location
            "\\s*(?:subject (\\d+)-(\\d+) aa)?");                       // 17,18. Optional subject location

        protected void processSimilarityQualifiers() throws DataError {
            for (String similarityString: feature.getQualifierValues("similarity")) {
                processSimilarityQualifier(similarityString);
            }
        }

        private void processSimilarityQualifier(String similarityString) throws DataError {
            Matcher matcher = similarityPattern.matcher(similarityString);
            if (!matcher.matches()) {
                throw new DataError(String.format("Failed to parse /similarity=\"%s\"", similarityString));
            }

            Similarity similarity = new Similarity();
            similarity.setAnalysisProgram(matcher.group(1));
            DbXRef primaryDbXRef = objectManager.getDbXRef(matcher.group(2), matcher.group(3));
            if (primaryDbXRef == null) {
                throw new DataError(String.format("Could not find database '%s' for primary dbxref of /similarity", matcher.group(2)));
            }
            similarity.setPrimaryDbXRef(primaryDbXRef);

            if (matcher.group(4) != null) {
                String dbName = matcher.group(4);
                for (String accession: matcher.group(5).split(",\\s*")) {
                    DbXRef secondaryDbXRef = objectManager.getDbXRef(dbName, accession);
                    if (secondaryDbXRef == null) {
                        throw new DataError(String.format("Could not find database '%s' for secondary dbxref of /similarity", matcher.group(4)));
                    }
                    similarity.addDbXRef(secondaryDbXRef);
                }
            }

            // These three may be null, which is okay
            similarity.setOrganismName(matcher.group(6));
            similarity.setProduct(matcher.group(7));
            similarity.setGeneName(matcher.group(8));

            if (matcher.group(9) != null) {
                try {
                    similarity.setLength(Integer.parseInt(matcher.group(9)));
                } catch (NumberFormatException e) {
                    throw new DataError("Failed to parse length field of /similarity: " + matcher.group(9));
                }
            }

            try {
                similarity.setId(Double.parseDouble(matcher.group(10)));
            } catch (NumberFormatException e) {
                throw new DataError("Failed to parse id field of /similarity: " + matcher.group(10));
            }

            if (matcher.group(11) != null) {
                try {
                    similarity.setUngappedId(Double.parseDouble(matcher.group(11)));
                } catch (NumberFormatException e) {
                    throw new DataError("Failed to parse ungapped id field of /similarity: " + matcher.group(11));
                }
            }

            try {
                similarity.setEValue(Double.parseDouble(matcher.group(12).replaceAll("\\s+", "")));
            } catch (NumberFormatException e) {
                throw new DataError("Failed to parse E() field of /similarity: " + matcher.group(12));
            }

            if (matcher.group(13) != null) {
                try {
                    similarity.setRawScore(Double.parseDouble(matcher.group(13)));
                } catch (NumberFormatException e) {
                    throw new DataError("Failed to parse score field of /similarity: " + matcher.group(13));
                }
            }

            if (matcher.group(14) != null) {
                try {
                    similarity.setOverlap(Integer.parseInt(matcher.group(14)));
                } catch (NumberFormatException e) {
                    throw new DataError("Failed to parse score field of /similarity: " + matcher.group(13));
                }
            }

            if (matcher.group(15) != null) {
                try {
                    similarity.setQueryStart(Integer.parseInt(matcher.group(15)));
                    similarity.setQueryEnd(Integer.parseInt(matcher.group(16)));
                } catch (NumberFormatException e) {
                    throw new DataError(String.format("Failed to parse query location of /similarity: %s-%s", matcher.group(15), matcher.group(16)));
                }
            }

            if (matcher.group(17) != null) {
                try {
                    similarity.setTargetStart(Integer.parseInt(matcher.group(17)));
                    similarity.setTargetEnd(Integer.parseInt(matcher.group(18)));
                } catch (NumberFormatException e) {
                    throw new DataError(String.format("Failed to parse subject location of /similarity: %s-%s", matcher.group(17), matcher.group(18)));
                }
            }

            focalFeature.addSimilarity(similarity);
        }


        private final Pattern subqualifierPattern = Pattern.compile("\\G\\s*([^=]+)=\\s*([^;]*)\\s*(?:;|\\z)");

        protected void processCuration() throws DataError {
            processPropertyQualifier("curation", "genedb_misc", "curation");

            if (sloppyControlledCuration) {
                processControlledCurationSloppy();
            } else {
                processControlledCurationStrict();
            }
        }
        private void processControlledCurationSloppy() throws DataError {
            int rank = feature.getQualifierValues("curation").size();
            for (String controlledCuration: feature.getQualifierValues("controlled_curation")) {
                String curation = String.format("[%s]", controlledCuration);
                logger.trace(String.format("Sloppy curation: adding /curation=\"%s\" with rank %d", curation, rank));
                focalFeature.addFeatureProp(curation, "genedb_misc", "curation", rank++);

                Matcher matcher = subqualifierPattern.matcher(controlledCuration);
                while (matcher.find()) {
                    String key = matcher.group(1).toLowerCase();
                    String value = matcher.group(2);

                    if (key.equals("db_xref") && value.length() > 0) {
                        if (value.indexOf(":") < 0) {
                            value = "PMID:" + value;
                        }
                        logger.trace(String.format("Sloppy controlled_curation: adding dbxref for '%s'", value));
                        addDbXRefs(value);
                    }
                }
            }
        }

        private Set<String> subqualifiers = new HashSet<String>() {{
            Collections.addAll(this,
                "term", "cv", "qualifier", "evidence", "db_xref", "residue", "attribution", "date");
        }};

        private void processControlledCurationStrict() throws DataError {
            String controlledCuration = feature.getQualifierValue("controlled_curation");
            if (controlledCuration == null) {
                return;
            }

            Matcher matcher = subqualifierPattern.matcher(controlledCuration);
            Map<String, String> valuesByKey = new HashMap<String, String>();
            while (matcher.find()) {
                String key = matcher.group(1).toLowerCase();
                String value = matcher.group(2);

                if (subqualifiers.contains(key)) {
                    valuesByKey.put(key, value);
                }
            }

            if (!valuesByKey.containsKey("term")) {
                throw new DataError("/controlled_curation has no 'term' field");
            }
            String term = valuesByKey.get("term");
            String cv   = valuesByKey.containsKey("cv") ? valuesByKey.get("cv") : "genedb_misc";

            FeatureCvTerm featureCvTerm = focalFeature.addCvTerm(cv, term);

            featureCvTerm.addPropIfNotNull("feature_property", "date",   valuesByKey.get("date"));
            featureCvTerm.addPropIfNotNull("genedb_misc", "attribution", valuesByKey.get("attribution"));
            featureCvTerm.addPropIfNotNull("genedb_misc", "evidence",    valuesByKey.get("evidence"));
            featureCvTerm.addPropIfNotNull("genedb_misc", "qualifier",   valuesByKey.get("qualifier"));

            if (valuesByKey.containsKey("db_xref")) {
                addDbXRefs(valuesByKey.get("db_xref"));
            }
        }

        private Pattern dbxrefPattern = Pattern.compile("([^:]+):(.*)");
        /**
         * Add DbXRefs.
         * @param dbxrefs a pipe-separated list of <code>db:accession</code>
         * @throws DataError
         */
        protected void addDbXRefs(String dbxrefs) throws DataError {
            for (String dbxref: dbxrefs.split("\\|")) {
                Matcher matcher = dbxrefPattern.matcher(dbxref);
                if (!matcher.matches()) {
                    throw new DataError(String.format("db_xref '%s' is not of the form database:accession", dbxref));
                }
                String dbName = matcher.group(1);
                String accession = matcher.group(2);
                Db db = generalDao.getDbByName(dbName);
                if (db == null) {
                    throw new DataError(String.format("Unrecognised database '%s' in dbxref", dbName));
                }
                DbXRef dbXRef = new DbXRef(db, accession);
                focalFeature.addDbXRef(dbXRef);
            }
        }

        /**
         * Use the qualifiers of the CDS feature to add various bits of annotation
         * to the transcript (or to the polypeptide, if there is one). Specifically,
         * add synonyms, properties and products.
         */
        protected void processTranscriptQualifiers() throws DataError {

            checkForTemporarySystematicIdEqualToSystematicId();

            addTranscriptSynonymsFromQualifier("synonym", "synonym", true);
            addTranscriptSynonymsFromQualifier("previous_systematic_id", "systematic_id", false);
            addTranscriptSynonymsFromQualifier("systematic_id", "systematic_id", true);
            addTranscriptSynonymsFromQualifier("temporary_systematic_id", "temporary_systematic_id", true);

            int commentRank = processPropertyQualifier("note",     "feature_property", "comment");
            for (String name: qualifierProperties) {
                TermNormaliser normaliser = qualifierNormalisers.get(name);
                processPropertyQualifier(name, "genedb_misc", name, normaliser, uniqueQualifiers.contains(name));
            }

            addColourToExons();

            processCvTermQualifier("class", "RILEY", false, normaliseRileyNumber);
            processCvTermQualifier("product", "genedb_products", true);

            if (taxonomicDivision.equals("PRO")) {
                // Bacteria don't have splicing, so a CDS feature is a gene and
                // a transcript and that is the end of it. One or more /gene
                // or /synonym qualifiers may be used to indicate synonyms.
                addTranscriptSynonymsFromQualifier("gene", "synonym", true);
            }

            if (feature.hasQualifier("partial")) {
                logger.trace(String.format("Marking feature '%s' as partial", focalFeature.getUniqueName()));
                focalFeature.addFeatureProp("partial", "feature_property", "comment", commentRank++);
            }

            processGO();
            processSimilarityQualifiers();
            processCuration();
        }

        private void addColourToExons() throws DataError {
            String colour = feature.getQualifierValue("colour");
            if (colour == null) {
                return;
            }

            TermNormaliser colourNormaliser = qualifierNormalisers.get("colour");
            String normalisedColour = colour;
            if (colourNormaliser != null) {
                normalisedColour = colourNormaliser.normalise(colour);
            }

            logger.trace(String.format("Adding /colour=\"%s\" to exons of '%s'", normalisedColour, transcript.getUniqueName()));

            for (AbstractExon exon: transcript.getExons()) {
                logger.trace(String.format("Adding /colour=\"%s\" to exon '%s'", normalisedColour, exon.getUniqueName()));
                exon.addFeatureProp(normalisedColour, "genedb_misc", "colour", 0);
            }
        }

        /**
         * Explicitly check for the case where /previous_systematic_id is equal to the
         * actual systematic ID, because otherwise the resulting constraint violation
         * error is difficult to understand and track down.
         * @throws DataError if so
         */
        private void checkForTemporarySystematicIdEqualToSystematicId() throws DataError {
            String systematicId = feature.getQualifierValue("systematic_id");
            if (systematicId == null) {
                return;
            }
            for (String temporarySystematicId: feature.getQualifierValues("temporary_systematic_id")) {
                if (systematicId.equals(temporarySystematicId)) {
                    throw new DataError("Feature has /temporary_systematic_id with the same value as /systematic_id");
                }
            }
        }
    }


    /**
     * Convert the qualifier value into a canonical form before treating it as
     * a term name. What this means will depend on the specific normalizer used.
     */
    private static interface TermNormaliser {
        public String normalise(String term) throws DataError;
    }

    /**
     * A term normaliser for Riley classification numbers, which for example
     * will normalise "2.2.07" to "2.2.7".
     */
    private static final TermNormaliser normaliseRileyNumber = new TermNormaliser() {
        private final Pattern RILEY_PATTERN = Pattern.compile("(\\d{1,2})\\.(\\d{1,2})\\.(\\d{1,2})");
        public String normalise(String term) throws DataError {
            Matcher matcher = RILEY_PATTERN.matcher(term);
            if (!matcher.matches()) {
                throw new DataError(String.format("Failed to parse Riley number '%s'", term));
            }
            return String.format("%d.%d.%d",
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)));
        }
    };

    /**
     * A term normaliser (and format validator) for integers.
     */
    private static final TermNormaliser normaliseInteger = new TermNormaliser() {
        public String normalise(String term) throws DataError {
            try {
                return String.valueOf(Integer.parseInt(term));
            } catch (NumberFormatException e) {
                throw new DataError(String.format("Failed to parse integer '%s'", term));
            }
        }
    };

    /**
     * A list of the qualifiers that correspond directly to similarly-named
     * properties in the <code>genedb_misc</code> CV.
     */
    private static final List<String> qualifierProperties = new ArrayList<String>();
    private static final Map<String,TermNormaliser> qualifierNormalisers = new HashMap<String,TermNormaliser>();
    private static final Set<String> uniqueQualifiers = new HashSet<String>();
    static {
        Collections.addAll(qualifierProperties,
            "method", "colour", "status",
            "blast_file", "blastn_file", "blastp+go_file", "blastp_file",
            "blastx_file", "fasta_file", "fastx_file", "tblastn_file",
            "tblastx_file", "clustalx_file", "sigcleave_file", "pepstats_file",
            "EC_number");

        Collections.addAll(uniqueQualifiers, "colour", "status");

        qualifierNormalisers.put("colour", normaliseInteger);

        // Some files (e.g. Streptococcus_pneumoniae_D39.embl) have things other than integers in /status.
        // qualifierNormalisers.put("status", normaliseInteger);
    }

    class CDSLoader extends GeneLoader {
        public CDSLoader(FeatureTable.CDSFeature cdsFeature) throws DataError {
            super(cdsFeature);

            isPseudo = cdsFeature.isPseudo();
            geneUniqueName = cdsFeature.getSharedId();
            transcriptUniqueName = cdsFeature.getUniqueName();

            if (taxonomicDivision.equals("PRO")) {
                // Bacteria don't have splicing, so a CDS feature is a gene and
                // a transcript and that is the end of it. One or more /gene
                // qualifiers may be used to indicate synonyms. The primary_name
                // is optional, as usual.
                geneName = cdsFeature.getQualifierValue("primary_name");
            } else {
                geneName = cdsFeature.getGeneName();
            }

            String codonStart = cdsFeature.getQualifierValue("codon_start");
            if (codonStart != null) {
                try {
                    phase = Integer.parseInt(codonStart) - 1;
                } catch (NumberFormatException e) {
                    throw new DataError(
                        String.format("Could not parse value of /codon_start qualifier ('%s')", codonStart));
                }
                if (phase < 0 || phase > 2) {
                    throw new DataError(
                        String.format("Value of /codon_start qualifier out of range (%d)", phase+1));
                }
            }

            singlySpliced = false;
            if (geneUniqueName == null) {
                singlySpliced = true;
                geneUniqueName = transcriptUniqueName;
            }
        }


        @Override
        protected Class<? extends Transcript> getTranscriptClass() {
            return isPseudo ? PseudogenicTranscript.class : MRNA.class;
        }
    }

    private void loadCDS(FeatureTable.CDSFeature cdsFeature) throws DataError {
        new CDSLoader(cdsFeature).load();
    }


    /* Loader for non-coding RNA features */
    private Map<String,Integer> syntheticNcRNAIndexByType = new HashMap<String,Integer>();
    private class NcRNALoader extends GeneLoader {
        private Class<? extends NcRNA> transcriptClass;
        private String type;
        public NcRNALoader(Class<? extends NcRNA> transcriptClass, String type,
                FeatureTable.Feature feature)
            throws DataError
        {
            super(feature);
            this.transcriptClass = transcriptClass;
            this.type = type;

            geneUniqueName = transcriptUniqueName = feature.getUniqueName(false);
            if (geneUniqueName == null) {
                geneUniqueName = transcriptUniqueName = makeSyntheticName();
            }
        }

        private String makeSyntheticName() {
            if (syntheticNcRNAIndexByType.containsKey(type)) {
                syntheticNcRNAIndexByType.put(type, 1 + syntheticNcRNAIndexByType.get(type));
            } else {
                syntheticNcRNAIndexByType.put(type, 1);
            }
            return String.format("%s_%s%d",
                topLevelFeature.getUniqueName(), type, syntheticNcRNAIndexByType.get(type));
        }

        @Override
        protected void processTranscriptQualifiers() throws DataError {
            processPropertyQualifier("note",  "feature_property", "comment");
            if (TRNA.class.isAssignableFrom(transcriptClass)) {
                processPropertyQualifier("anticodon", "feature_property", "anticodon", true);
            }
            processCvTermQualifier("product", "genedb_products", true);

            if (taxonomicDivision.equals("PRO")) {
                // Bacteria don't have splicing, so a CDS feature is a gene and
                // a transcript and that is the end of it. One or more /gene
                // qualifiers may be used to indicate synonyms.
                addTranscriptSynonymsFromQualifier("gene",    "synonym", true);
                addTranscriptSynonymsFromQualifier("synonym", "synonym", true);
            }

            processCuration();
        }

        @Override
        protected Class<? extends Transcript> getTranscriptClass() {
            return transcriptClass;
        }
    }
    private void loadNcRNA(FeatureTable.Feature feature) throws DataError {
        new NcRNALoader(NcRNA.class, "ncRNA", feature).load();
    }
    private void loadRRNA(FeatureTable.Feature feature) throws DataError {
        new NcRNALoader(RRNA.class, "rRNA", feature).load();
    }
    private void loadTRNA(FeatureTable.Feature feature) throws DataError {
        new NcRNALoader(TRNA.class, "tRNA", feature).load();
    }
    private void loadSnRNA(FeatureTable.Feature feature) throws DataError {
        new NcRNALoader(SnRNA.class, "snRNA", feature).load();
    }
    private void loadSnoRNA(FeatureTable.Feature feature) throws DataError {
        new NcRNALoader(SnoRNA.class, "snoRNA", feature).load();
    }


    /* UTR */
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

        int part = 1;
        List<EmblLocation> utrParts = utrLocation.getParts();
        for (EmblLocation utrPartLocation: utrParts) {
            String utrUniqueName = String.format("%s:%dutr", uniqueName, utrClass == ThreePrimeUTR.class ? 3 : 5);
            if (utrParts.size() > 1) {
                utrUniqueName += ":" + part;
            }

            logger.debug(String.format("Creating %s feature '%s' at %d-%d",
                utrType, utrUniqueName, utrPartLocation.getFmin(), utrPartLocation.getFmax()));

            UTR utr = transcript.createUTR(utrClass, utrUniqueName, utrPartLocation.getFmin(), utrPartLocation.getFmax());
            session.persist(utr);
            ++ part;
        }
    }

    /* Setters and Spring stuff */
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

    public void setFeatureUtils(FeatureUtils featureUtils) {
        this.featureUtils = featureUtils;
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
