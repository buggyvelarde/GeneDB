package org.genedb.db.loading;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.CvTermDao;
import org.genedb.db.dao.DaoFactory;
import org.genedb.db.hibernate.Cv;
import org.genedb.db.hibernate.Cvterm;
import org.genedb.db.hibernate.Feature;
import org.genedb.db.hibernate.FeatureRelationship;
import org.genedb.db.hibernate.FeatureSynonym;
import org.genedb.db.hibernate.Featureloc;
import org.genedb.db.hibernate.Organism;
import org.genedb.db.hibernate.Pub;
import org.genedb.db.hibernate.Synonym;

import org.biojava.bio.seq.StrandedFeature;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FeatureUtils {
    
	private DaoFactory daoFactory;
	private Pub DUMMY_PUB;
	
    
	public Feature createFeature(String typeName, String uniqueName, Organism organism) {
	    //System.err.println("createFeature called");
	    CvTermDao cvtermDao = daoFactory.getCvTermDao();
            CvDao cvDao = daoFactory.getCvDao();
            Cv so = cvDao.findByName("sequence").get(0);
            Cvterm type = cvtermDao.findByNameInCv(typeName, so).get(0);
            //System.err.println("Got cvterm type:"+type);
            Date now = new Date();
            org.genedb.db.hibernate.Feature feature = new org.genedb.db.hibernate.Feature();
            feature.setOrganism(organism);
            feature.setCvterm(type);
            feature.setUniquename(uniqueName);
            feature.setIsAnalysis(false);
            feature.setIsObsolete(false);
            feature.setTimeaccessioned(now);
            feature.setTimelastmodified(now);
            //System.err.println("Returning "+feature);
            return feature;
	}

	public static String calcMD5(String in) {
	    byte[] residues = in.getBytes();
	    try {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
//	        MessageDigest tc1 = md.clone();
		byte[] md5Bytes = md5.digest(residues);

	        StringBuilder hexValue = new StringBuilder();
		for (int i=0 ; i<md5Bytes.length ; i++) {
		    int val = md5Bytes[i] & 0xff; 
		    if (val < 16) hexValue.append("0");
		    hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();
	        
	    }
	    catch (NoSuchAlgorithmException exp) {
		exp.printStackTrace(); // Shouldn't happen
	    }
	    return null;
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
	public Featureloc createLocation(org.genedb.db.hibernate.Feature parent, org.genedb.db.hibernate.Feature child, int min, int max, short strand) {
	    Featureloc fl = new Featureloc();
	    fl.setRank(0);
	    fl.setFeatureBySrcfeatureId(parent);
	    fl.setFeatureByFeatureId(child);
	    fl.setFmin(min);
	    fl.setFmax(max);
	    fl.setIsFminPartial(false);
	    fl.setIsFmaxPartial(false);
	    fl.setStrand(strand);
	    return fl;
	}

	public FeatureRelationship createRelationship(org.genedb.db.hibernate.Feature subject, org.genedb.db.hibernate.Feature object, Cvterm relType) {
	    FeatureRelationship fr = new FeatureRelationship();
	    fr.setCvterm(relType);
	    fr.setFeatureBySubjectId(subject);
	    fr.setFeatureByObjectId(object);
	    return fr;
	}


	
	@SuppressWarnings("unchecked")
	public void createSynonym(Cvterm type, String name, 
		org.genedb.db.hibernate.Feature gene, boolean isCurrent) {
	    Synonym synonym = null;
	    List<Synonym> matches = daoFactory.getHibernateTemplate().findByNamedParam(
		    "from Synonym s where s.name=:name and s.cvterm=:cvterm",
		    new String[] {"name", "cvterm"},
		    new Object[] {name, type});
	    if (matches.size()==0) {
		synonym = new Synonym();
		synonym.setCvterm(type);
		synonym.setName(name);
		synonym.setSynonymSgml(name);
		daoFactory.persist(synonym);
	    } else {
		synonym = matches.get(0);
	    }
	    
	    FeatureSynonym fs = null;
	    List<FeatureSynonym> matches2 = daoFactory.getHibernateTemplate().findByNamedParam(
		    "from FeatureSynonym fs where fs.feature=:feature and fs.synonym=:synonym",
		    new String[] {"feature", "synonym"},
		    new Object[] {gene, synonym});
	    if (matches2.size()==0) {
		fs = new FeatureSynonym();
		fs.setFeature(gene);
		fs.setIsCurrent(isCurrent);
		fs.setIsInternal(false);
		fs.setSynonym(synonym);
		fs.setPub(this.DUMMY_PUB);
		daoFactory.persist(fs);
	    } else {
		fs = matches2.get(0);
	    }
	    //daoFactory.persist(fs);
	    gene.getFeatureSynonyms().add(fs);
	}

	public void createSynonyms(Cvterm type, List<String> names, 
		org.genedb.db.hibernate.Feature feature, boolean isCurrent) {
	    
	    for (String name : names) {
		this.createSynonym(type, name, feature, isCurrent);
	    }
	}

	public void setDummyPub(Pub dummyPub) {
	    this.DUMMY_PUB = dummyPub;
	}

	public void setDaoFactory(DaoFactory daoFactory) {
	    this.daoFactory = daoFactory;
	}
	
}
