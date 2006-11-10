package org.genedb.db.loading;


import static org.genedb.db.loading.EmblQualifiers.QUAL_TOP_LEVEL;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.cv.Cv;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.organism.Organism;
import org.gmod.schema.pub.Pub;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureLoc;
import org.gmod.schema.sequence.FeatureProp;
import org.gmod.schema.sequence.FeatureRelationship;
import org.gmod.schema.sequence.FeatureSynonym;
import org.gmod.schema.sequence.Synonym;

import org.biojava.bio.seq.StrandedFeature;
import org.springframework.beans.factory.InitializingBean;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FeatureUtils implements InitializingBean {
    
	private CvDao cvDao;
	private Pub DUMMY_PUB;
    private SequenceDao sequenceDao;
    private Cv so;
    protected CvTerm GENEDB_TOP_LEVEL;
	
    
    
    
	public Feature createFeature(String typeName, String uniqueName, Organism organism) {
        List<CvTerm> cvTerms = cvDao.getCvTermByNameInCv(typeName, so);
        if (cvTerms.size()==0) {
            System.err.println("Unable to find name '"+typeName+"' in ontology '"+so.getName()+"'");
            throw new ExpectedLookupMissing("Unable to find name '"+typeName+"' in ontology '"+so.getName()+"'");
        }
        CvTerm type = cvTerms.get(0);
            //System.err.println("Got cvterm type:"+type);
            Date now = new Date();
            Timestamp ts = new Timestamp(now.getTime());
            Feature feature = new Feature(organism, type, uniqueName, false, false, ts, ts);
            return feature;
	}

	
	public static void dumpFeature(org.biojava.bio.seq.Feature f, String msg) {
	    System.err.print("--- ");
	    if(msg != null) {
	        System.err.print(msg);
	    }
	    System.err.println();
	    System.err.println("Type="+f.getType());
	    System.err.print("Location="+f.getLocation().getMin()+".."+f.getLocation().getMax()+"  ");
	    if (f instanceof StrandedFeature) {
	        System.err.print(((StrandedFeature)f).getStrand().getToken());
	    }
	    System.err.println();
	    // Annotation
	    Map map = f.getAnnotation().asMap();
	    Iterator it = map.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry entry = (Map.Entry) it.next();
	        System.err.println("   "+entry.getKey()+"="+entry.getValue());
	    }
	}
	
	/**
     * Create a simple FeatureLocation object, tying an object to one parent, with rank 0, no fuzzy ends
     * 
	 * @param parent The feature this is located to
	 * @param child The feature to locate
	 * @param min The minimum position on the parent
	 * @param max The maximum position on the parent
	 * @param strand The strand-edness of the feature relative to the parent
	 * @return the newly constructed FeatureLocation, not persisted
	 */
	public FeatureLoc createLocation(Feature parent, Feature child, int min, int max, short strand) {
	    return new FeatureLoc(parent, child, min, false, max, false, strand, 0, 0, 0);
	}

	public FeatureRelationship createRelationship(Feature subject, Feature object, CvTerm relType, int rank) {
	    FeatureRelationship fr = new FeatureRelationship(subject, object, relType, rank);
        
        object.getFeatureRelationshipsForObjectId().add(fr);
        subject.getFeatureRelationshipsForSubjectId().add(fr);
        
	    return fr;
	}



	@SuppressWarnings("unchecked")
	public void createSynonym(CvTerm type, String name, 
	        Feature gene, boolean isCurrent) {
	    Synonym synonym = null;
	    Synonym match = sequenceDao.getSynonymByNameAndCvTerm(name, type);
	    if (match == null) {
	        synonym = new Synonym(type, name, name);
	        sequenceDao.persist(synonym);
	    } else {
	        synonym = match;
	    }

	    FeatureSynonym fs = null;
	    List<FeatureSynonym> matches2 = sequenceDao.getFeatureSynonymsByFeatureAndSynonym(gene, synonym);
	    if (matches2.size()==0) {
	        fs = new FeatureSynonym(synonym, gene, this.DUMMY_PUB, isCurrent, false);
	        sequenceDao.persist(fs);
	    } else {
	        fs = matches2.get(0);
	    }
	    //daoFactory.persist(fs);
	    gene.getFeatureSynonyms().add(fs);
	}

	public void createSynonyms(CvTerm type, List<String> names, 
		Feature feature, boolean isCurrent) {
	    
	    for (String name : names) {
	        this.createSynonym(type, name, feature, isCurrent);
	    }
	}

	public void setDummyPub(Pub dummyPub) {
	    this.DUMMY_PUB = dummyPub;
	}

    public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
    
        this.sequenceDao = sequenceDao;
    }

    public void afterPropertiesSet() {
        so = cvDao.getCvByName("sequence").get(0);
        Cv CV_GENEDB = cvDao.getCvByName("genedb_misc").get(0);
        GENEDB_TOP_LEVEL = cvDao.getCvTermByNameInCv(QUAL_TOP_LEVEL, CV_GENEDB).get(0);
    }
	
    public void markTopLevelFeature(org.gmod.schema.sequence.Feature topLevel) {
        FeatureProp fp = new FeatureProp();
        fp.setCvTerm(GENEDB_TOP_LEVEL);
        fp.setValue("true");
    }
    
}
