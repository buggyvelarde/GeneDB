package org.genedb.db.dao;


import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.dao.SequenceDaoI;
import org.gmod.schema.general.DbXRef;
import org.gmod.schema.organism.Organism;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureCvTerm;
import org.gmod.schema.sequence.FeatureCvTermDbXRef;
import org.gmod.schema.sequence.FeatureCvTermPub;
import org.gmod.schema.sequence.FeatureDbXRef;
import org.gmod.schema.sequence.FeatureSynonym;
import org.gmod.schema.sequence.Synonym;
import org.gmod.schema.utils.CountedName;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.ArrayList;
import java.util.List;

public class SequenceDao extends BaseDao implements SequenceDaoI {
    
 
    /* (non-Javadoc)
     * @see org.genedb.db.dao.SequenceDaoI#getFeatureById(int)
     */
    public Feature getFeatureById(int id) {
        return (Feature) getHibernateTemplate().load(Feature.class, id);
    }

    /* (non-Javadoc)
     * @see org.genedb.db.dao.SequenceDaoI#getFeatureByUniqueName(java.lang.String)
     */
    public Feature getFeatureByUniqueName(String name, String featureType) {
    	@SuppressWarnings("unchecked")
    	List<Feature> features = (List<Feature>) getHibernateTemplate().findByNamedParam(
                "from Feature f where f.uniqueName=:name and f.cvTerm.name=:featureType",
                new String[]{"name","featureType"},new Object[]{name,featureType});
        if (features.size() > 0) {
            return features.get(0);
        }
        return null;
    }
    
    
    @SuppressWarnings("unchecked")
    public List<Feature> getFeaturesByUniqueName(String name) {
        List features = getHibernateTemplate().findByNamedParam(
                "from Feature f where f.uniqueName like :name", "name", name);
        return features;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.dao.SequenceDaoI#getFeatureByAnyCurrentName(java.lang.String)
     */
    @SuppressWarnings({ "unchecked", "cast" })
    public List<Feature> getFeaturesByAnyCurrentName(String name) {
        List<Feature> features = (List<Feature>) getHibernateTemplate().findByNamedParam(
                "select f from Feature f, FeatureSynonym fs, Synonym s where f=fs.feature and fs.synonym=s and fs.current=true and s.name=:name",
                "name", name);
        return features;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.dao.SequenceDaoI#getFeatureByRange(int, int, int, org.genedb.db.jpa.Feature, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public List<Feature> getFeaturesByRange(int min,int max,int strand,Feature feat,String type) {
        List<Feature> features;
        int fid = feat.getFeatureId();
        //int min = loc.getMin();
        //int max = loc.getMax();
        //String name = "mRNA";
        features = getHibernateTemplate().findByNamedParam("select f " +
                "from Feature f, FeatureLoc loc, CvTerm cvt where " +
                "f.featureId=loc.featureByFeatureId and f.cvTerm=cvt.cvTermId and cvt.name=:type and loc.strand="+strand+" and" +
                " loc.featureBySrcFeatureId="+fid+" and (" +
                " loc.fmin<=:min and loc.fmax>=:max)",
                new String[]{"type", "min", "max"},
                new Object[]{type, min, max});
        return features;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.dao.SequenceDaoI#getFeatureByAnyName(org.genedb.db.helpers.NameLookup, java.lang.String)
     */
    @SuppressWarnings({ "unchecked", "cast" })
    public List<Feature> getFeaturesByAnyName(String name, String featureType) {

        // TODO Taxon and filter
        String lookup = name.replaceAll("\\*", "%");

        logger.info("lookup is " + lookup);
        List<Feature> features = (List<Feature>)
        getHibernateTemplate().findByNamedParam("select f from Feature f, FeatureSynonym fs, Synonym s, CvTerm cvt where f=fs.feature and fs.synonym=s and fs.current=true and f.cvTerm=cvt.cvTermId and cvt.name=:featureType and s.name like :lookup",
                new String[]{"lookup", "featureType"}, new Object[] {lookup, featureType});
        return features;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.dao.SequenceDaoI#getFeatureCvTermByFeatureAndCvTerm(org.genedb.db.jpa.Feature, org.genedb.db.hibernate3gen.CvTerm, boolean)
     */
    @SuppressWarnings("unchecked")
    public List<FeatureCvTerm> getFeatureCvTermsByFeatureAndCvTermAndNot(Feature feature, CvTerm cvTerm, boolean not) {
        List<FeatureCvTerm> list = getHibernateTemplate().findByNamedParam("from FeatureCvTerm fct where fct.feature=:feature and fct.cvTerm=:cvTerm and fct.not=:not", 
                new String[]{"feature", "cvTerm", "not"}, 
                new Object[]{feature, cvTerm, not});

        return list;
    }
    
    
    @SuppressWarnings("unchecked")
	public List<Feature> getFeaturesByCvNameAndCvTermNameAndOrganisms(String cvName, 
    		String cvTermName, String orgs) {
    	logger.info("Querying with cvName='"+cvName+"' cvTermName='"+cvTermName+"' orgs='"+orgs+"'");
        return getHibernateTemplate().findByNamedParam("select f" +
                " from CvTerm cvt,FeatureCvTerm fct,Feature f " +
        "where f.organism.commonName in ("+orgs+") and f=fct.feature and cvt=fct.cvTerm and cvt.cv.name=:cvName and cvt.name=:cvTermName",
        new String[]{"cvName", "cvTermName"}, new Object[]{cvName, cvTermName});
    }

    /* (non-Javadoc)
     * @see org.genedb.db.dao.SequenceDaoI#getSynonymsByNameAndCvTerm(java.lang.String, org.genedb.db.hibernate3gen.CvTerm)
     */
    @SuppressWarnings("unchecked")
    public Synonym getSynonymByNameAndCvTerm(String name, CvTerm type) {
        List<Synonym> tmp = getHibernateTemplate().findByNamedParam(
                "from Synonym s where s.name=:name and s.cvTerm=:cvterm",
                new String[] {"name", "cvterm"},
                new Object[] {name, type});

        return firstFromList(tmp, "name", name, "cvterm", type.getName());
    }


    /* (non-Javadoc)
     * @see org.genedb.db.dao.SequenceDaoI#getFeatureSynonymsByFeatureAndSynonym(org.genedb.db.jpa.Feature, org.genedb.db.hibernate3gen.Synonym)
     */
    @SuppressWarnings("unchecked")
    public List<FeatureSynonym> getFeatureSynonymsByFeatureAndSynonym(Feature feature, Synonym synonym) {
        return getHibernateTemplate().findByNamedParam(
                "from FeatureSynonym fs where fs.feature=:feature and fs.synonym=:synonym",
                new String[] {"feature", "synonym"},
                new Object[] {feature, synonym});
    }

    @SuppressWarnings("unchecked")
    public List<Feature> getFeaturesByLocatedOnFeature(Feature parent) {
        List<Feature> features;
        //int fid = parent.getFeatureId();
        features = getHibernateTemplate().findByNamedParam("select f " +
                "from Feature f, FeatureLoc loc, CvTerm cvt where " +
                "f.featureId=loc.featureByFeatureId and" +
                " loc.featureBySrcFeatureId=:parent",
                new String[]{"parent"},
                new Object[]{parent});
        return features;
    }

    @SuppressWarnings("unchecked")
    public List<FeatureDbXRef> getFeatureDbXRefsByFeatureUniquename(String uniqueName) {
        if (uniqueName == null) {
            return getHibernateTemplate().find("select from FeatureDbXRef");
        }
        return getHibernateTemplate().findByNamedParam(
                "from FeatureDbXRef fdxr where fdxr.feature.uniqueName=:uniqueName",
                new String[] {"uniqueName"},
                new Object[] {uniqueName});
    }

    
    
    @SuppressWarnings("unchecked")
    public List<FeatureSynonym> getFeatureSynonymsByFeatureUniquename(String uniqueName) {
        if (uniqueName == null) {
            return getHibernateTemplate().find("select from FeatureSynonym");
        }
        return getHibernateTemplate().findByNamedParam(
                "from FeatureSynonym fs where fs.feature.uniqueName=:uniqueName",
                new String[] {"uniqueName"},
                new Object[] {uniqueName});
    }

    @SuppressWarnings("unchecked")
    public List<List> getFeatureByGO(String go) {
    	String temp[] = go.split(":");
    	String number = temp[1];
    	List <Feature> polypeptides;
    	List <CvTerm> goName;
    	List <Feature> features = new ArrayList<Feature>();
    	polypeptides = getHibernateTemplate().findByNamedParam("select f " +
	    			"from Feature f, DbXRef d, CvTerm c, FeatureCvTerm fc where " +
	    			"d.accession=:number and d.dbXRefId=c.dbXRef and c.cvTermId=fc.cvTerm " +
	    			"and fc.feature=f.featureId",
    			new String[]{"number"},
    			new Object[]{number});
		for (Feature polypep : polypeptides) {
			logger.info(polypep.getUniqueName());
			List<Feature> genes = getHibernateTemplate().findByNamedParam("select f " +
					"from Feature f,FeatureRelationship f1,FeatureRelationship f2 where " +
					"f2.featureBySubjectId=:polypep and f2.featureByObjectId=f1.featureBySubjectId " +
					"and f1.featureByObjectId=f",
					new String[]{"polypep"},
					new Object[]{polypep});
			if(genes.size() > 0) {
				features.add(genes.get(0));
			}
		}
		goName = getHibernateTemplate().findByNamedParam("select cv " +
				"from CvTerm cv where cv.dbXRef.accession=:number", new String[]{"number"}, new Object[]{number});
		
		List <Feature> flocs = new ArrayList<Feature>();
		String name = "chromosome";
		flocs = getHibernateTemplate().findByNamedParam("select f from Feature f " +
				"where f.cvTerm.name=:name",
				new String[]{"name"},
				new Object[]{name});
		List <List> data = new ArrayList<List>();
		data.add(features);
		data.add(flocs);
		data.add(goName);
    	return data;
	}



	@SuppressWarnings("unchecked")
	public FeatureDbXRef getFeatureDbXRefByFeatureAndDbXRef(Feature feature, DbXRef dbXRef) {
		List<FeatureDbXRef> results = getHibernateTemplate().findByNamedParam(
                "from FeatureDbXRef fdxr where fdxr.feature=:feature and fdxr.dbXRef=:dbXRef",
                new String[] {"feature", "dbXRef"},
                new Object[] {feature, dbXRef});
		return firstFromList(results, feature, dbXRef);
	}



	@SuppressWarnings("unchecked")
	public List<Feature> getFeaturesByAnyNameAndOrganism(String nl, String orgNames, String featureType) {

        String lookup = nl.replaceAll("\\*", "%");
        
        logger.info("Lookup='"+lookup+"' featureType='"+featureType+"' orgs='"+orgNames+"'");
        // The list of orgs is being included literally as it didn't seem to work as a parameter
        List<Feature> features = getHibernateTemplate().findByNamedParam("select f from Feature f where" +
        		" f.uniqueName like :lookup and f.cvTerm.name=:featureType and f.organism.commonName in ( "+orgNames+" )", 
        		new String[]{"lookup","featureType"}, new Object[]{lookup,featureType, });
        
        System.out.println("Size in DAO " + features.size());
        return features;
	}
	
	// Maybe replace this with lucene query
	@SuppressWarnings("unchecked")
	public List<Feature> getFeaturesByAnyNameOrProductAndOrganism(String nl, String orgs, String featureType) {
		
        String lookup = nl.replaceAll("\\*", "%");
         
        logger.info("Lookup='"+lookup+"' featureType='"+featureType+"' orgs='"+orgs+"'");
        // The list of orgs is being included literally as it didn't seem to work as a parameter
        return getHibernateTemplate().findByNamedParam("select f from Feature f, FeatureProp fp where" +
        		" (f.uniqueName like :lookup or ( fp.cvTerm.cv.name = 'genedb_products' and fp.cvTerm.name like :lookup and fp.feature = f)) and f.cvTerm.name=:featureType and f.organism.commonName in ( "+orgs+" )", 
        		new String[]{"lookup","featureType"}, new Object[]{lookup,featureType, });
	}

	@SuppressWarnings("unchecked")
    // FIXME - Remove hard coded value - make more general?
    public List<CountedName> getProducts() {
		return getHibernateTemplate().find("select new CountedName(cvt.name,count(f.uniqueName))" +
                " from CvTerm cvt,FeatureCvTerm fct,Feature f " +
				"where f=fct.feature and cvt=fct.cvTerm and cvt.cv=15 group by cvt.name");
	}

	@SuppressWarnings("unchecked")
    public List<Feature> getFeaturesByCvTermName(String cvTermName) {
		List<Feature> features = getHibernateTemplate().findByNamedParam(
				"select f.feature from FeatureCvTerm f where f.cvTerm.name like :cvTermName", 
				"cvTermName", cvTermName);
		return features;
	}

	@SuppressWarnings("unchecked")
    // FIXME - Use top level properties instead
    public List<Feature> getTopLevelFeatures() {
		String name = "chromosome%";
		List<Feature> topLevels = getHibernateTemplate().findByNamedParam("select f from Feature f " +
				"where f.cvTerm.name like :name",
				"name",
				name);
		return topLevels;
	}

	@SuppressWarnings("unchecked")
	public List<Feature> getFeaturesByCvTermNameAndCvName(String cvTermName, String cvName) {
		List<Feature> features = getHibernateTemplate().findByNamedParam(
				"select f.feature from FeatureCvTerm f where f.cvTerm.name like :cvTermName and f.cvTerm.cv.name like :cvName", 
				new String[]{"cvTermName","cvName"}, new Object[]{cvTermName,cvName});
		return features;

	}

    public List<Feature> getAllFeatureSynonymsAsFeature() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<FeatureCvTermDbXRef> getFeatureCvTermDbXRefByFeature(Feature feature) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<FeatureCvTermPub> getFeatureCvTermPubByFeature(Feature feature) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<FeatureCvTerm> getFeatureCvTermsByFeature(Feature feature) {
        // TODO Auto-generated method stub
        return null;
    }

	public List<String> getPossibleMatches(String name, CvTerm cvTerm, int limit) {
		HibernateTemplate ht = new HibernateTemplate(getSessionFactory());
        ht.setMaxResults(limit);
        return ht.findByNamedParam(
                "select f.uniqueName from Feature f where lower(f.uniqueName) like lower(:name) and f.cvTerm = :cvTerm",
                new String[]{"name", "cvTerm"}, new Object[]{"%"+name+"%", cvTerm});
	}

	public List<Feature> getFeaturesByOrganism(Organism org) {
		List<Feature> features = getHibernateTemplate().findByNamedParam(
				"from Feature f where f.organism=:org", "org", org);
		return features;
	}

	public List<Feature> getFeaturesByUniqueNames(List<String> names) {
		boolean notFirst = false;
		StringBuilder featureIds = new StringBuilder();
		for (String name : names) {
			if (notFirst) {
        		featureIds.append(", ");
        	} else {
        		notFirst = true;
        	}
			featureIds.append('\'');
			featureIds.append(name);
			featureIds.append('\'');
		}
		String query = "from Feature f where f.uniqueName in (" + featureIds.toString() + ")";
		List<Feature> features = getHibernateTemplate().find(query);
		
		return features;
	}

	public List<Feature> getFeaturesByLocation(int min, int max, String type, String organism, Feature parent) {
		
		List<Feature> features = null;
		features = getHibernateTemplate().findByNamedParam("select f from Feature f , FeatureLoc fl " +
				"where fl.fmin>=:min " +
				"and fl.fmax<=:max and fl.featureByFeatureId=f.featureId " +
				"and fl.featureBySrcFeatureId=:parent and f.cvTerm.name=:type " +
				"and f.organism.commonName=:organism", 
				new String[]{"min","max","type","organism","parent"}, new Object[]{min,max,type,organism,parent});
		
		return features;
	}

}
