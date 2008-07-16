package org.genedb.web.mvc.controller;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.GeneralDao;
import org.genedb.db.dao.SequenceDao;
import org.genedb.db.domain.objects.InterProHit;
import org.genedb.db.domain.objects.PolypeptideRegionGroup;
import org.genedb.db.domain.objects.SimpleRegionGroup;

import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.GPIAnchorCleavageSite;
import org.gmod.schema.feature.MRNA;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.PolypeptideDomain;
import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.SignalPeptide;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.feature.TransmembraneRegion;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class ModelBuilder {
    private static final Logger logger = Logger.getLogger(ModelBuilder.class);

    /**
     * Generate a model object with the details of the specified feature.
     * If the feature is a Polypeptide, then the corresponding transcript
     * is used.
     *
     * @param feature the feature to use
     * @return the populated model
     */
    public Map<String, Object> prepareFeature(Feature feature) {
        return prepareFeature(feature, new HashMap<String,Object>());
    }

    /**
     * Populate a model object with the details of the specified feature.
     * If the feature is a Polypeptide, then the corresponding transcript
     * is used.
     *
     * @param feature the feature to use
     * @param model the model object to populate
     */
    public Map<String, Object> prepareFeature(Feature feature, Map<String, Object> model) {
        if (feature instanceof AbstractGene)
            return prepareGene((AbstractGene) feature, model);
        else if (feature instanceof Transcript)
            return prepareTranscript((Transcript) feature, model);
        else if (feature instanceof Polypeptide)
            return prepareTranscript(((Polypeptide) feature).getTranscript(), model);
        else {
            logger.error(String.format("Cannot build model for feature '%s' of type '%s'", feature.getUniqueName(), feature.getClass()));
            return model;
        }
    }

    /**
     * Populate a model object with the details of the specified gene.
     * If the gene is alternatively spliced, the first transcript (by <code>feature_id</code>)
     * is used.
     *
     * @param uniqueName the uniqueName of the gene
     * @param model the Map object to populate
     * @return a reference to <code>model</code>
     */
    public Map<String, Object> prepareGene(String uniqueName, Map<String, Object> model) {
        AbstractGene gene = sequenceDao.getFeatureByUniqueName(uniqueName, AbstractGene.class);
        return prepareGene(gene, model);
    }

    /**
     * Populate a model object with the details of the specified gene.
     * If the gene is alternatively spliced, the first transcript (by <code>feature_id</code>)
     * is used.
     *
     * @param gene the gene
     * @param model the Map object to populate
     * @return the populated model
     */
    public Map<String, Object> prepareGene(AbstractGene gene, Map<String, Object> model) {
        model.put("gene", gene);

        Collection<Transcript> transcripts = gene.getTranscripts();
        if (transcripts.isEmpty()) {
            logger.error(String.format("Gene '%s' has no transcripts", gene.getUniqueName()));
            return model;
        }

        Transcript firstTranscript = null;
        for (Transcript transcript : transcripts)
            if (firstTranscript == null || transcript.getFeatureId() < firstTranscript.getFeatureId())
                firstTranscript = transcript;

        return prepareTranscript(firstTranscript, model);
    }

    /**
     * Populate a model object with the details of the specified transcript.
     *
     * @param uniqueName the unique name of the transcript
     * @param model the model object to populate
     * @return the populated model
     */
    public Map<String, Object> prepareTranscript(String uniqueName, Map<String, Object> model) {
        Transcript transcript = sequenceDao.getFeatureByUniqueName(uniqueName, Transcript.class);
        return prepareTranscript(transcript, model);
    }

    /**
     * Populate a model object with the details of the specified transcript.
     *
     * @param transcript the transcript
     * @param model the model object to populate
     * @return the populated model
     */
    public Map<String,Object> prepareTranscript(Transcript transcript, Map<String,Object> model) {

        if (!model.containsKey("gene"))
            model.put("gene", transcript.getGene());

        model.put("transcript", transcript);

        if (transcript instanceof ProductiveTranscript) {
            model.put("PMID", generalDao.getDbByName("PMID").getUrlPrefix());

            ProductiveTranscript codingTranscript = (ProductiveTranscript) transcript;
            Polypeptide polypeptide = codingTranscript.getProtein();
            if (polypeptide == null) {
                /*
                 * A pseudogenic transcript need not have a polypeptide,
                 * but an mRNA transcript must. If we find an mRNA transcript
                 * that has no polypeptide, we log an error and continue
                 * as best we can.
                 */
                if (transcript instanceof MRNA)
                    logger.error(String.format("Transcript '%s' has no polypeptide!",
                        codingTranscript.getUniqueName()));
                return model;
            }

            model.put("polypeptide", polypeptide);
            model.put("pepstats", polypeptide.calculateStats());

            model.put("CC",        cvDao.getCountedNamesByCvNamePatternAndFeatureAndOrganism("CC\\_*", polypeptide));
            model.put("BP",        cvDao.getCountedNamesByCvNameAndFeatureAndOrganism("biological_process", polypeptide));
            model.put("CellularC", cvDao.getCountedNamesByCvNameAndFeatureAndOrganism("cellular_component", polypeptide));
            model.put("MF",        cvDao.getCountedNamesByCvNameAndFeatureAndOrganism("molecular_function", polypeptide));

            List<PolypeptideRegionGroup> domainInformation = prepareDomainInformation(polypeptide);
            model.put("domainInformation", domainInformation);
            model.put("algorithmData", prepareAlgorithmData(polypeptide));
        }
        return model;
    }

    private List<PolypeptideRegionGroup> prepareDomainInformation(Polypeptide polypeptide) {
        Map<DbXRef, org.genedb.db.domain.objects.InterProHit> interProHitsByDbXRef
            = new HashMap<DbXRef, org.genedb.db.domain.objects.InterProHit>();
        SimpleRegionGroup otherMatches = new SimpleRegionGroup ("Other matches");

        for (PolypeptideDomain domain: polypeptide.getRegions(PolypeptideDomain.class)) {

            org.genedb.db.domain.objects.DatabasePolypeptideRegion thisHit
                = org.genedb.db.domain.objects.DatabasePolypeptideRegion.build(domain);

            if (thisHit == null) {
                continue;
            }

            DbXRef interProDbXRef = domain.getInterProDbXRef();
            Hibernate.initialize(interProDbXRef);
            if (interProDbXRef == null)
                otherMatches.addDomain(thisHit);
            else {
                if (!interProHitsByDbXRef.containsKey(interProDbXRef))
                    interProHitsByDbXRef.put(interProDbXRef,
                        new InterProHit(interProDbXRef));
                interProHitsByDbXRef.get(interProDbXRef).addDomain(thisHit);
            }
        }

        List<PolypeptideRegionGroup> domainInformation = new ArrayList<PolypeptideRegionGroup>(interProHitsByDbXRef.values());

        if (!otherMatches.isEmpty()) {
            domainInformation.add(otherMatches);
        }

        return domainInformation;
    }

    private <S> void putIfNotEmpty(Map<S,? super Map<?,?>> map, S key, Map<?,?> value) {
        if (!value.isEmpty()) {
            map.put(key, value);
        }
    }
    private <S> void putIfNotEmpty(Map<S,? super Collection<?>> map, S key, Collection<?> value) {
        if (!value.isEmpty()) {
            map.put(key, value);
        }
    }
    private <S,T> void putIfNotNull(Map<S,T> map, S key, T value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private Map<String,Object> prepareAlgorithmData(Polypeptide polypeptide) {
        Map<String,Object> algorithmData = new HashMap<String,Object>();
        putIfNotEmpty(algorithmData, "SignalP", prepareSignalPData(polypeptide));
        putIfNotEmpty(algorithmData, "DGPI", prepareDGPIData(polypeptide));
        putIfNotEmpty(algorithmData, "PlasmoAP", preparePlasmoAPData(polypeptide));
        putIfNotEmpty(algorithmData, "TMHMM", prepareTMHMMData(polypeptide));
        return algorithmData;
    }

    private Map<String,Object> prepareSignalPData(Polypeptide polypeptide) {
        Map<String,Object> signalPData = new HashMap<String,Object>();

        String prediction  = polypeptide.getProperty("genedb_misc", "SignalP_prediction");
        String peptideProb = polypeptide.getProperty("genedb_misc", "signal_peptide_probability");
        String anchorProb  = polypeptide.getProperty("genedb_misc", "signal_anchor_probability");

        putIfNotNull(signalPData, "prediction",  prediction);
        putIfNotNull(signalPData, "peptideProb", peptideProb);
        putIfNotNull(signalPData, "anchorProb",  anchorProb);

        Collection<SignalPeptide> signalPeptides = polypeptide.getRegions(SignalPeptide.class);
        if (!signalPeptides.isEmpty()) {
            if (signalPeptides.size() > 1) {
                logger.error(String.format("Polypeptide '%s' has %d signal peptide regions; only expected one",
                    polypeptide.getUniqueName(), signalPeptides.size()));
            }
            SignalPeptide signalPeptide = signalPeptides.iterator().next();
            FeatureLoc signalPeptideLoc = signalPeptide.getRankZeroFeatureLoc();

            signalPData.put("cleavageSite", signalPeptideLoc.getFmax());
            signalPData.put("cleavageSiteProb", signalPeptide.getProbability());
        }

        return signalPData;
    }

    private Map<String,Object> prepareDGPIData(Polypeptide polypeptide) {
        Map<String,Object> dgpiData = new HashMap<String,Object>();

        /* If the GPI_anchored property is not present, we do not add
         * anything here. That way, an empty map indicates that there
         * is no DGPI data.
         */
        if (polypeptide.hasProperty("genedb_misc", "GPI_anchored")) {
            dgpiData.put("anchored", true);
        }

        Collection<GPIAnchorCleavageSite> cleavageSites = polypeptide.getRegions(GPIAnchorCleavageSite.class);
        if (!cleavageSites.isEmpty()) {
            if (cleavageSites.size() > 1) {
                logger.error(String.format("There are %d GPI anchor cleavage sites on polypeptide '%s'; only expected one",
                    cleavageSites.size(), polypeptide.getUniqueName()));
            }
            GPIAnchorCleavageSite cleavageSite = cleavageSites.iterator().next();
            FeatureLoc cleavageSiteLoc = cleavageSite.getRankZeroFeatureLoc();
            dgpiData.put("location", cleavageSiteLoc.getFmax());
            dgpiData.put("score", cleavageSite.getScore());
        }

        return dgpiData;
    }

    private Map<String,Object> preparePlasmoAPData(Polypeptide polypeptide) {
        Map<String,Object> plasmoAPData = new HashMap<String,Object>();
        String score = polypeptide.getProperty("genedb_misc", "PlasmoAP_score");
        if (score != null) {
            plasmoAPData.put("score", score);
            switch (Integer.parseInt(score)) {
            case 0: case 1: case 2: plasmoAPData.put("description", "Unlikely"); break;
            case 3: plasmoAPData.put("description", "Unknown"); break;
            case 4: plasmoAPData.put("description", "Likely"); break;
            case 5: plasmoAPData.put("description", "Very likely"); break;
            default:
                throw new RuntimeException(String.format("Polypeptide '%s' has unrecognised PlasmoAP score '%s'",
                    polypeptide.getUniqueName(), score));
            }
        }

        return plasmoAPData;
    }

    private List<String> prepareTMHMMData(Polypeptide polypeptide) {
        SortedSet<TransmembraneRegion> sortedTransmembraneRegions = new TreeSet<TransmembraneRegion>(
                new Comparator<TransmembraneRegion>() {

                    @Override
                    public int compare(TransmembraneRegion a, TransmembraneRegion b) {
                        FeatureLoc aLoc = a.getRankZeroFeatureLoc();
                        FeatureLoc bLoc = b.getRankZeroFeatureLoc();
                        return aLoc.getFmin() - bLoc.getFmin();
                    }
                });
        sortedTransmembraneRegions.addAll(polypeptide.getRegions(TransmembraneRegion.class));

        List<String> tmhmmData = new ArrayList<String>();

        for (TransmembraneRegion transmembraneRegion: sortedTransmembraneRegions) {
            tmhmmData.add(String.format("%d-%d",
                1 + transmembraneRegion.getRankZeroFeatureLoc().getFmin(),
                transmembraneRegion.getRankZeroFeatureLoc().getFmax()));
        }

        return tmhmmData;
    }

    private SequenceDao sequenceDao;
    private GeneralDao generalDao;
    private CvDao cvDao;

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }

    public void setGeneralDao(GeneralDao generalDao) {
        this.generalDao = generalDao;
    }
}
