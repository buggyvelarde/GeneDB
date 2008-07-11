package org.genedb.web.mvc.controller;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.GeneralDao;
import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.general.DbXRef;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureLoc;
import org.gmod.schema.sequence.feature.AbstractGene;
import org.gmod.schema.sequence.feature.MRNA;
import org.gmod.schema.sequence.feature.Polypeptide;
import org.gmod.schema.sequence.feature.PolypeptideDomain;
import org.gmod.schema.sequence.feature.ProductiveTranscript;
import org.gmod.schema.sequence.feature.Transcript;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ModelBuilder {
    private static final Logger logger = Logger.getLogger(ModelBuilder.class);

    /**
     * Populate a model object with the details of the specified feature.
     * If the feature is a Polypeptide, then the corresponding transcript
     * is used.
     *
     * @param feature the feature to use
     * @param model the model object to populate. If null, a new empty model is created first.
     * @return the populated model
     */
    public Map<String, Object> prepareFeature(Feature feature, Map<String, Object> model) {
        if (model == null) {
            model = new HashMap<String,Object>();
        }
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
            model.put("polyprop", polypeptide.calculateProperties());

            model.put("CC",        cvDao.getCountedNamesByCvNamePatternAndFeatureAndOrganism("CC\\_*", polypeptide));
            model.put("BP",        cvDao.getCountedNamesByCvNameAndFeatureAndOrganism("biological_process", polypeptide));
            model.put("CellularC", cvDao.getCountedNamesByCvNameAndFeatureAndOrganism("cellular_component", polypeptide));
            model.put("MF",        cvDao.getCountedNamesByCvNameAndFeatureAndOrganism("molecular_function", polypeptide));

            model.put("domainInformation", prepareDomainInformation(polypeptide));
        }
        return model;
    }

    /**
     * How to order the hits within each subsection.
     * Here we order them by database and accession ID.
     */
    private static final Comparator<Map<String, Object>> hitComparator
        = new Comparator<Map<String, Object>> ()
    {
        public int compare(Map<String, Object> a, Map<String, Object> b) {
            DbXRef aDbXRef = (DbXRef) a.get("dbxref");
            DbXRef bDbXRef = (DbXRef) b.get("dbxref");
            int dbNameComparison = aDbXRef.getDb().getName().compareTo(bDbXRef.getDb().getName());
            if (dbNameComparison != 0)
                return dbNameComparison;
            return aDbXRef.getAccession().compareTo(bDbXRef.getAccession());
        }
    };
    private List<Map<String,Object>> prepareDomainInformation(Polypeptide polypeptide) {
        Map<DbXRef, Set<Map<String, Object>>> detailsByInterProHit
            = new HashMap<DbXRef, Set<Map<String, Object>>>();
        Set<Map<String, Object>> otherMatches = new HashSet<Map<String, Object>> ();

        for (PolypeptideDomain domain: polypeptide.getRegions(PolypeptideDomain.class)) {
            FeatureLoc domainLoc = domain.getRankZeroFeatureLoc();
            DbXRef dbxref = domain.getDbXRef();
            if (dbxref == null) {
                logger.error(String.format("The polypeptide domain '%s' has no DbXRef",
                    domain.getUniqueName()));
                continue;
            }

            Map<String, Object> thisHit = new HashMap<String, Object>();
            thisHit.put("dbxref", dbxref);
            thisHit.put("fmin", domainLoc.getFmin());
            thisHit.put("fmax", domainLoc.getFmax());
            thisHit.put("score", domain.getScore());

            DbXRef interProDbXRef = domain.getInterProDbXRef();
            Hibernate.initialize(interProDbXRef);
            if (interProDbXRef == null)
                otherMatches.add(thisHit);
            else {
                if (!detailsByInterProHit.containsKey(interProDbXRef))
                    detailsByInterProHit.put(interProDbXRef,
                        new TreeSet<Map<String, Object>>(hitComparator));
                detailsByInterProHit.get(interProDbXRef).add(thisHit);
            }
        }

        List<Map<String,Object>> domainInformation = new ArrayList<Map<String,Object>>();

        for (Map.Entry<DbXRef,Set<Map<String,Object>>> entry: detailsByInterProHit.entrySet()) {
            DbXRef interProDbXRef = entry.getKey();
            Set<Map<String,Object>> hits = entry.getValue();

            Map<String,Object> subsection = new HashMap<String,Object>();
            subsection.put("interproDbXRef", interProDbXRef);
            subsection.put("hits", hits);
            domainInformation.add(subsection);
        }

        if (!otherMatches.isEmpty()) {
            Map<String,Object> otherMatchesSubsection = new HashMap<String,Object>();
            otherMatchesSubsection.put("hits", otherMatches);
            domainInformation.add(otherMatchesSubsection);
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
