package org.genedb.db.loading;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.GeneralDao;
import org.genedb.db.dao.OrganismDao;
import org.genedb.db.dao.PubDao;
import org.genedb.util.Counters;
import org.genedb.util.IterableArray;

import org.gmod.schema.cfg.FeatureTypeUtils;
import org.gmod.schema.feature.AbstractExon;
import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Centromere;
import org.gmod.schema.feature.Contig;
import org.gmod.schema.feature.DirectRepeatRegion;
import org.gmod.schema.feature.FivePrimeUTR;
import org.gmod.schema.feature.Gap;
import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.InvertedRepeatRegion;
import org.gmod.schema.feature.MRNA;
import org.gmod.schema.feature.NcRNA;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.PolypeptideMotif;
import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.Pseudogene;
import org.gmod.schema.feature.PseudogenicTranscript;
import org.gmod.schema.feature.RRNA;
import org.gmod.schema.feature.Region;
import org.gmod.schema.feature.RepeatRegion;
import org.gmod.schema.feature.RepeatUnit;
import org.gmod.schema.feature.SECISElement;
import org.gmod.schema.feature.SnRNA;
import org.gmod.schema.feature.SnoRNA;
import org.gmod.schema.feature.Supercontig;
import org.gmod.schema.feature.TRNA;
import org.gmod.schema.feature.ThreePrimeUTR;
import org.gmod.schema.feature.TopLevelFeature;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.feature.UTR;
import org.gmod.schema.mapped.Analysis;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureCvTerm;
import org.gmod.schema.mapped.HasPubsAndDbXRefs;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.mapped.Pub;
import org.gmod.schema.mapped.PubDbXRef;
import org.gmod.schema.mapped.Synonym;
import org.gmod.schema.utils.ObjectManager;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
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
     * A unique number should be appended to the uniquename of the singly
     * spliced transcript features in the database. We must do this because
     * Artemis requires each feature to have a globally unique name, and does
     * not work correctly if a transcript has the same uniquename as its gene.
     * Similarly the GFF3 feature format requires feature names to be
     * globally unique, and we want to be able to export our data in GFF3 format.
     *
     * For alternatively-spliced genes, on the other hand, there is no need to
     * append the transcript type, because the transcript will have an assigned
     * uniquename (the /systematic_id of the CDS) that is different from the
     * uniquename of the gene (the /shared_id of the CDS).
     */
    //private enum AppendType { ALWAYS, NEVER, SINGLY_SPLICED_ONLY };
    //private static final AppendType APPEND_TYPE_TO_TRANSCRIPT_UNIQUENAME = AppendType.SINGLY_SPLICED_ONLY;

    // Injected beans
    private CvDao cvDao;
    private GeneralDao generalDao;
    private OrganismDao organismDao;
    private PubDao pubDao;
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
    private boolean reportUnusedQualifiers = true;
    private boolean goTermErrorsAreNotFatal = false;

    private Collection<String> ignoredFeatures = new HashSet<String>();
    private Collection<String> ignoredQualifiers = new HashSet<String>();
    private Map<String,Collection<String>> ignoredQualifiersByFeatureType = new HashMap<String,Collection<String>>();

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

    public OverwriteExisting getOverwriteExisting() {
        return this.overwriteExisting;
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
     * Whether GO term errors - in particular the case where the database does not contain
     * a term with the specified accession number - should be logged and ignored rather than
     * fatal.
     *
     * @param goTermErrorsAreNotFatal
     */
    public void setGoTermErrorsAreNotFatal(boolean goTermErrorsAreNotFatal) {
        this.goTermErrorsAreNotFatal = goTermErrorsAreNotFatal;
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

    /**
     * Whether we should log a list of unused qualifiers once the file has been loaded.
     * If set to true, this list is logged as a series of WARN messages, one for each
     * type of feature encountered in the file that has unused qualifiers. The default
     * value is <code>true</code>.
     *
     * @param reportUnusedQualifiers
     */
    public void setReportUnusedQualifiers(boolean reportUnusedQualifiers) {
        this.reportUnusedQualifiers = reportUnusedQualifiers;
    }

    /**
     * Ignore features of the named type.
     *
     * @param feature the name of the feature type to ignore
     */
    public void ignoreFeature(String featureType) {
        ignoredFeatures.add(featureType);
    }

    /**
     * Ignore the named qualifier.
     *
     * @param qualifier the name of the qualifier to ignore
     */
    public void ignoreQualifier(String qualifier) {
        ignoredQualifiers.add(qualifier);
    }

    /**
     * Ignore the named qualifier when it appears on a feature of the specified type.
     *
     * @param qualifier the name of the qualifier to ignore
     * @param featureType the type of feature on which to ignore the named qualifier
     */
    public void ignoreQualifier(String qualifier, String featureType) {
        synchronized(ignoredQualifiersByFeatureType) {
            if (!ignoredQualifiersByFeatureType.containsKey(featureType)) {
                ignoredQualifiersByFeatureType.put(featureType, new HashSet<String>());
            }
            ignoredQualifiersByFeatureType.get(featureType).add(qualifier);
        }
    }

    private void propagateIgnoreFeaturesAndQualifiers(FeatureTable featureTable) {
        for (String featureType: ignoredFeatures) {
            featureTable.ignoreFeature(featureType);
        }
        for (String qualifier: ignoredQualifiers) {
            featureTable.ignoreQualifier(qualifier);
        }
        for (Map.Entry<String,Collection<String>> entry: ignoredQualifiersByFeatureType.entrySet()) {
            String featureType = entry.getKey();
            for (String qualifier: entry.getValue()) {
                featureTable.ignoreQualifier(qualifier, featureType);
            }
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
        
        PropertyConfigurator.configure("resources/classpath/log4j.loader.properties"); 
        
        propagateIgnoreFeaturesAndQualifiers(emblFile.getFeatureTable());

        TopLevelFeature topLevelFeature;
        try {
            topLevelFeature = getTopLevelFeature(emblFile.getAccession());
        } catch (TopLevelFeatureException e) {
            logger.error(e.getMessage());
            return;
        }

        doLoad(emblFile, topLevelFeature);

        /* Unused qualifiers can only be reported if there is a featuretable.
         * In some of our embl files, there are no features (E.g., Etenella
         * contigs) and this causes the loader to fail as it tries to look 
         * in the featuretable for unused qualifiers. Hence, the if clause
         * below was modified to check if the featretable is null (the
         * reportUnusedQualifiers is true by default)
         * nds 26 august 2010
         */
        if (reportUnusedQualifiers && emblFile.getFeatureTable()!=null) { 
            reportUnusedQualifiers(emblFile.getFeatureTable());
        } else {
            logger.debug("Not reporting on unused qualifiers");
        }
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

            organism = (Organism) session.merge(organism);
            if (!organism.isPopulated()) {
                logger.info(String.format("Marking organism '%s' as populated", organism));
                session.persist(organism.addProperty("genedb_misc", "populated"));
            }
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

                if (! (existingTopLevelFeature instanceof TopLevelFeature)) {
                    logger.warn(String.format("Existing feature is %s, not a top-level feature",
                        existingTopLevelFeature.getClass()));
                }
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
        session.flush();
        return null;
    }

    private String taxonomicDivision;
    private TopLevelFeature topLevelFeature;
    private Map<String,AbstractGene> genesByUniqueName = new HashMap<String,AbstractGene>();
    private Map<String,Transcript> transcriptsByUniqueName = new HashMap<String,Transcript>();
    private NavigableMap<Integer,Contig> contigsByStart = new TreeMap<Integer,Contig>();
    private Set<String> repeatRegionUniqueNames = new HashSet<String>();
    private Set<String> repeatUnitUniqueNames = new HashSet<String>();
    private Map<String,Integer> syntheticNcRNAIndexByType = new HashMap<String,Integer>();

    /**
     * We want to create a single Analysis object/row for each distinct analysis program
     * referenced in /similarity qualifiers in this file. These are stored in this map.
     */
    private Map<String,Analysis> similarityAnalysisByProgram = new HashMap<String,Analysis>();

    /**
     * Reset all our local state: necessary if the user retries after an error,
     * or if the same EmblLoader object is used more than once (to load more than one file).
     *
     * @param topLevelFeature
     */
    private void init(TopLevelFeature topLevelFeature) {
        if (topLevelFeature == null) {
            throw new IllegalArgumentException("topLevelFeature cannot be null");
        }
        this.topLevelFeature = topLevelFeature;
        this.genesByUniqueName.clear();
        this.transcriptsByUniqueName.clear();
        this.contigsByStart.clear();
        this.similarityAnalysisByProgram.clear();
        this.repeatRegionUniqueNames.clear();
        this.repeatUnitUniqueNames.clear();
        this.syntheticNcRNAIndexByType.clear();
        this.archivedFeatureIndexes.clear();

        this.motifIndex = 1;

        this.objectManager.flush();
    }

    private void loadContigsAndGaps(EmblLocation.Join locations) throws DataError {
        int pos = 0; // Position (interbase) on topLevelFeature
        for(EmblLocation location: locations.locations) {
            if (location instanceof EmblLocation.External) {
                EmblLocation.External externalLocation = (EmblLocation.External) location;
                int contigLength = externalLocation.simple.getLength();
                String contigUniqueName = externalLocation.accession;
                Contig contig = createContig(pos, contigLength, contigUniqueName);
                contigsByStart.put(pos, contig);

                pos += contigLength;
            } else if (location instanceof EmblLocation.Complement) {
                EmblLocation complementedLocation = ((EmblLocation.Complement) location).location;
                if (!(complementedLocation instanceof EmblLocation.External)) {
                    throw new DataError("The CO section should contain only external references and gaps");
                }
                EmblLocation.External externalComplementedLocation = (EmblLocation.External) complementedLocation;

                int contigLength = externalComplementedLocation.simple.getLength();
                String contigUniqueName = externalComplementedLocation.accession + "_reversed";
                Contig contig = createContig(pos, contigLength, contigUniqueName);
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

    /**
     * @param pos
     * @param contigLength
     * @param contigUniqueName
     * @return
     */
    private Contig createContig(int pos, int contigLength, String contigUniqueName) {
        logger.debug(String.format("Creating contig '%s' at %d-%d", contigUniqueName, pos, pos + contigLength));
        Contig contig = TopLevelFeature.make(Contig.class, contigUniqueName, organism);
        contig.setResidues(topLevelFeature.getResidues(pos, pos + contigLength));
        session.persist(contig);
        topLevelFeature.addLocatedChild(contig, pos, pos + contigLength, (short) 0, 0);
        return contig;
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

        Feature focalFeature = null;
        if (featureType.equals("repeat_region")) {
            focalFeature = loadRepeatRegion(feature);
        }
        else if (featureType.equals("repeat_unit")) {
            focalFeature = loadRepeatUnit(feature);
        }
        else if (featureType.equals("CDS")) {
            focalFeature = loadCDS((FeatureTable.CDSFeature) feature);
        }
        else if (featureType.equals("tRNA")) {
            focalFeature = loadNcRNA(TRNA.class, featureType, feature);
        }
        else if (featureType.equals("rRNA")) {
            focalFeature = loadNcRNA(RRNA.class, featureType, feature);
        }
        else if (featureType.equals("snRNA")) {
            focalFeature = loadNcRNA(SnRNA.class, featureType, feature);
        }
        else if (featureType.equals("snoRNA")) {
            focalFeature = loadNcRNA(SnoRNA.class, featureType, feature);
        }
        else if (featureType.equals("misc_RNA") || featureType.equals("ncRNA")) {
            focalFeature = loadNcRNA(NcRNA.class, featureType, feature);
        }
        else if (featureType.equals("3'UTR") || featureType.equals("5'UTR")) {
            utrs.add(feature);
        }
        else if (featureType.equals("gap")) {
            focalFeature = loadGap(feature);
        }
        else if (featureType.equals("CDS_motif")) {
            focalFeature = loadMotif(feature);
        }
        else if (featureType.equals("LTR")) {
            throw new DataError("Tell Robin he needs to write code for loading LTR features!");
            // TODO
        }
        else if (featureType.equals("fasta_record")) {
            loadFastaRecord(feature); // These are often used to identify individual contigs within a bin chromosome
        }
        else if (featureType.equals("misc_feature") && feature.getQualifierValues("note").contains(new String("centromere"))){
            /* Centromeres are pulled out as misc_features with note="centromere" with writedb_entry.
             * Here we check for this note and load the centromere. Rest of the misc features are archived.
             * nds, 30 Nov 2010
             */
            focalFeature = loadCentromere(feature); 
            
        }
        else if (featureType.equals("misc_feature") && feature.getQualifierValues("note").contains(new String("SECIS_element"))){
            /* SECIS_elements are pulled out with a note='SECIS_element' */
            focalFeature = loadSECISElement(feature);
        }
        else {
            logger.info(String.format("Archiving %s feature", featureType));
            archiveFeature(feature);
        }

        if (focalFeature != null) {
            archiveUnusedQualifiers(feature, focalFeature);
        }
    }

    private void loadFastaRecord(FeatureTable.Feature feature) {

        String featureUniqueName = feature.getQualifierValues("label").get(0);
        logger.warn(String.format("Creating archived contig feature '%s' from '%s' feature on line %d",
            featureUniqueName, feature.type, feature.lineNumber));

        Feature focalFeature = new Region(
            organism, featureUniqueName,
            /*analysis:*/false, /*obsolete:*/true,
            new Timestamp(System.currentTimeMillis()));
        locate(focalFeature, feature.location);

        int rank=0;
        focalFeature.addFeatureProp(
                String.format("Archived from %s feature %s with location %s; file '%s', line %d",
                feature.type, featureUniqueName, feature.location, feature.getFilePath(), feature.lineNumber),
                "feature_property", "comment", rank++);
        for (String note: feature.getQualifierValues("note")) {
            focalFeature.addFeatureProp(note, "feature_property", "comment", rank++);
        }
        for (String colour: feature.getQualifierValues("colour")) {
            focalFeature.addFeatureProp(colour, "genedb_misc", "colour", rank++);
        }
        session.persist(focalFeature);
        archiveUnusedQualifiers(feature, focalFeature);

    }
    // Centromeres
    private Centromere loadCentromere(FeatureTable.Feature feature) throws DataError {
        EmblLocation centromereLocation = feature.location;
        String centromereName = feature.getUniqueName();

        logger.info(String.format("Adding a centromere %s at %d-%d on %s", 
                   centromereName, centromereLocation.getFmin(), centromereLocation.getFmax(), topLevelFeature.getUniqueName() ));
        Centromere centromere = Centromere.make(topLevelFeature, centromereName, centromereLocation.getFmin(), centromereLocation.getFmax()); 
        session.persist(centromere);    
   
        // Add any literature (duplicated effort here since the processLiterature() method is within the geneLoader). Fix later.
        Pattern literaturePattern = Pattern.compile("(?:PMID:)?\\s*(\\d+)(?:;.*)?");
        for (String pmid: feature.getQualifierValues("literature", "citation")) {
            Matcher matcher = literaturePattern.matcher(pmid);
            if (!matcher.matches()) {
                throw new DataError("Failed to parse literature/citation qualifier: " + pmid);
            }
            String accession = matcher.group(1);      
            DbXRef dbXRef = objectManager.getDbXRef("PMID", accession);
            Pub pub = objectManager.getPub(String.format("PMID:%s", accession), "unfetched");
            session.persist(pub.addDbXRef(dbXRef, true));
            session.persist(centromere.addPub(pub));
        }
        
        return centromere;

    }

    //SECIS_elements
    private SECISElement loadSECISElement(FeatureTable.Feature feature) throws DataError {
        EmblLocation secisLocation = feature.location;
        String secisName = feature.getUniqueName();

        logger.info(String.format("Adding a SECIS_element %s at %d-%d on %s", 
                   secisName, secisLocation.getFmin(), secisLocation.getFmax(), topLevelFeature.getUniqueName() ));
      
        SECISElement secisElement = new SECISElement(organism, secisName);
        locate(secisElement,secisLocation);
        session.persist(secisElement);   
        
        int rank=0;
        for (String note: feature.getQualifierValues("note")) {
            if(!note.equalsIgnoreCase("SECIS_element") && !note.equalsIgnoreCase("false")){ //The note=false just means it is not obsolete
                secisElement.addFeatureProp(note, "feature_property", "comment", rank++);
            }
        }
 
        return secisElement;

    }
    
    
    private Counters archivedFeatureIndexes = new Counters();
    private void archiveFeature(FeatureTable.Feature feature) {
        String featureUniqueName = String.format("%s:archived:%s:%d",
            topLevelFeature.getUniqueName(), feature.type,
            archivedFeatureIndexes.nextval(feature.type));
        logger.warn(String.format("Archiving '%s' feature on line %d as '%s'",
            feature.type, feature.lineNumber, featureUniqueName));

        Feature focalFeature = new Region(
            organism, featureUniqueName,
            /*analysis:*/false, /*obsolete:*/true,
            new Timestamp(System.currentTimeMillis()));

        locate(focalFeature, feature.location);

        focalFeature.addFeatureProp(
            String.format("Archived from %s feature with location %s; file '%s', line %d",
                feature.type, feature.location, feature.getFilePath(), feature.lineNumber),
            "feature_property", "comment", 0);

        session.persist(focalFeature);
        archiveUnusedQualifiers(feature, focalFeature);
    }

    private void archiveUnusedQualifiers(FeatureTable.Feature feature, Feature focalFeature) {
        int rank = 0;
        for (String unusedQualifier: feature.getUnusedQualifiers()) {
            logger.trace(String.format("Archiving qualifier on '%s': %s",
                focalFeature.getUniqueName(), unusedQualifier));
            focalFeature.addFeatureProp(unusedQualifier, "genedb_misc", "EMBL_qualifier", rank++);
        }
    }

    private void reportUnusedQualifiers(FeatureTable featureTable) {
        Map<String,Set<String>> unusedQualifiersByFeatureType = new HashMap<String, Set<String>>();

        // Collect unused qualifiers
        for (FeatureTable.Feature feature: featureTable.getFeatures()) {
            for (String unusedQualifier: feature.getUnusedQualifierNames()) {
                if (!unusedQualifiersByFeatureType.containsKey(feature.type)) {
                    unusedQualifiersByFeatureType.put(feature.type, new HashSet<String>());
                }
                unusedQualifiersByFeatureType.get(feature.type).add(unusedQualifier);
            }
        }

        // Report unused qualifiers
        if (unusedQualifiersByFeatureType.isEmpty()) {
            logger.info("No unused qualifiers to report");
            return;
        }
        for (Map.Entry<String,Set<String>> entry: unusedQualifiersByFeatureType.entrySet()) {
            StringBuilder message = new StringBuilder(String.format("Unused qualifiers for %s features:%n", entry.getKey()));
            for (String qualifierName: entry.getValue()) {
                message.append(String.format("\t/%s%n", qualifierName));
            }
            logger.warn(message);
        }
    }

    private Gap loadGap(FeatureTable.Feature gapFeature) {
        EmblLocation gapLocation = gapFeature.location;

        logger.debug(String.format("Creating gap at %d-%d", gapLocation.getFmin(), gapLocation.getFmax()));
        Gap gap = topLevelFeature.addGap(gapLocation.getFmin(), gapLocation.getFmax());
        session.persist(gap);

        int rank=0;
        for (String note: gapFeature.getQualifierValues("note")) {
            gap.addFeatureProp(note, "feature_property", "comment", rank++);
        }

        return gap;
    }

    private int motifIndex = 1;
    private PolypeptideMotif loadMotif(FeatureTable.Feature motifFeature) {
        String motifUniqueName = String.format("%s:motif:%d", topLevelFeature.getUniqueName(), motifIndex++);
        PolypeptideMotif motif = new PolypeptideMotif(organism, motifUniqueName);
        session.persist(motif);
        locate(motif, motifFeature.location);

        int rank = 0;
        for (String note: motifFeature.getQualifierValues("note")) {
            motif.addFeatureProp(note, "feature_property", "comment", rank++);
        }

        return motif;
    }

    private Feature loadRepeatRegion(FeatureTable.Feature repeatRegionFeature) throws DataError {
        String repeatRegionName = repeatRegionFeature.getQualifierValue("FEAT_NAME");
        EmblLocation repeatRegionLocation = repeatRegionFeature.location;
        int fmin = repeatRegionLocation.getFmin();
        int fmax = repeatRegionLocation.getFmax();

        String repeatType = repeatRegionFeature.getQualifierValue("rpt_type");
        final Class<? extends RepeatRegion> repeatRegionClass;
        if (repeatType == null) {
            repeatRegionClass = RepeatRegion.class;
        } else {
            repeatType = repeatType.toLowerCase();
            if (repeatType.equals("direct")) {
                repeatRegionClass = DirectRepeatRegion.class;
            } else if (repeatType.equals("inverted")) {
                repeatRegionClass = InvertedRepeatRegion.class;
            } else {
                throw new DataError(String.format("Unknown repeat type '%s'", repeatType));
            }
        }

        String repeatRegionUniqueName = String.format("%s:repeat:%d-%d", topLevelFeature.getUniqueName(), fmin, fmax);
        if (repeatRegionUniqueNames.contains(repeatRegionUniqueName)) {
            logger.warn(String.format("The repeat region '%s' already exists." +
                    "Ignoring second (or subsequent) occurence at line %d",
                repeatRegionUniqueName, repeatRegionFeature.lineNumber));
            return null;
        }
        repeatRegionUniqueNames.add(repeatRegionUniqueName);

        logger.debug(String.format("Creating repeat region '%s' (%s) of type '%s' at %d-%d",
            repeatRegionUniqueName, repeatRegionName, repeatRegionClass.getSimpleName(), fmin, fmax));
        RepeatRegion repeatRegion = RepeatRegion.make(repeatRegionClass,
            organism, repeatRegionUniqueName, repeatRegionName);

        int rank = 0;
        String label = repeatRegionFeature.getQualifierValue("label");
        if (label != null) {
            repeatRegion.addFeatureProp(String.format("/label=%s", label), "feature_property", "comment", rank++);
        }
        for(String note : repeatRegionFeature.getQualifierValues("note")) {
            repeatRegion.addFeatureProp(note, "feature_property", "comment", rank++);
        }

        // Add a comment for the /rpt_family, if present
        String rptFamily = repeatRegionFeature.getQualifierValue("rpt_family");
        if (rptFamily != null) {
            repeatRegion.addFeatureProp(String.format("/rpt_family=%s", rptFamily),
                "feature_property", "comment", rank++);
        }

        session.persist(repeatRegion);
        locate(repeatRegion, fmin, fmax, (short)0, null);

        return repeatRegion;
    }

    // TODO loadRepeatUnit is very similar to loadRepeatRegion: unify?

    private Feature loadRepeatUnit(FeatureTable.Feature repeatUnitFeature) throws DataError {
        EmblLocation repeatUnitLocation = repeatUnitFeature.location;
        int fmin = repeatUnitLocation.getFmin();
        int fmax = repeatUnitLocation.getFmax();
        String repeatUnitUniqueName = String.format("%s:repeat_unit:%d-%d", topLevelFeature.getUniqueName(), fmin, fmax);

        if (repeatUnitUniqueNames.contains(repeatUnitUniqueName)) {
            logger.warn(String.format("The repeat region '%s' already exists." +
                    "Ignoring second (or subsequent) occurence at line %d",
                repeatUnitUniqueName, repeatUnitFeature.lineNumber));
            return null;
        }
        repeatUnitUniqueNames.add(repeatUnitUniqueName);

        logger.debug(String.format("Creating repeat unit '%s' at %d-%d",
            repeatUnitUniqueName, fmin, fmax));
        RepeatUnit repeatUnit = RepeatUnit.make(RepeatUnit.class,
            organism, repeatUnitUniqueName, null);

        String colour = repeatUnitFeature.getQualifierValue("colour");
        if (colour != null) {
            repeatUnit.addFeatureProp(colour, "genedb_misc", "colour", 0);
        }
        int rank = 0;
        String label = repeatUnitFeature.getQualifierValue("label");
        if (label != null) {
            repeatUnit.addFeatureProp(String.format("/label=%s", label), "feature_property", "comment", rank++);
        }
        for (String note: repeatUnitFeature.getQualifierValues("note")) {
            repeatUnit.addFeatureProp(note, "feature_property", "comment", rank++);
        }

        session.persist(repeatUnit);
        locate(repeatUnit, fmin, fmax, (short)0, null);

        return repeatUnit;
    }

    // Can't define static fields in inner classes, grr.
    private static final Set<String> goQualifiers = new HashSet<String>();
    static {
        Collections.addAll(goQualifiers, "aspect", "GOid", "term", "qualifier",
            "evidence", "db_xref", "with", "from", "date", "autocomment", "attribution");
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
        protected boolean isObsolete = false;
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
        public Feature load() throws DataError {
            if (geneUniqueName == null) {
                throw new RuntimeException("Cannot load a gene with no uniqueName");
            }
            if (transcriptUniqueName == null) {
                throw new RuntimeException("Cannot load a transcript with no uniqueName");
            }
            loadTranscript(loadOrFetchGene());

            return focalFeature;
        }

        private AbstractGene loadOrFetchGene() {

            if (topLevelFeature.getClass().equals(Gene.class)) {
                /* If the gene is acting as the topLevelFeature
                 * don't create a new gene feature here
                 * Instead return the existing topLevel gene feature
                 */
                logger.debug(String.format("The toplevel feature is a gene"));
                return (AbstractGene) topLevelFeature;
            }
            else if (singlySpliced) {
                AbstractGene gene = createSinglySplicedGene();
                genesByUniqueName.put(geneUniqueName, gene);
                return gene;
            }
            else {
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
            gene.setObsolete(isObsolete); //Is it obsolete?
            logger.info(String.format("Setting gene %s 's obsolete status to %s", gene.getUniqueName(), isObsolete));
            locate(gene, location);
            session.persist(gene);
            return gene;
        }

        private void loadTranscript(AbstractGene gene) throws DataError {
            
            logger.debug(String.format("Creating transcript '%s' for gene '%s'", transcriptUniqueName, gene.getUniqueName()));

            /**
             * A unique number should be appended to the uniquename of the singly
             * spliced transcript features in the database. We must do this because
             * Artemis requires each feature to have a globally unique name, and does
             * not work correctly if a transcript has the same uniquename as its gene.
             * Similarly the GFF3 feature format requires feature names to be
             * globally unique, and we want to be able to export our data in GFF3 format.
             *
             * For alternatively-spliced genes, on the other hand, there is no need to
             * append the transcript type, because the transcript will have an assigned
             * uniquename (the /systematic_id of the CDS) that is different from the
             * uniquename of the gene (the /shared_id of the CDS).
             */

            String actualTranscriptUniqueName;
            if (transcriptUniqueName.equals(gene.getUniqueName())) { // will occur for singly-spliced genes
                actualTranscriptUniqueName = String.format("%s.1", transcriptUniqueName); //make transcript uniquename differ from gene uniquename
            } else {
                actualTranscriptUniqueName = transcriptUniqueName;
            }

            this.transcript = gene.makeTranscript(getTranscriptClass(), actualTranscriptUniqueName, location.getFmin(), location.getFmax(), gene, location);
            transcript.setObsolete(isObsolete); //Is it obsolete?
            session.persist(transcript);

            focalFeature = transcript;
            if (transcript instanceof ProductiveTranscript) {
                Polypeptide polypeptide = ((ProductiveTranscript) transcript).getProtein();
                if (polypeptide != null) {
                    polypeptide.setObsolete(isObsolete);
                    focalFeature = polypeptide;
                }
            }

            transcriptsByUniqueName.put(actualTranscriptUniqueName /*transcriptUniqueName */, transcript);
            loadExons(actualTranscriptUniqueName);
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
                session.persist(transcript.addSynonym(synonym, isCurrent, /*isInternal:*/ false));
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
            return processPropertyQualifier(qualifierName, propertyCvName, propertyTermName, qualifierParsers.get(qualifierName), isUnique);
        }

        protected int processPropertyQualifier(String qualifierName, String propertyCvName, String propertyTermName) throws DataError {
            return processPropertyQualifier(qualifierName, propertyCvName, propertyTermName, qualifierParsers.get(qualifierName), false);
        }

        private int processPropertyQualifier(String qualifierName, String propertyCvName, String propertyTermName,
                TermParser parser, boolean isUnique) throws DataError {
            Set<String> values = new HashSet<String>();
            int rank = 0;
            for(String qualifierValue: feature.getQualifierValues(qualifierName)) {
                if (parser != null) {
                    for (String normalisedValue: parser.parse(qualifierValue)) {
                        rank = processNormalisedProperty(qualifierName, propertyCvName, propertyTermName,
                            isUnique, values, rank, normalisedValue);
                    }
                } else {
                    rank = processNormalisedProperty(qualifierName, propertyCvName, propertyTermName,
                        isUnique, values, rank, qualifierValue);
                }
            }
            return rank;
        }

        private int processNormalisedProperty(String qualifierName, String propertyCvName,
                String propertyTermName, boolean isUnique, Set<String> values, int rank,
                String normalisedValue) throws DataError {
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
            return rank;
        }

        protected void processCvTermQualifier(String qualifierName, String cvName, String dbName, boolean createTerms)
                throws DataError {
            processCvTermQualifier(qualifierName, cvName, dbName, createTerms, qualifierParsers.get(qualifierName));
        }

        protected void processCvTermQualifier(String qualifierName, String cvName, String dbName,
                boolean createTerms, TermParser termParser)
                throws DataError {

            Set<String> terms = new HashSet<String>();
            for (String term: feature.getQualifierValues(qualifierName)) {
                if (termParser != null) {
                    for (String partNormalisedTerm: termParser.parse(term)) {
                    	String normalisedTerm = partNormalisedTerm.trim();
                        processNormalisedCvTermQualifier(qualifierName, cvName, dbName, createTerms, terms, term,
                            normalisedTerm);
                    }
                } else {
                    processNormalisedCvTermQualifier(qualifierName, cvName, dbName, createTerms, terms, term, term);
                }
            }
        }

        private void processNormalisedCvTermQualifier(String qualifierName, String cvName,
                String dbName, boolean createTerms, Set<String> terms, String term, String normalisedTerm)
                throws DataError {
            String lcNormalisedTerm = normalisedTerm.toLowerCase();
            if (terms.contains(lcNormalisedTerm)) {
                logger.warn(
                    String.format("The qualifier /%s=\"%s\" appears more than once. Ignoring subsequent copies.",
                        qualifierName, term));
                return;
            } else {
                terms.add(lcNormalisedTerm);
            }

            FeatureCvTerm featureCvTerm = focalFeature.addCvTerm(cvName, normalisedTerm, dbName, createTerms);
            if (featureCvTerm == null) {
                throw new DataError(
                    String.format("Failed to find term '%s' in CV '%s'", normalisedTerm, cvName));
            }
            session.persist(featureCvTerm);
        }

        private void loadExons() throws DataError { //if you don't specify the transcriptUniqueName use the existing one taken from EMBL file
            loadExons(transcriptUniqueName);
        }

        /*
         * specifying a transcriptUniquename whe you load the exons allows the modified transcriptUniqueName (with appended .1) to be used
         * in making the exonUniqueName for singly spliced genes
         */
        private void loadExons(String actualTranscriptUniqueName) throws DataError {
            int exonIndex = 0;
            for (EmblLocation exonLocation: location.getParts()) {
                if (exonLocation instanceof EmblLocation.External) {
                    throw new DataError("Found an external exon (trans-splicing). We can't handle that yet.");
                }
                String exonUniqueName = String.format("%s:exon:%d", actualTranscriptUniqueName, ++exonIndex);
                logger.debug(String.format("Creating exon '%s' at %d-%d", exonUniqueName, exonLocation.getFmin(), exonLocation.getFmax()));
                AbstractExon exon = transcript.createExon(exonUniqueName, exonLocation.getFmin(), exonLocation.getFmax(), phase);
                exon.setObsolete(isObsolete);
                session.persist(exon);
            }
        }

        protected void processGO() throws DataError {

            String comment = "From EMBL file"; //default value for autocomment

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

                    /* nds (24.6.2010):It is rare but sometimes the key here
                     * has acquired an unnecessary space either by data errors
                     * in Chado or in the EMBL file.
                     * Example: au tocomment="From EMBL file"
                     * The replace below was put in place to deal with this problem.
                     */
                    key = key.replaceAll("\\s","").trim();


                    if (!goQualifiers.contains(key)) {
                        throw new DataError(String.format("Failed to parse /GO=\"%s\"; don't know what to do with %s=%s", go, key, value));
                    }
                    // "aspect", "GOid", "term", "qualifier", "evidence", "db_xref", "with", "date", "attribution", "residue", "autocomment"

                    /* nds (25.6.2010): Sometimes the values have the same
                     * problem as above but a replace cannot be applied for
                     * all the values. I've commented out the replace below
                     * as it's possible this was a problem specific for
                     * Plasmodium reichenowi.
                     */
//                    if(!key.equals("autocomment")){
//                        value = value.replaceAll("\\s","").trim();
//                    }

                    if (key.equals("GOid")) {
                        goInstance.setId(value);
                    } else if (key.equals("date")) {
                        goInstance.setDate(value);
                    } else if (key.equals("evidence")) {
                        GoEvidenceCode evidenceCode = GoEvidenceCode.parse(value);
                        if (evidenceCode == null) {
                            throw new DataError(String.format("Failed to parse GO evidence code '%s'", value));
                        }
                        goInstance.setEvidence(evidenceCode);
                    } else if (key.equals("qualifier")) {
                        goInstance.addQualifier(value);
                    } else if (key.equals("with") || key.equals("from")) {
                        goInstance.setWithFrom(value);
                    } else if (key.equals("aspect")) {
                        goInstance.setSubtype(value);
                    } else if (key.equals("attribution")) {
                        goInstance.setAttribution(value);
                    } else if (key.equals("residue")) {
                        goInstance.setResidue(value);
                    } else if (key.equals("db_xref")) {
                        goInstance.setRef(value);
                        /* TODO: Temp fix to avoid duplicate pubdbxref entries,
                         * fix properly later using the object manager: nds*/
                        Pattern DBXREF_PATTERN = Pattern.compile("\\S+:(\\S+)");
                        Matcher matcher = DBXREF_PATTERN.matcher(value);
                        if(matcher.matches()){
                            seenPubAccessions.add(matcher.group(1));
                        }
                    } else if (key.equals("autocomment")){
                        comment = value;

                    }
                }
                if (goTermErrorsAreNotFatal) {
                    try {
                        featureUtils.createGoEntries(focalFeature, goInstance, comment /*"From EMBL file"*/, (DbXRef) null);
                    } catch (DataError e) {
                        logger.error("Error loading GO term: " + e.getMessage());
                    }
                } else {
                    featureUtils.createGoEntries(focalFeature, goInstance, comment /*"From EMBL file" */, (DbXRef) null);
                }
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

       And here is an example from Eimeria tenella where the algorithm, program and version are
       all specified in the first field.

            FT                   /similarity="ComparativeBlastX_uni blastall v2.2.6;
            FT                   SWALL:A6WB28.1;  ;  ;  ; ; ; ; E()=19.0063; ; 58 aa overlap;
            FT                   query 24-81 aa; subject 875-1042 aa"


         */
        private final Pattern similarityPattern = Pattern.compile(
            "(\\w+|\\w+ +\\w+ +v[\\d.]+);" +                                // 1.     Algorithm, e.g. fasta, blastp
            "\\s*(\\w+):([\\w.]+)" +                                            // 2,3.   Primary dbxref, e.g. SWALL:Q26723
            "(?:\\s+\\((\\w+):([\\w.]+(?:,\\s*(?:\\w+:)?[\\w.]+)*)\\))?;" +     // 4,5.   Optional secondary dbxrefs, e.g. "EMBL:M20871", "EMBL:BC002634, AAH02634"
            "\\s*([^;]+)?;" +                                                   // 6.     Organism name
            "\\s*([^;]+)?;" +                                                   // 7.     Product name
            "\\s*([^;]+)?;" +                                                   // 8.     Gene name
            "\\s*(?:length\\s+(\\d+)\\s+aa)?;" +                                // 9.     Optional match length
            "\\s*(?:id=(\\d{1,3}(?:\\.\\d{1,3})?)%)?;" +                        // 10.    Optional degree of identity (percentage)
            "\\s*(?:ungapped\\s+id=(\\d{1,3}(?:\\.\\d{1,3})?)%)?;" +            // 11.    Optional ungapped identity (percentage)
            "\\s*E\\(\\)=(\\d*(?:\\.\\d+)?(?:e[+-]? ?\\d+)?);" +                // 12.    E-value
            "\\s*(?:score=(\\d+))?;" +                                          // 13.    Optional score
            "\\s*(?:(\\d+)\\s+aa\\s+overlap)?;" +                               // 14.    Optional overlap length (integer)
            "\\s*(?:query\\s+(\\d+)-\\s*(\\d+) aa)?;" +                         // 15,16. Optional query location
            "\\s*(?:subject\\s+(\\d+)-\\s*?(\\d+) aa)?");                       // 17,18. Optional subject location

        protected void processSimilarityQualifiers() throws DataError {
            for (String similarityString: feature.getQualifierValues("similarity")) {
                processSimilarityQualifier(similarityString);
            }
        }

        private Map<String,Integer> numberOfSimilaritiesByPrimaryDbXRef = new HashMap<String,Integer>();
        private void processSimilarityQualifier(String similarityString) throws DataError {
            Matcher matcher = similarityPattern.matcher(similarityString);
            if (!matcher.matches()) {
                throw new DataError(String.format("Failed to parse /similarity=\"%s\"", similarityString));
            }

            Similarity similarity = new Similarity();
            String program = matcher.group(1);
            if (!similarityAnalysisByProgram.containsKey(program)) {
                logger.trace(String.format("Creating Analysis object for program '%s'", program));
                Analysis analysis = new Analysis();
                if (program.indexOf(' ') > 0) {
                    // Program string contains spaces, so it's of the form "algorithm program version"
                    String[] splitProgram = program.split(" +");
                    if (splitProgram.length != 3) {
                        throw new DataError("Unexpected problem parsing similarity program: " + program);
                    }
                    analysis.setAlgorithm(splitProgram[0]);
                    analysis.setProgram  (splitProgram[1]);
                    analysis.setProgramVersion(splitProgram[2]);
                } else {
                    analysis.setProgram(program);
                    analysis.setProgramVersion("unknown");
                }
                similarityAnalysisByProgram.put(program, analysis);
            }
            Analysis analysis = similarityAnalysisByProgram.get(program);
            session.saveOrUpdate(analysis);

            similarity.setAnalysis(analysis);
            DbXRef primaryDbXRef = objectManager.getDbXRef(matcher.group(2), matcher.group(3));
            if (primaryDbXRef == null) {
                throw new DataError(String.format("Could not find database '%s' for primary dbxref of /similarity", matcher.group(2)));
            }
            similarity.setPrimaryDbXRef(primaryDbXRef);

            {
                // Set the unique identifier to something unique

                String primaryDbXRefString = primaryDbXRef.toString();
                if (!numberOfSimilaritiesByPrimaryDbXRef.containsKey(primaryDbXRefString)) {
                    numberOfSimilaritiesByPrimaryDbXRef.put(primaryDbXRefString, 1);
                } else {
                    numberOfSimilaritiesByPrimaryDbXRef.put(primaryDbXRefString, 1 + numberOfSimilaritiesByPrimaryDbXRef.get(primaryDbXRefString));
                }
                int numberOfSimilarities = numberOfSimilaritiesByPrimaryDbXRef.get(primaryDbXRefString);
                similarity.setUniqueIdentifier(String.format("%s_%s_%d", transcriptUniqueName, primaryDbXRefString, numberOfSimilarities));
            }

            if (matcher.group(4) != null) {
                String dbName = matcher.group(4);
                for (String accession: matcher.group(5).split(",\\s*")) {
                    int colonIndex = accession.indexOf(':');
                    if (colonIndex >= 0) {
                        dbName = accession.substring(0, colonIndex);
                        accession = accession.substring(colonIndex + 1);
                    }
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

            if (matcher.group(10) != null) {
                try {
                    similarity.setId(Double.parseDouble(matcher.group(10)));
                } catch (NumberFormatException e) {
                    throw new DataError("Failed to parse id field of /similarity: " + matcher.group(10));
                }
            }

            if (matcher.group(11) != null) {
                try {
                    similarity.setUngappedId(Double.parseDouble(matcher.group(11)));
                } catch (NumberFormatException e) {
                    throw new DataError("Failed to parse ungapped id field of /similarity: " + matcher.group(11));
                }
            }

            String eValueString = matcher.group(12);
            if (eValueString.startsWith("e") || eValueString.startsWith("E")) {
                eValueString = "1" + eValueString;
            }
            try {
                similarity.setEValue(Double.parseDouble(eValueString.replaceAll("\\s+", "")));
            } catch (NumberFormatException e) {
                throw new DataError("Failed to parse E() field of /similarity: " + eValueString);
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
            Set<String> seenQualifiedTerms = new HashSet<String>();
            for (String controlledCuration: feature.getQualifierValues("controlled_curation")) {
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
                String cv   = valuesByKey.containsKey("cv") ? valuesByKey.get("cv") : "CC_genedb_controlledcuration";

                String qualifiedTerm = String.format("%s:%s", cv, term);
                if (seenQualifiedTerms.contains(qualifiedTerm)) {
                    logger.warn(String.format(
                        "There is more than one /controlled_curation qualifier with term '%s' in %s feature on line %d." +
                        "Ignoring subsequent occurences.",
                        qualifiedTerm, feature.type, feature.lineNumber));
                    continue;
                }
                seenQualifiedTerms.add(qualifiedTerm);

                logger.trace(String.format("/controlled_curation: adding term '%s:%s' to %s",
                    cv, term, focalFeature));
                FeatureCvTerm featureCvTerm = focalFeature.addCvTerm(cv, term);

                featureCvTerm.addPropIfNotNull("feature_property", "date",   valuesByKey.get("date"));
                featureCvTerm.addPropIfNotNull("genedb_misc", "attribution", valuesByKey.get("attribution"));
                featureCvTerm.addPropIfNotNull("genedb_misc", "evidence",    valuesByKey.get("evidence"));
                featureCvTerm.addPropIfNotNull("genedb_misc", "qualifier",   valuesByKey.get("qualifier"));

                if (valuesByKey.containsKey("db_xref")) {
                    addDbXRefs(featureCvTerm, valuesByKey.get("db_xref"));
                }

                session.persist(featureCvTerm);
            }
        }

        private Pattern dbxrefPattern = Pattern.compile("([^:]+):(.*)");
        /**
         * Add DbXRefs to the focal feature.
         * @param dbxrefs a pipe-separated list of <code>db:accession</code>
         * @throws DataError if the string cannot be parsed or the database does not exist
         */
        protected void addDbXRefs(String dbxrefs) throws DataError {
            for (String dbxref: dbxrefs.split("\\|")) {
                addDbXRef(focalFeature, dbxref);
            }
        }

        /**
         * Add a DbXRef to the specified object.
         * @param target the object to which the reference should be added
         * @param dbxref a string of the form <code>db:accession</code>
         * @throws DataError if the string cannot be parsed or the database does not exist
         */
        private void addDbXRef(HasPubsAndDbXRefs target, String dbxref) throws DataError {
            Matcher matcher = dbxrefPattern.matcher(dbxref);
            if (!matcher.matches()) {
                throw new DataError(String.format("db_xref '%s' is not of the form database:accession", dbxref));
            }
            String dbName = matcher.group(1);
            String accession = matcher.group(2);
            addDbXRef(target, dbName, accession);
        }

        private void addDbXRef(HasPubsAndDbXRefs target, String dbName, String accession) throws DataError {
            DbXRef dbXRef = objectManager.getDbXRef(dbName, accession);
            if (dbXRef == null) {
                throw new DataError(String.format("Database '%1$s' does not exist (for dbxref '%1$s:%2$s')",
                    dbName, accession));
            }
            if (dbName.equals("PMID")) {
                // PMID is a special case; these are stored as FeaturePubs
                addPub(target, accession, dbXRef);
            }
            else {
                session.persist(target.addDbXRef(dbXRef));
            }
        }

        private void addPub(HasPubsAndDbXRefs target, String accession, DbXRef dbXRef) {
            logger.trace(String.format("Adding publication id '%s' to %s",
                accession, target.toString()));
            Pub pub = objectManager.getPub(String.format("PMID:%s", accession), "unfetched");
            session.persist(pub.addDbXRef(dbXRef, true));
            session.persist(target.addPub(pub));
        }

        private Set<String> seenPubAccessions = new HashSet<String>();
        private void addPub(String accession) {
            if (seenPubAccessions.contains(accession)) {
                logger.info(String.format(
                    "Ignoring duplicate publication with accession '%s' on %s feature at line %d",
                    accession, feature.type, feature.lineNumber));
                return;
            }
            DbXRef dbXRef = objectManager.getDbXRef("PMID", accession);
            addPub(focalFeature, accession, dbXRef);
            seenPubAccessions.add(accession);

        }

        private Pattern literaturePattern = Pattern.compile("(?:PMID:)?\\s*(\\d+)(?:;.*)?");
        protected void processLiterature() throws DataError {
            for (String pmid: feature.getQualifierValues("literature", "citation")) {
                Matcher matcher = literaturePattern.matcher(pmid);
                if (!matcher.matches()) {
                    throw new DataError("Failed to parse literature/citation qualifier: " + pmid);
                }
                addPub(matcher.group(1));
            }
        }

        /**
         * Add DbXRefs to the specified FeatureCvTerm.
         * @param dbxrefs a pipe-separated list of <code>db:accession</code>
         * @throws DataError
         */
        protected void addDbXRefs(FeatureCvTerm featureCvTerm, String dbxrefs) throws DataError {
            for (String dbxref: dbxrefs.split("\\|")) {
                addDbXRef(featureCvTerm, dbxref);
            }
        }

        /**
         * Use the qualifiers of the CDS feature to add various bits of annotation
         * to the transcript (or to the polypeptide, if there is one). Specifically,
         * add synonyms, properties and products.
         */
        protected void processTranscriptQualifiers() throws DataError {

            checkForPreviousSystematicIdEqualToSystematicId();

            addTranscriptSynonymsFromQualifier("synonym", "synonym", true);
            addTranscriptSynonymsFromQualifier("previous_systematic_id", "previous_systematic_id", true);

            int commentRank = processPropertyQualifier("note", "feature_property", "comment");
            for (String name: qualifierProperties) {
                processPropertyQualifier(name, "genedb_misc", name, uniqueQualifiers.contains(name));
            }

            for (String translation: feature.getQualifierValues("translation")) {
                translation = translation.replaceAll("\\s+", "");
                translation = translation.toUpperCase();
                logger.debug(String.format("Setting translation to sequence from EMBL file : %s", translation));
                focalFeature.setResidues(translation);
                focalFeature.addFeatureProp("Translation loaded from EMBL", "feature_property", "comment", commentRank++);
            }

            addColourToExons();

            processCvTermQualifier("class", "RILEY", "RILEY", false, normaliseRileyNumber);
            processCvTermQualifier("product", "genedb_products", "PRODUCT", true);

            String label = feature.getQualifierValue("label");
            if (label != null) {
                focalFeature.addFeatureProp(String.format("/label=%s", label), "feature_property", "comment", commentRank++);
            }

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

            for (String dbxrefs: feature.getQualifierValues("db_xref")) {
                addDbXRefs(dbxrefs);
            }

            processGO();
            processSimilarityQualifiers();
            processCuration();
            processLiterature();
        }

        protected void addColourToExons() throws DataError {
            String colour = feature.getQualifierValue("colour");
            if (colour == null) {
                return;
            }

            String normalisedColour = colour;
            TermParser colourParser = qualifierParsers.get("colour");
            if (colourParser != null) {
                if (!(colourParser instanceof TermNormaliser)) {
                    throw new RuntimeException("The /colour parser is not a TermNormaliser?!");
                }
                TermNormaliser colourNormaliser = (TermNormaliser) colourParser;
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
        private void checkForPreviousSystematicIdEqualToSystematicId() throws DataError {
            String systematicId = feature.getQualifierValue("systematic_id");
            if (systematicId == null) {
                return;
            }
            for (String temporarySystematicId: feature.getQualifierValues("previous_systematic_id")) {
                if (systematicId.equals(temporarySystematicId)) {
                    throw new DataError("Feature has /previous_systematic_id with the same value as /systematic_id");
                }
            }
        }
    }


    /**
     * Parse a property value. What this means will depend on the specific parser used.
     * The parser may return multiple values, e.g. for semicolon-separated product terms.
     */
    private static interface TermParser {
        public Iterable<String> parse(String term) throws DataError;
    }

    /**
     * Normalise a property value. What this means will depend on the specific normaliser used.
     * A TermNormaliser is a special sort of TermParser for the common case where the result
     * is a single string.
     */
    private static abstract class TermNormaliser implements TermParser {
        public abstract String normalise(String term) throws DataError;
        public final Iterable<String> parse(String term) throws DataError {
            return Collections.singleton(normalise(term));
        }
    }

    /**
     * A term parser for products, which splits on semicolon.
     */
    private static final TermParser productParser = new TermParser() {
        public Iterable<String> parse(String term) throws DataError {
            return IterableArray.fromArray(term.split(";\\s+"));
        }
    };

    /**
     * A term normaliser for Riley classification numbers, which for example
     * will normalise "2.2.07" to "2.2.7".
     */
    private static final TermNormaliser normaliseRileyNumber = new TermNormaliser() {
        private final Pattern RILEY_PATTERN = Pattern.compile("(\\d{1,2})\\.(\\d{1,2})\\.(\\d{1,2})");
        @Override
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
        @Override
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
    private static final Map<String,TermParser> qualifierParsers = new HashMap<String,TermParser>();
    private static final Set<String> uniqueQualifiers = new HashSet<String>();
    static {
        Collections.addAll(qualifierProperties,
            "method", "colour", "status",
            "blast_file", "blastn_file", "blastp+go_file", "blastp_file",
            "blastx_file", "fasta_file", "fastx_file", "tblastn_file",
            "tblastx_file", "clustalx_file", "sigcleave_file", "pepstats_file",
            "EC_number", "private");

        Collections.addAll(uniqueQualifiers, "colour", "status");

        qualifierParsers.put("colour", normaliseInteger);
        qualifierParsers.put("product", productParser);

        // Some files (e.g. Streptococcus_pneumoniae_D39.embl) have things other than integers in /status.
        // qualifierParsers.put("status", normaliseInteger);
    }

    class CDSLoader extends GeneLoader {
        public CDSLoader(FeatureTable.CDSFeature cdsFeature) throws DataError {
            super(cdsFeature);

            isPseudo = cdsFeature.isPseudo();
            isObsolete = cdsFeature.isObsolete();
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

    private Feature loadCDS(FeatureTable.CDSFeature cdsFeature) throws DataError {
        return new CDSLoader(cdsFeature).load();
    }


    /* Loader for non-coding RNA features */
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
            int commentRank = processPropertyQualifier("note",  "feature_property", "comment");
            if (TRNA.class.isAssignableFrom(transcriptClass)) {
                processPropertyQualifier("anticodon", "feature_property", "anticodon", true);
            }

            processPropertyQualifier("colour", "genedb_misc", "colour", true);
            processCvTermQualifier("product", "genedb_products", "PRODUCT", true);
            addColourToExons();

            String label = feature.getQualifierValue("label");
            if (label != null) {
                logger.trace(String.format("Adding /label=\"%s\" as comment on '%s'",
                    label, focalFeature.getUniqueName()));
                focalFeature.addFeatureProp(String.format("/label=%s", label), "feature_property", "comment", commentRank++);
            }

            if (taxonomicDivision.equals("PRO")) {
                // Bacteria don't have splicing, so a CDS feature is a gene and
                // a transcript and that is the end of it. One or more /gene
                // qualifiers may be used to indicate synonyms.
                addTranscriptSynonymsFromQualifier("gene",    "synonym", true);
                addTranscriptSynonymsFromQualifier("synonym", "synonym", true);
            }

            for (String dbxrefs: feature.getQualifierValues("db_xref")) {
                addDbXRefs(dbxrefs);
            }

            processCuration();
            processLiterature();
        }

        @Override
        protected Class<? extends Transcript> getTranscriptClass() {
            return transcriptClass;
        }
    }

    private Feature loadNcRNA(Class<? extends NcRNA> rnaClass, String rnaType,
            FeatureTable.Feature feature) throws DataError {
        return new NcRNALoader(rnaClass, rnaType, feature).load();
    }

    /* UTR */
    private List<UTR> loadUTR(FeatureTable.Feature utrFeature) throws DataError {
                
        String utrType = utrFeature.type;
        EmblLocation utrLocation = utrFeature.location;
        String uniqueName = utrFeature.getUniqueName();

        logger.debug(String.format("Loading %s for '%s' at %s", utrType, uniqueName, utrLocation));

        Transcript transcript = transcriptsByUniqueName.get(uniqueName);
        
        /* Sometimes the UTR locus tag will have the gene name as is the case with Schisto v5.
         * In order to deal with that, I'm looking for transcripts that look like the gene 
         * name here even though, technically, the UTR locus tag is meant to have the transcript.
         * Also, we cannot make this "prediction" if there are multiple transcripts
         * nds, 16th Nov 2010
         */
                
        if(transcript == null){
            List<String> possibleTranscriptNames = new ArrayList<String>();
            
            String possibleGeneName; //trying to figure out what the gene name is
            
            if(uniqueName.matches("\\S+\\.\\d:mRNA")){ //@$$! Need to escape the .!
                possibleGeneName = uniqueName.substring(0,uniqueName.length()-7);
            }else if(uniqueName.matches("\\S+:mRNA")){
                possibleGeneName = uniqueName.substring(0,uniqueName.length()-5);
            }else{
                possibleGeneName = uniqueName;                
            }
          
            for(String s: transcriptsByUniqueName.keySet()){
                
                if(s.matches(possibleGeneName.concat(".\\d")) || s.matches(possibleGeneName.concat(".\\d:mRNA"))){
                    possibleTranscriptNames.add(s);
                }               
            }
            if(possibleTranscriptNames.size()==1){ //No alternative splicing
                transcript = transcriptsByUniqueName.get(possibleTranscriptNames.get(0));
                logger.warn(String.format("Assuming %s is the transcript for this UTR for %s", possibleTranscriptNames.get(0), uniqueName));
            }else if (possibleTranscriptNames.size()==0){
                throw new DataError(String.format("Could not find a transcript '%s' for %s", uniqueName, utrType));
            }else if (possibleTranscriptNames.size() > 1) {
                throw new DataError(String.format("Multiple transcripts possible for this UTR", uniqueName, utrType));
            }
            
        }

 
        List<UTR> utrs = new ArrayList<UTR>();

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
            String utrUniqueName = String.format("%s:%dutr", transcript.getUniqueName()/*uniqueName*/, utrClass == ThreePrimeUTR.class ? 3 : 5);
            if (utrParts.size() > 1) {
                utrUniqueName += ":" + part;
            }

            logger.debug(String.format("Creating %s feature '%s' at %d-%d",
                    utrType, utrUniqueName, utrPartLocation.getFmin(), utrPartLocation.getFmax()));

            UTR utr = transcript.createUTR(utrClass, utrUniqueName, utrPartLocation.getFmin(), utrPartLocation.getFmax());
            utrs.add(utr);
            session.persist(utr);
            ++ part;


        }

        return utrs;
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

    public void setPubDao(PubDao pubDao) {
        this.pubDao = pubDao;
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
        objectManager.setPubDao(pubDao);
    }

}
