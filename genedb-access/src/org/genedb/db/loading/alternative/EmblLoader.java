package org.genedb.db.loading.alternative;

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
import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.Pseudogene;
import org.gmod.schema.feature.PseudogenicTranscript;
import org.gmod.schema.feature.RepeatRegion;
import org.gmod.schema.feature.Supercontig;
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
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Transactional
class EmblLoader {
    private static final Logger logger = Logger.getLogger(EmblLoader.class);

    // Injected beans
    private GeneralDao generalDao;     // See #afterPropertiesSet()
    private OrganismDao organismDao;
    private Organism organism;
    private ObjectManager objectManager;
    private SessionFactory sessionFactory;

    public void setOrganismCommonName(String organismCommonName) {
        this.organism = organismDao.getOrganismByCommonName(organismCommonName);
        if (organism == null) {
            throw new IllegalArgumentException(String.format("Organism '%s' not found", organismCommonName));
        }
    }

    private Session session;
    public void load(EmblFile emblFile) throws DataError {

        this.session = SessionFactoryUtils.doGetSession(sessionFactory, false);
        boolean alreadyThere = session.createCriteria(Feature.class)
            .add(Restrictions.eq("organism", organism))
            .add(Restrictions.eq("uniqueName", emblFile.getAccession()))
            .uniqueResult() != null;

        if (!alreadyThere) {
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
    private Map<String,AbstractGene> genesBySharedId;
    private Map<String,Transcript> transcriptsByUniqueName;
    private NavigableMap<Integer,Contig> contigsByStart;

    private void init(Supercontig supercontig) {
        this.supercontig = supercontig;
        this.genesBySharedId = new HashMap<String,AbstractGene>();
        this.transcriptsByUniqueName = new HashMap<String,Transcript>();
        this.contigsByStart = new TreeMap<Integer,Contig>();
    }

    private void loadContigsAndGaps(EmblLocation.Join locations) throws DataError {
        int pos = 0; // Position (interbase) on supercontig
        for(EmblLocation location: locations.locations) {
            if (location instanceof EmblLocation.External) {
                EmblLocation.External externalLocation = (EmblLocation.External) location;
                int contigLength = externalLocation.simple.getLength();

                logger.info(String.format("Creating contig '%s' at %d-%d", externalLocation.accession, pos, pos + contigLength));
                Contig contig = TopLevelFeature.make(Contig.class, externalLocation.accession, organism);
                contig.setResidues(supercontig.getResidues(pos, pos + contigLength));
                session.persist(contig);
                supercontig.addLocatedChild(contig, pos, pos + contigLength, (short) 0, 0);

                contigsByStart.put(pos, contig);

                pos += contigLength;
            } else if (location instanceof EmblLocation.Gap) {
                EmblLocation.Gap gapLocation = (EmblLocation.Gap) location;
                int gapLength = gapLocation.getLength();

                logger.info(String.format("Creating gap at %d-%d", pos, pos + gapLength));
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
            logger.error(String.format("The feature '%s' (%s) is not contained in a contig",
                feature.getUniqueName(), feature.getName()));
            return;
        }
        logger.info(String.format("The feature '%s' lies on contig '%s'", feature.getUniqueName(), contig.getUniqueName()));
        contig.addLocatedChild(feature, fmin - contig.getFmin(), fmax - contig.getFmin(), strand, phase, 1);
    }

    private void loadFeatures(EmblFile.FeatureTable featureTable) throws DataError {
        List<EmblFile.FeatureTable.Feature> utrs = new ArrayList<EmblFile.FeatureTable.Feature>();

        for (EmblFile.FeatureTable.Feature feature: featureTable.getFeatures()) {
            String featureType = feature.type;

            if (featureType.equals("repeat_region")) {
                loadRepeatRegion(feature);
            }
            else if (featureType.equals("CDS")) {
                loadCDS(feature);
            }
            else if (featureType.equals("3'UTR") || featureType.equals("5'UTR")) {
                utrs.add(feature);
            }
            else {
                logger.warn(String.format("Ignoring '%s' feature", featureType));
            }
        }

        for (EmblFile.FeatureTable.Feature utr: utrs) {
            loadUTR(utr);
        }
    }

    private void loadRepeatRegion(EmblFile.FeatureTable.Feature repeatRegionFeature) throws DataError {
        String repeatRegionName = repeatRegionFeature.getQualifierValue("FEAT_NAME");
        EmblLocation repeatRegionLocation = repeatRegionFeature.location;
        int fmin = repeatRegionLocation.getFmin();
        int fmax = repeatRegionLocation.getFmax();

        logger.info(String.format("Creating repeat region '%s' at %d-%d", repeatRegionName, fmin, fmax));
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

    private void loadCDS(EmblFile.FeatureTable.Feature cdsFeature) throws DataError {
        EmblLocation cdsLocation = cdsFeature.location;
        boolean isPseudo = cdsFeature.hasQualifier("pseudo");

        String sharedId = cdsFeature.getQualifierValue("shared_id");
        List<String> otherTranscripts = cdsFeature.getQualifierValues("other_transcript");
        String uniqueName = cdsFeature.getUniqueName();

        Class<? extends AbstractGene> geneClass = isPseudo ? Pseudogene.class : Gene.class;
        AbstractGene gene;
        if (sharedId == null && otherTranscripts.isEmpty()) {
            // Not alternatively spliced

            if (uniqueName.contains(".")) {
                logger.warn(String.format(
                    "The transcript '%s' is not alternately spliced, yet its systematic name contains a dot",
                    uniqueName));
            }

            sharedId = uniqueName;
            gene = AbstractGene.make(geneClass, organism, uniqueName, cdsFeature.getQualifierValue("FEAT_NAME"));
            locate(gene, cdsLocation);
            session.persist(gene);
        } else {
            if (sharedId == null) {
                // An alternately-spliced transcript does not always have a /shared_id qualifier.
                // Sometimes there are just a selection of /other_transcript qualifiers.
                Matcher dotMatcher = Pattern.compile("(.*)\\.\\d+").matcher(uniqueName);
                if (! dotMatcher.matches()) {
                    logger.error(String.format(
                        "Alternately-spliced transcript '%s' has no /shared_id qualifier, and its systematic name doesn't end with .<n>",
                        uniqueName));
                    return;
                }
                sharedId = dotMatcher.group(1);
                logger.info(String.format("[CDS %s] assuming /shared_id of '%s'", uniqueName, sharedId));
            }

            if (genesBySharedId.containsKey(sharedId)) {
                logger.debug(String.format("Gene for shared ID '%s' already exists", sharedId));
                gene = genesBySharedId.get(sharedId);
            } else {
                // This is the first transcript, so create the gene
                logger.debug(String.format("Creating gene for shared ID '%s'", sharedId));
                gene = AbstractGene.make(geneClass, organism, sharedId, null); // ??? Can the gene name be represented in the EMBL file? How?
                locate(gene, cdsLocation);

                /*
                 * Note that we're using whether or not the CDS has a /temporary_systematic_id
                 * to decide whether the gene ID is temporary or not. This is a bit odd, but
                 * I don't think there's any other way to decide.
                 */
                if (cdsFeature.hasQualifier("temporary_systematic_id")) {
                    gene.setTemporarySystematicId(sharedId);
                }
                else {
                    gene.setSystematicId(sharedId);
                }

                session.persist(gene);
            }
        }

        genesBySharedId.put(sharedId, gene);

        logger.debug(String.format("Creating transcript '%s' for gene '%s'", uniqueName, gene.getUniqueName()));
        Class<? extends Transcript> transcriptClass = isPseudo ? PseudogenicTranscript.class : MRNA.class;
        Transcript transcript = gene.makeTranscript(transcriptClass, uniqueName);

        transcriptsByUniqueName.put(uniqueName, transcript);

        for (String synonymString: cdsFeature.getQualifierValues("synonym")) {
            logger.debug(String.format("Adding synonym '%s' for transcript", synonymString));
            Synonym synonym = objectManager.getSynonym("synonym", synonymString);
            session.persist(synonym);
            transcript.addSynonym(synonym);
        }

        for (String previousSystematicIdString: cdsFeature.getQualifierValues("previous_systematic_id")) {
            logger.debug(String.format("Adding previous systematic ID '%s' for transcript", previousSystematicIdString));
            Synonym synonym = objectManager.getSynonym("systematic_id", previousSystematicIdString);
            session.persist(synonym);
            transcript.addNonCurrentSynonym(synonym);
        }

        for(String note: cdsFeature.getQualifierValues("note")) {
            logger.debug(String.format("Adding comment '%s' for transcript", note));
            if (transcript instanceof ProductiveTranscript) {
                ((ProductiveTranscript) transcript).getProtein().addFeatureProp(note, "feature_property", "comment");
            } else {
                transcript.addFeatureProp(note, "feature_property", "comment");
            }
        }

        for(String method: cdsFeature.getQualifierValues("method")) {
            if (transcript instanceof ProductiveTranscript) {
                ((ProductiveTranscript) transcript).getProtein().addFeatureProp(method, "genedb_misc", "method");
            } else {
                transcript.addFeatureProp(method, "genedb_misc", "method");
            }
        }

        if (transcript instanceof ProductiveTranscript) {
            ProductiveTranscript productiveTranscript = (ProductiveTranscript) transcript;
            for (String product: cdsFeature.getQualifierValues("product")) {
                productiveTranscript.getProtein().addProduct(product);
            }
        }

        // Add exons
        int exonIndex = 0;
        for (EmblLocation exonLocation: cdsLocation.getParts()) {
            String exonUniqueName = String.format("%s:exon:%d", transcript.getUniqueName(), ++exonIndex);
            logger.debug(String.format("Creating exon '%s' at %d-%d", exonUniqueName, exonLocation.getFmin(), exonLocation.getFmax()));
            AbstractExon exon = transcript.createExon(exonUniqueName, exonLocation.getFmin(), exonLocation.getFmax());
            session.persist(exon);
        }
    }

    private void loadUTR(EmblFile.FeatureTable.Feature utrFeature) throws DataError {
        String utrType = utrFeature.type;
        EmblLocation utrLocation = utrFeature.location;
        String uniqueName = utrFeature.getUniqueName();

        logger.trace(String.format("Loading %s for '%s' at %s", utrType, uniqueName, utrLocation));

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

            logger.trace(String.format("Creating %s feature '%s' at %d-%d",
                utrType, utrUniqueName, utrPartLocation.getFmin(), utrPartLocation.getFmax()));

            transcript.createUTR(utrClass, utrUniqueName, utrPartLocation.getFmin(), utrPartLocation.getFmax());
            ++ part;
        }
    }

    public void setOrganismDao(OrganismDao organismDao) {
        this.organismDao = organismDao;
    }

    public void setObjectManager(ObjectManager objectManager) {
        this.objectManager = objectManager;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setGeneralDao(GeneralDao generalDao) {
        this.generalDao = generalDao;
    }

    public void afterPropertiesSet() {
        /*
         * We cannot set the generalDao of the objectManager
         * directly in Load.xml, because that creates a circular
         * reference that (understandably) causes Spring to
         * throw a tantrum. Thus we inject the generalDao into
         * here, and pass it to the ObjectManager after Spring
         * configuration.
         */
        objectManager.setGeneralDao(generalDao);
    }
}
