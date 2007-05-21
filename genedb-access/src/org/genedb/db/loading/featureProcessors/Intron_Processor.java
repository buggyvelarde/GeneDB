package org.genedb.db.loading.featureProcessors;

import static org.genedb.db.loading.EmblQualifiers.QUAL_NOTE;

import org.genedb.db.loading.ProcessingPhase;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureLoc;
import org.gmod.schema.sequence.FeatureProp;
import org.gmod.schema.sequence.FeatureRelationship;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Intron_Processor extends BaseFeatureProcessor {
	
	private static Map<String,Integer> intr = new HashMap<String,Integer>();
	
	public Intron_Processor() {
        super(new String[]{}, new String[]{}, new String[]{"note","citation"}, new String[]{},new String[]{});
    }
	
	@Override
	public void processStrandedFeature(Feature parent, StrandedFeature f, int offset) {
		// TODO Auto-generated method stub
		int j = 1;
		logger.debug("Entering processing for intron");
		Feature mrna;
		Collection<FeatureRelationship> featureRelations;
		Location loc = f.getLocation().translate(offset);
        Annotation an = f.getAnnotation();
        short strand = (short)f.getStrand().getValue();
        List<Feature> l;
        List<Feature> ex = new ArrayList<Feature>(0);
        int intronMin = loc.getMin()-1;
        int intronMax = loc.getMax();
        l = sequenceDao.getFeaturesByRange(intronMin,intronMax,
        						f.getStrand().getValue(), parent,"mRNA");
          if(l.size()>0) {
        	mrna = l.get(0);
        	
        	featureRelations = mrna.getFeatureRelationshipsForObjectId();
            for (FeatureRelationship featureRelation : featureRelations) {
            	Feature feature = featureRelation.getFeatureBySubjectId();   
            	logger.debug(feature.getUniqueName());
            	if(feature.getUniqueName().contains("exon")) {
            		ex.add(feature);
            	}
    		}
            if (! checkIntron(mrna, ex, intronMin, intronMax)) {
            	logger.info("intron does not fit properly in between exons for mRNA : " + l.get(0).getUniqueName());
            }
        	String systematicId = l.get(0).getUniqueName().replace("mRNA", "intron");
        	if(intr.containsKey(systematicId)) {
        		int i = intr.get(systematicId);
        		j = i + 1;
        		intr.put(systematicId, j);
        	} else {
        		intr.put(systematicId, j);
        	}
        	systematicId = systematicId + ":" + j;
        	
	        org.gmod.schema.sequence.Feature intron = this.featureUtils.createFeature("intron", systematicId, this.organism);
	        sequenceDao.persist(intron);
	        FeatureRelationship intronFr = featureUtils.createRelationship(intron,l.get(0), REL_PART_OF, 0); // FIXME Rank wrong
	        sequenceDao.persist(intronFr);
	        FeatureLoc intronFl = featureUtils.createLocation(parent,intron,loc.getMin()-1,loc.getMax(),
	                                                        strand);
	        sequenceDao.persist(intronFl);
	        //featureLocs.add(pepFl);
	        //featureRelationships.add(pepFr);
	        
	        //FeatureProp fp = createFeatureProp(intron, an, "colour", "colour", CV_GENEDB);
	        //sequenceDao.persist(fp);
	        createFeaturePropsFromNotes(intron, an, QUAL_NOTE, MISC_NOTE); 
        }
    }
    
//    protected String findName(Annotation an, String type) {
//        String[] keys = {QUAL_SYS_ID, "temporary_systematic_id", "gene"};
//        for (String key : keys) {
//            if (an.containsProperty(key)) {
//                return MiningUtils.getProperty(key, an, null);
//            }
//        }
//        throw new RuntimeException("No systematic id found for "+type+" entry");
//    }
    
    private boolean checkIntron(Feature mrna, List<Feature> ex, int intronMin, int intronMax) {
    	boolean right = false;
        boolean left = false;
        int mrnaMin = 0;
        int mrnaMax = 0;
        
        for (FeatureLoc fl : mrna.getFeatureLocsForFeatureId()) {
        	mrnaMin = fl.getFmin();
        	mrnaMax = fl.getFmax();
        }
    	
        if (mrnaMin == intronMin) {
        	left = true;
        }
        if (mrnaMax == intronMax) {
        	right = true;
        }
        if (! right) {
            for (Feature exon : ex) {
				for(FeatureLoc fl : exon.getFeatureLocsForFeatureId()) {
					int exonMin = fl.getFmin();
					
					if (exonMin - intronMax == 1) {
						right = true;
					}
				}
			}
        }
        if (! left) {
            for (Feature exon : ex) {
				for(FeatureLoc fl : exon.getFeatureLocsForFeatureId()) {
					int exonMax = fl.getFmax();
					
					if (exonMax - intronMin == -1) {
						left = true;
					}
				}
			}
        }
        if (left && right) {
        	return true;
        }
        return false;
    }

    @Override
    public ProcessingPhase getProcessingPhase() {
        return ProcessingPhase.THIRD;
    }
}