package org.genedb.db.loading.featureProcessors;

import static org.genedb.db.loading.EmblQualifiers.QUAL_NOTE;
import static org.genedb.db.loading.EmblQualifiers.QUAL_SYS_ID;

import org.genedb.db.hibernate3gen.FeatureLoc;
import org.genedb.db.hibernate3gen.FeatureProp;
import org.genedb.db.hibernate3gen.FeatureRelationship;
import org.genedb.db.jpa.Feature;
import org.genedb.db.loading.MiningUtils;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Intron_Processor extends BaseFeatureProcessor {
	
	private static Map<String,Integer> intr = new HashMap<String,Integer>();
	
	public Intron_Processor() {
        super(new String[]{}, new String[]{}, new String[]{"note","citation"}, new String[]{},new String[]{});
    }
	
	@Override
	public void processStrandedFeature(Feature parent, StrandedFeature f) {
		// TODO Auto-generated method stub
		int j = 1;
		logger.info("Entering processing for intron");
		org.genedb.db.jpa.Feature mrna;
		Set<FeatureRelationship> featureRelations;
		Location loc = f.getLocation();
        Annotation an = f.getAnnotation();
        short strand = (short)f.getStrand().getValue();
        List<org.genedb.db.jpa.Feature> l;
        List<org.genedb.db.jpa.Feature> ex = new ArrayList<org.genedb.db.jpa.Feature>();
        int intronMin = loc.getMin()-1;
        int intronMax = loc.getMax();
        l = this.daoFactory.getFeatureDao().findByRange(intronMin,intronMax,
        						f.getStrand().getValue(),
        						parent.getFeatureId(),"mRNA");
          if(l.size()>0) {
        	mrna = l.get(0);
        	
        	featureRelations = mrna.getFeatureRelationshipsForObjectId();
            for (FeatureRelationship featureRelation : featureRelations) {
            	Feature feature = featureRelation.getFeatureBySubjectId();   
            	logger.info(feature.getUniquename());
            	if(feature.getUniquename().contains("exon")) {
            		ex.add(feature);
            	}
    		}
            if (! checkIntron(mrna, ex, intronMin, intronMax)) {
            	logger.info("intron does not fit properly in between exons for mRNA : " + l.get(0).getUniquename());
            }
        	String systematicId = l.get(0).getUniquename().replace("mRNA", "intron");
        	if(intr.containsKey(systematicId)) {
        		Integer i = intr.get(systematicId);
        		j = i.intValue() + 1;
        		intr.remove(systematicId);
        		intr.put(systematicId, new Integer(j));
        	} else {
        		intr.put(systematicId, new Integer(j));
        	}
        	intr.put(systematicId, new Integer(0));
        	systematicId = systematicId + ":" + j;
        	
	        org.genedb.db.jpa.Feature intron = this.featureUtils.createFeature("intron", systematicId, this.organism);
	        this.daoFactory.persist(intron);
	        FeatureRelationship intronFr = featureUtils.createRelationship(intron,l.get(0), REL_PART_OF);
	        this.daoFactory.persist(intronFr);
	        FeatureLoc intronFl = featureUtils.createLocation(parent,intron,loc.getMin()-1,loc.getMax(),
	                                                        strand);
	        this.daoFactory.persist(intronFl);
	        //featureLocs.add(pepFl);
	        //featureRelationships.add(pepFr);
	        
	        FeatureProp fp = createFeatureProp(intron, an, "colour", "colour", CV_MISC);
	        this.daoFactory.persist(fp);
	        createFeaturePropsFromNotes(intron, an, QUAL_NOTE, MISC_NOTE); 
        }
    }
    
    protected String findName(Annotation an, String type) {
        String[] keys = {QUAL_SYS_ID, "temporary_systematic_id", "gene"};
        for (String key : keys) {
            if (an.containsProperty(key)) {
                return MiningUtils.getProperty(key, an, null);
            }
        }
        throw new RuntimeException("No systematic id found for "+type+" entry");
    }
    
    private boolean checkIntron(Feature mrna,List<Feature> ex,int intronMin,int intronMax) {
    	boolean right = false;
        boolean left = false;
        int mrnaMin = 0;
        int mrnaMax = 0;
        
        for (FeatureLoc fl : mrna.getFeaturelocsForFeatureId()) {
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
				for(FeatureLoc fl : exon.getFeaturelocsForFeatureId()) {
					int exonMin = fl.getFmin();
					
					if (exonMin - intronMax == 1) {
						right = true;
					}
				}
			}
        }
        if (! left) {
            for (Feature exon : ex) {
				for(FeatureLoc fl : exon.getFeaturelocsForFeatureId()) {
					int exonMax = fl.getFmax();
					
					if (exonMax - intronMin == -1) {
						left = true;
					}
				}
			}
        }
        if (left && right) {
        	return true;
        } else {
        	return false;
        }
    }
}