package org.genedb.db.loading;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.CvTermDao;
import org.genedb.db.dao.DaoFactory;
import org.genedb.db.hibernate3gen.Cv;
import org.genedb.db.hibernate3gen.CvTerm;
import org.genedb.db.hibernate3gen.FeatureLoc;
import org.genedb.db.hibernate3gen.FeatureRelationship;
import org.genedb.db.hibernate3gen.FeatureSynonym;
import org.genedb.db.hibernate3gen.Organism;
import org.genedb.db.hibernate3gen.Pub;
import org.genedb.db.hibernate3gen.Synonym;
import org.genedb.db.jpa.Feature;

import org.biojava.bio.seq.StrandedFeature;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FeatureUtils {
    
	private DaoFactory daoFactory;
	private Pub DUMMY_PUB;
	
    
	public Feature createFeature(String typeName, String uniqueName, Organism organism) {
	    //System.err.println("createFeature called");
	    CvTermDao cvtermDao = daoFactory.getCvTermDao();
            CvDao cvDao = daoFactory.getCvDao();
            Cv so = cvDao.findByName("sequence").get(0);
            CvTerm type = cvtermDao.findByNameInCv(typeName, so).get(0);
            //System.err.println("Got cvterm type:"+type);
            Date now = new Date();
            org.genedb.db.jpa.Feature feature = new org.genedb.db.jpa.Feature();
            feature.setOrganism(organism);
            feature.setCvTerm(type);
            feature.setUniquename(uniqueName);
            feature.setAnalysis(false);
            feature.setObsolete(false);
            feature.setTimeAccessioned(now);
            feature.setTimeLastModified(now);
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
	public FeatureLoc createLocation(org.genedb.db.jpa.Feature parent, org.genedb.db.jpa.Feature child, int min, int max, short strand) {
	    FeatureLoc fl = new FeatureLoc();
	    fl.setRank(0);
	    fl.setFeatureBySrcfeatureId(parent);
	    fl.setFeatureByFeatureId(child);
	    fl.setFmin(min);
	    fl.setFmax(max);
	    fl.setFminPartial(false);
	    fl.setFmaxPartial(false);
	    fl.setStrand(strand);
	    return fl;
	}

	public FeatureRelationship createRelationship(org.genedb.db.jpa.Feature subject, org.genedb.db.jpa.Feature object, CvTerm relType) {
	    FeatureRelationship fr = new FeatureRelationship();
	    fr.setCvterm(relType);
	    fr.setFeatureBySubjectId(subject);
	    fr.setFeatureByObjectId(object);
	    return fr;
	}


	
	@SuppressWarnings("unchecked")
	public void createSynonym(CvTerm type, String name, 
		org.genedb.db.jpa.Feature gene, boolean isCurrent) {
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
		fs.setCurrent(isCurrent);
		fs.setInternal(false);
		fs.setSynonym(synonym);
		fs.setPub(this.DUMMY_PUB);
		daoFactory.persist(fs);
	    } else {
		fs = matches2.get(0);
	    }
	    //daoFactory.persist(fs);
	    gene.getFeatureSynonyms().add(fs);
	}

	public void createSynonyms(CvTerm type, List<String> names, 
		org.genedb.db.jpa.Feature feature, boolean isCurrent) {
	    
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
