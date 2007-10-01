package org.genedb.db.loading;


import static org.genedb.db.loading.EmblQualifiers.QUAL_TOP_LEVEL;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.biojava.bio.BioException;
import org.biojava.bio.proteomics.IsoelectricPointCalc;
import org.biojava.bio.proteomics.MassCalc;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.SymbolPropertyTable;
import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.GeneralDao;
import org.genedb.db.dao.PubDao;
import org.genedb.db.dao.SequenceDao;
import org.gmod.schema.cv.Cv;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.general.Db;
import org.gmod.schema.general.DbXRef;
import org.gmod.schema.organism.Organism;
import org.gmod.schema.pub.Pub;
import org.gmod.schema.pub.PubDbXRef;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureLoc;
import org.gmod.schema.sequence.FeatureProp;
import org.gmod.schema.sequence.FeatureRelationship;
import org.gmod.schema.sequence.FeatureSynonym;
import org.gmod.schema.sequence.Synonym;
import org.gmod.schema.utils.PeptideProperties;
import org.springframework.beans.factory.InitializingBean;

public class FeatureUtils implements InitializingBean {
    
	private CvDao cvDao;
	private PubDao pubDao;
    private SequenceDao sequenceDao;
    private GeneralDao generalDao;
    private Cv so;
    protected CvTerm GENEDB_TOP_LEVEL;
	private Pub DUMMY_PUB;

	protected Pattern PUBMED_PATTERN;
    
    
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
	    return new FeatureLoc(parent, child, min, false, max, false, strand, null, 0, 0);
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

	public void setPubDao(PubDao pubDao) {
	    this.pubDao = pubDao;
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
        DUMMY_PUB = pubDao.getPubByUniqueName("null");
        PUBMED_PATTERN = Pattern.compile("PMID:|PUBMED:", Pattern.CASE_INSENSITIVE);
    }
	
    public void markTopLevelFeature(org.gmod.schema.sequence.Feature topLevel) {
        FeatureProp fp = new FeatureProp();
        fp.setCvTerm(GENEDB_TOP_LEVEL);
        fp.setValue("true");
    }


	public void setDummyPub(Pub dummyPub) {
		DUMMY_PUB = dummyPub;
	}
    
    private static FeatureLoc getZeroRankFeatureLoc(Feature f) {
        Collection <FeatureLoc> collection = f.getFeatureLocsForSrcFeatureId();
        for (FeatureLoc loc : collection) {
            if (loc.getRank() == 0) {
                return loc;
            }
        }
        return null;
    }
    
    public static String getResidues(Feature feature){
    	String residues = null;
    	residues = new String(feature.getResidues());
    	
    	if (!residues.contains("A")){
            // TODO Check what this is intended to do
    		if("gene".equals(feature.getCvTerm().getName())){
    			Collection<FeatureLoc> fl = feature.getFeatureLocsForFeatureId();
    			for (FeatureLoc loc : fl) {
					Feature toplevel = loc.getFeatureBySrcFeatureId();
					String temp = new String(toplevel.getResidues());
					residues = temp.substring(loc.getFmin(), loc.getFmax());
					return residues;
				}
    		} else if ("mrna".equals(feature.getCvTerm().getName())){
    			Collection<FeatureRelationship> fr = feature.getFeatureRelationshipsForSubjectId();
    			for (FeatureRelationship relationship : fr) {
					Feature gene = relationship.getFeatureByObjectId();
                    FeatureLoc parentLoc = getZeroRankFeatureLoc(gene);
					Feature toplevel = parentLoc.getFeatureBySrcFeatureId();
					String temp = new String(toplevel.getResidues());
					residues = temp.substring(parentLoc.getFmin(),parentLoc.getFmax());
					return residues;
				}
    		} else {
    			Collection<FeatureRelationship> fr = feature.getFeatureRelationshipsForSubjectId();
    			Feature mrna = null;
    			for (FeatureRelationship relationship : fr) {
    				mrna = relationship.getFeatureByObjectId();
    				System.out.println("mrna name is : " + mrna.getUniqueName());
    				break;
    			}
    			//Feature gene = null;
    			Collection<FeatureRelationship> fr2 = mrna.getFeatureRelationshipsForSubjectId();
    			for (FeatureRelationship relationship : fr2) {
					Feature gene = relationship.getFeatureByObjectId();
					System.out.println("gene name is : " + gene.getUniqueName());
					Feature toplevel = gene.getFeatureLocsForFeatureId().iterator().next().getFeatureBySrcFeatureId();
					String temp = new String(toplevel.getResidues());
					//System.out.println("Residues " + temp);
					FeatureLoc fl = mrna.getFeatureLocsForFeatureId().iterator().next();
					System.out.println(fl.getFmin() + " " + fl.getFmax());
					residues = temp.substring(fl.getFmin(),fl.getFmax());
					SeqTrans st = new SeqTrans();
					residues = st.translate(residues.getBytes(), 1,1);
					if (residues.contains("*")){
						String toSend = new String();
						toSend = residues;
						StringTokenizer token = new StringTokenizer(toSend,"*");
						residues = new String();
						while(token.hasMoreTokens()){
							residues = residues + token.nextToken();
						}
						//System.out.println("residues are : " + residues);
					}
					return residues;
				}
    		}
    		
    	}
    	return residues;
    }
    
