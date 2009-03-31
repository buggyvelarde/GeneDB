package org.genedb.web.mvc.model;

import org.genedb.db.dao.AuditDao;
import org.genedb.db.dao.SequenceDao;
import org.genedb.db.domain.objects.DatabasePolypeptideRegion;
import org.genedb.db.domain.objects.InterProHit;
import org.genedb.db.domain.objects.PolypeptideRegionGroup;
import org.genedb.db.domain.objects.SimpleRegionGroup;
import org.genedb.web.gui.DiagramCache;
import org.genedb.web.gui.ImageMapSummary;
import org.genedb.web.gui.ProteinMapDiagram;
import org.genedb.web.gui.RenderedDiagramFactory;
import org.genedb.web.gui.RenderedProteinMap;

import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.GPIAnchorCleavageSite;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.PolypeptideDomain;
import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.PseudogenicTranscript;
import org.gmod.schema.feature.SignalPeptide;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.feature.TransmembraneRegion;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureCvTerm;
import org.gmod.schema.mapped.FeatureDbXRef;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.FeatureProp;
import org.gmod.schema.mapped.FeaturePub;
import org.gmod.schema.mapped.FeatureRelationship;
import org.gmod.schema.mapped.FeatureSynonym;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.mapped.Synonym;
import org.gmod.schema.utils.PeptideProperties;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class TranscriptDTO implements Serializable {

    private transient Logger logger = Logger.getLogger(TranscriptDTO.class);

    private static final long serialVersionUID = 3878466785198622703L;

    @Autowired
    private transient SequenceDao sequenceDao;

    @Autowired
    private transient AuditDao auditDao;

    private String uniqueName;
    private String properName;
    private Map<String, List<String>> synonymsByTypes;
    private List<String> synonyms;
    private List<String> obsoleteNames;
    private List<FeatureCvTermDTO> products;
    private String typeDescription;
    private String topLevelFeatureType;
    private String topLevelFeatureDisplayName;
    private String topLevelFeatureUniqueName;
    private int topLevelFeatureLength;
    private String geneName;
    private List<String> notes;
    private List<String> comments;
    private boolean pseudo;
    private boolean anAlternateTranscript;
    private String location;
    private List<DbXRefDTO> dbXRefDTOs;
    private int min;
    private int max;
    List<FeatureCvTermDTO> controlledCurations;
    List<FeatureCvTermDTO> goBiologicalProcesses;
    List<FeatureCvTermDTO> goMolecularFunctions;
    List<FeatureCvTermDTO> goCellularComponents;
    List<String> clusterIds;
    List<String> orthologueNames;
    List<PolypeptideRegionGroup> domainInformation;
    private ImageMapSummary ims;
    private Map<String,Object> algorithmData;
    private PeptideProperties polypeptideProperties;
    private boolean proteinCoding;
    private String organismCommonName;
    private String organismHtmlShortName;
    private List<String> publications;
    private long lastModified = Long.MIN_VALUE;


    public String getOrganismCommonName() {
        return organismCommonName;
    }

    public String getOrganismHtmlShortName() {
        return organismHtmlShortName;
    }

    private Map<String,Object> prepareAlgorithmData(Polypeptide polypeptide) {
        Map<String,Object> aData = new HashMap<String,Object>();
        putIfNotEmpty(aData, "SignalP", prepareSignalPData(polypeptide));
        putIfNotEmpty(aData, "DGPI", prepareDGPIData(polypeptide));
        putIfNotEmpty(aData, "PlasmoAP", preparePlasmoAPData(polypeptide));
        putIfNotEmpty(aData, "TMHMM", prepareTMHMMData(polypeptide));
        return aData;
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
            case 0:
            case 1:
            case 2:
                plasmoAPData.put("description", "Unlikely");
                break;
            case 3:
                plasmoAPData.put("description", "Unknown");
                break;
            case 4:
                plasmoAPData.put("description", "Likely");
                break;
            case 5:
                plasmoAPData.put("description", "Very likely");
                break;
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




    public void populate(Transcript transcript, DiagramCache diagramCache) {
        AbstractGene gene = transcript.getGene();
        Polypeptide polypeptide = null;
        if (transcript instanceof ProductiveTranscript) {
            polypeptide = ((ProductiveTranscript)transcript).getProtein();
            if (transcript instanceof PseudogenicTranscript) {
                pseudo = true;
            } else {
                pseudo = false;
            }
        }

        //---------------------
        if (polypeptide == null) {
            logger.error(String.format("The transcript '%s' has no polypeptide", transcript.getUniqueName()));
            //return;
        } else {
            proteinCoding = true;
        }

        // =-----------------------------

        if (gene.getTranscripts().size()>1) {
            anAlternateTranscript = true;
        }
        populateNames(transcript, gene);
        populateParentDetails(gene);
        populateMisc(transcript);
        populateOrganismDetails(transcript);
        populateLastModified(transcript, polypeptide);

        if (polypeptide != null) {
            this.algorithmData = prepareAlgorithmData(polypeptide);
            this.polypeptideProperties = polypeptide.calculateStats();
            populateFromFeatureProps(polypeptide);

            products = populateFromFeatureCvTerms(polypeptide, "genedb_products");
            controlledCurations = populateFromFeatureCvTerms(polypeptide, "CC_");
            goBiologicalProcesses = populateFromFeatureCvTerms(polypeptide, "biological_process");
            goMolecularFunctions = populateFromFeatureCvTerms(polypeptide, "molecular_function");
            goCellularComponents = populateFromFeatureCvTerms(polypeptide, "cellular_component");
            populateFromFeatureDbXrefs(polypeptide);

            populateFromFeatureRelationships(polypeptide);

            populateFromFeaturePubs(polypeptide);

            domainInformation = prepareDomainInformation(polypeptide);
            //model.put("domainInformation", domainInformation);



                // Get image
            ProteinMapDiagram diagram = new ProteinMapDiagram(polypeptide, transcript, domainInformation);
            if (!diagram.isEmpty()) {
                RenderedProteinMap renderedProteinMap = (RenderedProteinMap) RenderedDiagramFactory.getInstance().getRenderedDiagram(diagram);

                        try {
                            ims = new ImageMapSummary(
                                    renderedProteinMap.getWidth(),
                                    renderedProteinMap.getHeight(),
                                    diagramCache.fileForProteinMap(renderedProteinMap),
                                    renderedProteinMap.getRenderedFeaturesAsHTML("proteinMapMap"));
                        } catch (IOException exp) {
                            ims = null;
                            logger.error("Failed to create an imageMapSummary", exp);
                        }
            }



        }

    }

    private void populateLastModified(Transcript transcript, Polypeptide polypeptide) {
            Date date = auditDao.getLastChangeForExistingFeature(transcript.getFeatureId());

            if (polypeptide != null) {
                Date polypeptideDate = auditDao.getLastChangeForExistingFeature(polypeptide.getFeatureId());
                if (date == null && polypeptideDate == null) {
                    return;
                }
                if (polypeptideDate != null && polypeptideDate.after(date)) {
                    date = polypeptideDate;
                }
            }

            if (date != null) {
                lastModified = date.getTime();
            }
    }

    private void populateOrganismDetails(Transcript transcript) {
        this.organismCommonName = transcript.getOrganism().getCommonName();
        this.organismHtmlShortName = transcript.getOrganism().getHtmlShortName();
    }

    private List<PolypeptideRegionGroup> prepareDomainInformation(Polypeptide polypeptide) {

        Map<DbXRef, InterProHit> interProHitsByDbXRef= Maps.newHashMap();
        SimpleRegionGroup otherMatches = new SimpleRegionGroup("Other matches", "Other");

        for (PolypeptideDomain domain: polypeptide.getRegions(PolypeptideDomain.class)) {

            DatabasePolypeptideRegion thisHit = DatabasePolypeptideRegion.build(domain);

            if (thisHit == null) {
                continue;
            }

            DbXRef interProDbXRef = domain.getInterProDbXRef();
            Hibernate.initialize(interProDbXRef);
            if (interProDbXRef == null) {
                otherMatches.addRegion(thisHit);
            } else {
                if (!interProHitsByDbXRef.containsKey(interProDbXRef)) {
                    interProHitsByDbXRef.put(interProDbXRef, new InterProHit(interProDbXRef));
                }
                interProHitsByDbXRef.get(interProDbXRef).addRegion(thisHit);
            }
        }

        List<PolypeptideRegionGroup> domainInfo = new ArrayList<PolypeptideRegionGroup>(interProHitsByDbXRef.values());

        if (!otherMatches.isEmpty()) {
            domainInfo.add(otherMatches);
        }

        return domainInfo;
    }

    private void populateFromFeatureRelationships(Polypeptide polypeptide) {

        clusterIds = Lists.newArrayList();
        orthologueNames = Lists.newArrayList();
        List<FeatureRelationship> filtered =
            polypeptide.getFeatureRelationshipsForSubjectIdFilteredByCvNameAndTermName("sequence", "orthologous_to");

        for (FeatureRelationship featureRelationship : filtered) {
            Feature f = featureRelationship.getObjectFeature();
            if (f.getType().getName().equals("protein_match")) {
                clusterIds.add(f.getUniqueName());
            } else {
                if (f.getType().getName().equals("polypeptide")) {
                    orthologueNames.add(f.getUniqueName());
                }
            }
        }

    }


    private void populateFromFeatureDbXrefs(Polypeptide polypeptide) {
        List<DbXRefDTO> ret = new ArrayList<DbXRefDTO>();
        for(FeatureDbXRef fdx : polypeptide.getFeatureDbXRefs()) {
            ret.add(new DbXRefDTO(fdx.getDbXRef().getDb().getName(),
                    fdx.getDbXRef().getAccession(),
                    fdx.getDbXRef().getDb().getUrlPrefix()));
        }
        if (ret.size() > 0) {
            dbXRefDTOs = ret;
        }
    }


    private void populateFromFeaturePubs(Polypeptide polypeptide) {
        List<String> ret = new ArrayList<String>();
        for(FeaturePub fp : polypeptide.getFeaturePubs()) {
            String name = fp.getPub().getUniqueName();
            if (name.startsWith("PMID")) {
                ret.add(name);
            } else {
                logger.warn(String.format("Got a pub that isn't a PMID ('%s') for '%s'", name, polypeptide.getUniqueName()));
            }
        }
        if (ret.size() > 0) {
            publications = ret;
        }
    }

    private List<FeatureCvTermDTO> populateFromFeatureCvTerms(Polypeptide polypeptide, String cvNamePrefix) {
        Assert.notNull(polypeptide);

        Organism org = polypeptide.getOrganism();
        List<FeatureCvTermDTO> dtos = new ArrayList<FeatureCvTermDTO>();
        for (FeatureCvTerm featureCvTerm : polypeptide.getFeatureCvTermsFilteredByCvNameStartsWith(cvNamePrefix)) {
            FeatureCvTermDTO fctd = new FeatureCvTermDTO(featureCvTerm);
            fctd.setCount(sequenceDao.getFeatureCvTermCountInOrganism(featureCvTerm, org));
            dtos.add(fctd);
        }
        if (dtos.size() > 0) {
            return dtos;
        }
        return null;
    }


    private void populateMisc(Transcript transcript) {
        String type = transcript.getType().getName();
        typeDescription = type;
        if ("mRNA".equals(type)) {
            typeDescription = "Protein coding gene";
        } else {
            if ("pseudogenic_transcript".equals(type)) {
                typeDescription = "Pseudogene";
            }
        }

        this.location = transcript.getExonLocsTraditional();
    }



    private void populateParentDetails(AbstractGene gene) {
        FeatureLoc top = gene.getRankZeroFeatureLoc();
        this.min = top.getFmin();
        this.max = top.getFmax();

        Feature topLevelFeature = top.getSourceFeature();
        this.topLevelFeatureType = topLevelFeature.getType().getName();
        this.topLevelFeatureDisplayName = topLevelFeature.getDisplayName();
        this.topLevelFeatureUniqueName = topLevelFeature.getUniqueName();
        this.topLevelFeatureLength = topLevelFeature.getSeqLen();
    }



    private void populateFromFeatureProps(Polypeptide polypeptide) {
        Assert.notNull(polypeptide);
        this.notes = stringListFromFeaturePropList(polypeptide, "feature_property", "comment");
        this.comments = stringListFromFeaturePropList(polypeptide, "genedb_misc", "curation");
    }



    private List<String> stringListFromFeaturePropList(Polypeptide polypeptide, String cvName, String cvTermName) {
        List<String> ret = new ArrayList<String>();
        List<FeatureProp> featurePropNotes = polypeptide.getFeaturePropsFilteredByCvNameAndTermName(cvName, cvTermName);
        for (FeatureProp featureProp : featurePropNotes) {
            ret.add(featureProp.getValue());
        }
        logger.debug(String.format("Got '%d' results for filtering featureprops for '%s' in '%s'", ret.size(), cvTermName, cvName));
        if (ret.size() > 0) {
            return ret;
        }
        return Collections.emptyList();
    }



    private void populateNames(Transcript transcript, AbstractGene gene) {
        this.uniqueName = transcript.getUniqueName();
        this.geneName = gene.getDisplayName();
        if (transcript.getName() != null && !transcript.getName().equals(uniqueName)) {
            this.properName = transcript.getName();
        }
        Collection<FeatureSynonym> featureSynonyms = gene.getFeatureSynonyms();
        //Get the map of lists of synonyms
        synonymsByTypes = findFromSynonymsByType(featureSynonyms);
    }


    /**
     * Create lists of synonyms, where each list grouped by the synonym type
     * @param synonyms
     * @return
     */
    private Map<String, List<String>> findFromSynonymsByType(Collection<FeatureSynonym> synonymCollection) {
        HashMap<String, List<String>> synonymsByType = new HashMap<String, List<String>>();
        for (FeatureSynonym featSynonym : synonymCollection) {
            Synonym synonym = featSynonym.getSynonym();
            String typeName = formatSynonymTypeName(synonym.getType().getName());
            List<String> filtered = synonymsByType.get(typeName);
            if (filtered == null){
                filtered = new ArrayList<String>();
                synonymsByType.put(typeName, filtered);
            }
            filtered.add(synonym.getName());
        }

        if (synonymsByType.size() > 0 ) {
            return synonymsByType;
        }
        return null;
    }

    /**
     * Re-format the synonym type name
     * @param rawName
     * @return
     */
    private String formatSynonymTypeName(String rawName){

        char formattedName[] = rawName.toCharArray();
        for(int i=0; i<formattedName.length; ++i){

            //Replace underscores with spaces
            if (formattedName[i]=='_'){
                formattedName[i] = ' ';

            //Replace first char lowercase to a uppercase char
            }else if(i==0 && Character.isLowerCase(formattedName[i])){
                formattedName[i] = Character.toUpperCase(formattedName[i]);

            //Replace any occurrence of a lowercase char preceeded a space with a upper case char
            }else if(i>0 && formattedName[i-1]==' ' && Character.isLowerCase(formattedName[i])){
                formattedName[i] = Character.toUpperCase(formattedName[i]);
            }
        }
        return String.valueOf(formattedName).trim();
    }


    public String getUniqueName() {
        return uniqueName;
    }



    public String getProperName() {
        return properName;
    }



    public List<String> getSynonyms() {
        return listOrEmptyList(synonyms);
    }



    public List<String> getObsoleteNames() {
        return listOrEmptyList(obsoleteNames);
    }



    public List<FeatureCvTermDTO> getProducts() {
        return listOrEmptyList(products);
    }



    public List<String> getPublications() {
        return listOrEmptyList(publications);
    }


    private <T> List<T> listOrEmptyList(List<T> list) {
        if (list == null || list.size() == 0) {
            return Collections.emptyList();
        }
        return list;
    }



    public String getTypeDescription() {
        return typeDescription;
    }



    public String getTopLevelFeatureType() {
        return topLevelFeatureType;
    }



    public String getTopLevelFeatureDisplayName() {
        return topLevelFeatureDisplayName;
    }


    public String getTopLevelFeatureUniqueName() {
        return topLevelFeatureUniqueName;
    }

    public boolean isAnAlternateTranscript() {
        return anAlternateTranscript;
    }



    public String getGeneName() {
        return geneName;
    }



    public List<String> getNotes() {
        return notes;
    }



    public List<String> getComments() {
        return comments;
    }



    public boolean isPseudo() {
        return pseudo;
    }



    public String getLocation() {
        return location;
    }

    public List<DbXRefDTO> getDbXRefDTOs() {
        return listOrEmptyList(dbXRefDTOs);
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public List<FeatureCvTermDTO> getControlledCurations() {
        return listOrEmptyList(controlledCurations);
    }

    public List<FeatureCvTermDTO> getGoBiologicalProcesses() {
        return listOrEmptyList(goBiologicalProcesses);
    }

    public List<FeatureCvTermDTO> getGoMolecularFunctions() {
        return listOrEmptyList(goMolecularFunctions);
    }

    public List<FeatureCvTermDTO> getGoCellularComponents() {
        return listOrEmptyList(goCellularComponents);
    }

    public List<String> getClusterIds() {
        return listOrEmptyList(clusterIds);
    }

    public List<String> getOrthologueNames() {
        return listOrEmptyList(orthologueNames);
    }

    public List<PolypeptideRegionGroup> getDomainInformation() {
        return domainInformation;
    }

    /**
     * Get a summary of the protein map for this transcript
     *
     * @return the details for the protein map, or null if it shouldn't be shown
     */
    public ImageMapSummary getIms() {
        return ims;
    }

    public boolean isProteinCoding() {
        return proteinCoding;
    }

    public int getTopLevelFeatureLength() {
        return topLevelFeatureLength;
    }

    public long getLastModified() {
        return lastModified;
    }

    public Map<String, List<String>> getSynonymsByTypes() {
        return synonymsByTypes;
    }

    public void setSynonymsByTypes(Map<String, List<String>> synonymsByTypes) {
        this.synonymsByTypes = synonymsByTypes;
    }


}
