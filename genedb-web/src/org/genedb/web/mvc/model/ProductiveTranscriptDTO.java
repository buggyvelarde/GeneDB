package org.genedb.web.mvc.model;

import org.genedb.web.mvc.controller.ModelBuilder;

import org.gmod.schema.feature.GPIAnchorCleavageSite;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.SignalPeptide;
import org.gmod.schema.feature.TransmembraneRegion;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.utils.PeptideProperties;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductiveTranscriptDTO extends TranscriptDTO {

    private static final Logger logger = Logger.getLogger(ProductiveTranscriptDTO.class);
    private Map<String,Object> algorithmData;
    private PeptideProperties polypeptideProperties;

    public void populate(ProductiveTranscript transcript) {
        super.populate(transcript);
        Polypeptide polypeptide = transcript.getProtein();
        this.algorithmData = prepareAlgorithmData(polypeptide);
        this.polypeptideProperties = polypeptide.calculateStats();
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
        /* If the GPI_anchored property is not present, we do not add the
         * predicted cleavage site, even if there is one.
         */
        if (!polypeptide.hasProperty("genedb_misc", "GPI_anchored")) {
            return Collections.emptyMap();
        }

        Map<String,Object> dgpiData = new HashMap<String,Object>();
        dgpiData.put("anchored", true);

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
        List<String> tmhmmData = new ArrayList<String>();

        for (TransmembraneRegion transmembraneRegion: polypeptide.getRegions(TransmembraneRegion.class)) {
            tmhmmData.add(String.format("%d-%d",
                1 + transmembraneRegion.getRankZeroFeatureLoc().getFmin(),
                transmembraneRegion.getRankZeroFeatureLoc().getFmax()));
        }

        return tmhmmData;
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

    private <S> void putIfNotEmpty(Map<S,? super Map<?,?>> map, S key, Map<?,?> value) {
        if (!value.isEmpty()) {
            map.put(key, value);
        }
    }

    public Map<String, Object> getAlgorithmData() {
        return algorithmData;
    }


    public PeptideProperties getPolypeptideProperties() {
        return polypeptideProperties;
    }

}
