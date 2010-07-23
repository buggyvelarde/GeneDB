package org.genedb.web.mvc.controller;

import org.genedb.web.gui.DiagramCache;
import org.genedb.web.mvc.model.TranscriptDTO;
import org.genedb.web.mvc.model.TranscriptDTOFactory;

import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;

import org.apache.log4j.Logger;

import java.util.Collection;


public class ModelBuilder {

    private static final Logger logger = Logger.getLogger(ModelBuilder.class);

    private DiagramCache diagramCache;

    private TranscriptDTOFactory transcriptDTOFactory;

    /**
     * Populate a model object with the details of the specified feature.
     * If the feature is a Polypeptide, then the corresponding transcript
     * is used.
     *
     * @param feature the feature to use
     * @param model the model object to populate
     */
    public Transcript findTranscriptForFeature(Feature feature) {
        if (feature instanceof AbstractGene) {
            return findTranscriptForGene((AbstractGene) feature);
        }
        if (feature instanceof Transcript) {
            return (Transcript) feature;
        }
        if (feature instanceof Polypeptide) {
            return ((Polypeptide) feature).getTranscript();
        }
        logger.error(String.format("Cannot build model for feature '%s' of type '%s'", feature.getUniqueName(), feature.getClass()));
        return null;
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
    public Transcript findTranscriptForGene(AbstractGene gene) {

        Collection<Transcript> transcripts = gene.getTranscripts();
        if (transcripts.isEmpty()) {
            logger.error(String.format("Gene '%s' has no transcripts", gene.getUniqueName()));
            return null;
        }

        Transcript firstTranscript = null;
        for (Transcript transcript : transcripts)
            if (firstTranscript == null || transcript.getFeatureId() < firstTranscript.getFeatureId()) {
                firstTranscript = transcript;
            }

        return firstTranscript;
    }

    /**
     * Populate a model object with the details of the specified transcript.
     *
     * @param transcript the transcript
     * @param model the model object to populate
     * @return the populated model
     */
    public TranscriptDTO prepareTranscript(Transcript transcript) {
        TranscriptDTO dto = transcriptDTOFactory.make(transcript, diagramCache);
        return dto;
    }


    public void setDiagramCache(DiagramCache diagramCache) {
        this.diagramCache = diagramCache;
    }

    public void setTranscriptDTOFactory(TranscriptDTOFactory transcriptDTOFactory) {
        this.transcriptDTOFactory = transcriptDTOFactory;
    }
}
