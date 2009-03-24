package org.genedb.web.mvc.model;

import java.util.ArrayList;
import java.util.List;

import org.genedb.db.domain.services.MockBasicGeneService;
import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Chromosome;
import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureCvTerm;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.FeatureRelationship;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.mapped.TestChromosome;
import org.gmod.schema.mapped.TestTranscript;

import com.sleepycat.collections.StoredMap;

public class CacheSynchTestDelegate extends CacheSynchroniser {
    
    @Override
    protected List<Feature> findTopLevelFeatures(List<String> uniqueNames){
        List<Feature> features = new ArrayList<Feature>();
        for(String uniqueName : uniqueNames){
            Feature feature = createTopLevelFeature(uniqueName);
            feature.setSeqLen(6000);
            features.add(feature);
        }
        return features;
    }
    
    @Override
    protected List<Transcript> findTranscripts(List<String> uniqueNames){
        List<Transcript> transcripts = new ArrayList<Transcript>();
        for(String uniqueName : uniqueNames){
            Transcript transcript = new TestTranscript(
                    createOrganism(), uniqueName, true, false, null);
            transcript.setSeqLen(6000);
            
            AbstractGene gene = createAbstractGene(uniqueName);
            transcript.addFeatureRelationshipsForSubjectId(
                    new FeatureRelationship(null, gene, null, 0));
            transcript.setGene(gene);
            transcript.addCvTerm(createCvTerm());
            transcripts.add(transcript);
        }
        return transcripts;
    }
    
    private AbstractGene createAbstractGene(String uniqueName){
        Gene gene = new Gene(createOrganism(), uniqueName, true, false, null);
        FeatureLoc featureLoc = new FeatureLoc(
                createTopLevelFeature(uniqueName), null, 4, true, 7, true, null, null, 0, 0); 
        ((Feature)gene).addFeatureLoc(featureLoc);
       return gene;
    }
    

    @Override
    protected void init(){
        dtoMap = bmf.getDtoMap(); // TODO More nicely
        contextMapMap = bmf.getContextMapMap();
        basicGeneService = new MockBasicGeneService();
    }
    
    private Organism createOrganism(){
        Organism organism = new Organism("_genus", "_species", "_commonName", "_abbreviation", "_comment");
        return organism;
    }
    
    private Chromosome createTopLevelFeature(String uniqueName){
        Chromosome chr = new TestChromosome(createOrganism(), uniqueName, true, false, null);
        ((Feature)chr).addCvTerm(createCvTerm());
        ((Feature)chr).addFeatureCvTerm(createFeatureCvTerm(chr));
        return chr;
    }
    
    private CvTerm createCvTerm(){
        CvTerm cvterm = new CvTerm(null, null, "_cvterm", "gergeww");
        return cvterm;
    }
    
    private FeatureCvTerm createFeatureCvTerm(Feature feature){
        FeatureCvTerm fct = new FeatureCvTerm(createCvTerm(),feature, null, false, 10);
        return fct;
    }
    
    public StoredMap<String, String> getContextMapMap(){
        return contextMapMap;
    }
    
    public StoredMap<String, TranscriptDTO> getDtoMap(){
        return dtoMap;
    }
}
