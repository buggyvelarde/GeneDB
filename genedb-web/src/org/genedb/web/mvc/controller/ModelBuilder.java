package org.genedb.web.mvc.controller;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.GeneralDao;
import org.genedb.db.dao.SequenceDao;
import org.genedb.db.domain.objects.InterProHit;
import org.genedb.db.domain.objects.PolypeptideRegionGroup;
import org.genedb.db.domain.objects.SimpleRegionGroup;
import org.genedb.web.mvc.model.TranscriptDTO;
import org.genedb.web.mvc.model.TranscriptDtoFactory;

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
import org.gmod.schema.mapped.FeatureCvTerm;
import org.gmod.schema.mapped.FeatureLoc;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelBuilder {
    private static final Logger logger = Logger.getLogger(ModelBuilder.class);

    @Autowired
    private TranscriptDtoFactory transcriptDtoFactory;

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

        if (!model.containsKey("gene")) {
            model.put("gene", transcript.getGene());
        }

        model.put("transcript", transcript);


        TranscriptDTO dto = transcriptDtoFactory.makeDto(transcript);
        model.put("dto", dto);

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
                if (transcript instanceof MRNA) {
                    logger.error(String.format("Transcript '%s' has no polypeptide!",
                        codingTranscript.getUniqueName()));
                }
                return model;
            }

            model.put("polypeptide", polypeptide);

            model.put("CC",        cvDao.getCountedNamesByCvNamePatternAndFeatureAndOrganism("CC\\_*", polypeptide));
            //model.put("BP",        cvDao.getCountedNamesByCvNameAndFeatureAndOrganism("biological_process", polypeptide));
            //model.put("CellularC", cvDao.getCountedNamesByCvNameAndFeatureAndOrganism("cellular_component", polypeptide));
            //model.put("MF",        cvDao.getCountedNamesByCvNameAndFeatureAndOrganism("molecular_function", polypeptide));

            List<PolypeptideRegionGroup> domainInformation = prepareDomainInformation(polypeptide);
            model.put("domainInformation", domainInformation);
        }
        return model;
    }

    private List<PolypeptideRegionGroup> prepareDomainInformation(Polypeptide polypeptide) {
        Map<DbXRef, org.genedb.db.domain.objects.InterProHit> interProHitsByDbXRef
            = new HashMap<DbXRef, org.genedb.db.domain.objects.InterProHit>();
        SimpleRegionGroup otherMatches = new SimpleRegionGroup ("Other matches", "Other");

        for (PolypeptideDomain domain: polypeptide.getRegions(PolypeptideDomain.class)) {

            org.genedb.db.domain.objects.DatabasePolypeptideRegion thisHit
                = org.genedb.db.domain.objects.DatabasePolypeptideRegion.build(domain);

            if (thisHit == null) {
                continue;
            }

            DbXRef interProDbXRef = domain.getInterProDbXRef();
            Hibernate.initialize(interProDbXRef);
            if (interProDbXRef == null)
                otherMatches.addRegion(thisHit);
            else {
                if (!interProHitsByDbXRef.containsKey(interProDbXRef))
                    interProHitsByDbXRef.put(interProDbXRef,
                        new InterProHit(interProDbXRef));
                interProHitsByDbXRef.get(interProDbXRef).addRegion(thisHit);
            }
        }

        List<PolypeptideRegionGroup> domainInformation = new ArrayList<PolypeptideRegionGroup>(interProHitsByDbXRef.values());

        if (!otherMatches.isEmpty()) {
            domainInformation.add(otherMatches);
        }

        return domainInformation;
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