    /**
     * Create, or lookup a Pub object from a PMID:acc style input, although the 
     * prefix is ignored
     * 
     * @param ref the reference
     * @return the Pub object
     */
    public Pub findOrCreatePubFromPMID(String ref) {
        Db DB_PUBMED = generalDao.getDbByName("MEDLINE");
        int colon = ref.indexOf(":");
        String accession = ref;
        if (colon != -1) {
            accession = ref.substring(colon+1);
        }
        DbXRef dbXRef = generalDao.getDbXRefByDbAndAcc(DB_PUBMED, accession);
        Pub pub;
        if (dbXRef == null) {
            dbXRef = new DbXRef(DB_PUBMED, accession);
            generalDao.persist(dbXRef);
            CvTerm cvTerm = cvDao.getCvTermById(1); //TODO -Hack
            pub = new Pub("PMID:"+accession, cvTerm);
            generalDao.persist(pub);
            PubDbXRef pubDbXRef = new PubDbXRef(pub, dbXRef, true);
            generalDao.persist(pubDbXRef);
        } else {
            pub = pubDao.getPubByDbXRef(dbXRef);
        }
        return pub;
    }
    
    /**
     * Take a pipe-seperated string and split them up,  
     * then lookup or create them 
     * 
     * @param xref A list of pipe seperated dbxrefs strings
     * @return A list of DbXrefs
     */
    public List<DbXRef> findOrCreateDbXRefsFromString(String xref) {
        List<DbXRef> ret = new ArrayList<DbXRef>();
        StringTokenizer st = new StringTokenizer(xref, "|");
        while (st.hasMoreTokens()) {
            ret.add(findOrCreateDbXRefFromString(st.nextToken()));
        }
        return ret;
    }
    
    /**
     * Take a db reference and look it up, or create it if it doesn't exist
     * 
     * @param xref the reference ie db:id
     * @return the created or looked-up DbXref
     */
    public DbXRef findOrCreateDbXRefFromString(String xref) {
        int index = xref.indexOf(':');
        if (index == -1) {
            return null;
        }
        String dbName = xref.substring(0, index);
        String accession = xref.substring(index+1);
        Db db = generalDao.getDbByName(dbName);
        if (db == null) {
            return null;
        }
        DbXRef dbXRef = generalDao.getDbXRefByDbAndAcc(db, accession);
        if (dbXRef == null) {
            dbXRef = new DbXRef(db, accession);
            sequenceDao.persist(dbXRef);
        }
        return dbXRef;
    }
    
    public void findPubOrDbXRefFromString(String xrefString, List<Pub> pubs, List<DbXRef> dbXRefs) {
        boolean makePubs = (pubs != null) ? true : false;
        String[] xrefs = xrefString.split("\\|");
        for (String xref : xrefs) {
            if (makePubs && looksLikePub(xref)) {
                pubs.add(findOrCreatePubFromPMID(xref));
            } else {
                DbXRef dbXRef = findOrCreateDbXRefFromString(xref);
                if (dbXRef != null) {
                    dbXRefs.add(dbXRef);
                }
            }
        }
    }
    
    /**
	 * Does a string look likes it's a PubMed reference
	 * 
	 * @param xref The string to examine
	 * @return true if it looks like a PubMed reference
	 */
	public boolean looksLikePub(String xref) {
		boolean ret =  PUBMED_PATTERN.matcher(xref).lookingAt();
		return ret;
	}
	

    public PeptideProperties calculatePepstats(Feature polypeptide) {

        String seqString = FeatureUtils.getResidues(polypeptide);
        Alphabet protein = ProteinTools.getAlphabet();
        SymbolTokenization proteinToke = null;
        SymbolList seq = null;
        PeptideProperties pp = new PeptideProperties();
        try {
            proteinToke = protein.getTokenization("token");
            seq = new SimpleSymbolList(proteinToke, seqString);
        } catch (BioException e) {

        }
        IsoelectricPointCalc ipc = new IsoelectricPointCalc();
        Double cal = 0.0;
        try {
            cal = ipc.getPI(seq, false, false);
        } catch (IllegalAlphabetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BioException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        DecimalFormat df = new DecimalFormat("#.##");
        pp.setIsoelectricPoint(df.format(cal));

        CvTerm MISC_ISOELECTRIC = cvDao.getCvTermByNameAndCvName("isoelectric_point", "genedb_misc"); 
        CvTerm MISC_MASS = cvDao.getCvTermByNameAndCvName("molecular mass", "genedb_misc"); 
        CvTerm MISC_CHARGE = cvDao.getCvTermByNameAndCvName("protein_charge", "genedb_misc"); 


        FeatureProp fp = new FeatureProp(polypeptide, MISC_ISOELECTRIC, df.format(cal), 0); 

        pp.setAminoAcids(Integer.toString(seqString.length()));
        MassCalc mc = new MassCalc(SymbolPropertyTable.AVG_MASS,false);
        try {
            cal = mc.getMass(seq)/1000;
        } catch (IllegalSymbolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        pp.setMass(df.format(cal));

        fp = new FeatureProp(polypeptide, MISC_MASS, df.format(cal), 0);

        cal = ProteinUtils.getCharge(seq);
        pp.setCharge(df.format(cal));


        fp = new FeatureProp(polypeptide, MISC_CHARGE, df.format(ProteinUtils.getCharge(seq)), 0);

        return pp;
    }
}
