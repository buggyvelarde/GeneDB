package org.genedb.db.dao;

import org.genedb.db.hibernate3gen.CvTerm;
import org.genedb.db.hibernate3gen.FeatureCvTerm;
import org.genedb.db.jpa.Feature;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.List;

public class FeatureDao extends HibernateDaoSupport {

    public Feature findById(int id) {
	return (Feature) getHibernateTemplate().load(Feature.class, id);
    }

    public Feature findByUniqueName(String name) {
	List features = getHibernateTemplate().findByNamedParam(
		"from Feature f where f.uniquename=:name", "name", name);
	if (features.size() > 0) {
	    return (Feature) features.get(0);
	}
	return null;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    public List<Feature> findByAnyCurrentName(String name) {
        List<Feature> features = (List<Feature>) getHibernateTemplate().findByNamedParam(
                "select f from Feature f, FeatureSynonym fs, Synonym s where f=fs.feature and fs.synonym=s and fs.current=true and s.name=:name",
                "name", name);
        return features;
    }
    
    @SuppressWarnings("unchecked")
	public List<Feature> findByRange(int min,int max,int strand,int fid,String name) {
    	List<Feature> features;
    	 //int min = loc.getMin();
    	//int max = loc.getMax();
    	//String name = "mRNA";
    	features = (List<Feature>)getHibernateTemplate().findByNamedParam("select f " +
    			"from Feature f, FeatureLoc loc, CvTerm cvt where " +
    			"f.featureId=loc.featureByFeatureId and f.cvTerm=cvt.cvTermId and cvt.name='" + name + "' and loc.strand=" + strand + " and" +
    			" loc.featureBySrcfeatureId=" + fid + " and (" +
    			" loc.fmin<=:min and loc.fmax>=:max)",new String[]{"min","max"},new Object[]{min,max});
     	return features;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    public List<Feature> findByAnyName(NameLookup nl,String featureType) {

        // Add wildcards if needed
        if (nl.isNeedWildcards()) {
            String lookup = nl.getLookup();
            if (!lookup.startsWith("*")) {
                lookup = "*" + lookup;
            }
            if (!lookup.endsWith("*")) {
                lookup += "*";
            }
            nl.setLookup(lookup);
            nl.setNeedWildcards(false);
        }
        
        String lookup = nl.getLookup().replaceAll("\\*", "%");
        
        // TODO Start for paging
        getHibernateTemplate().setMaxResults(nl.getPageSize()); // TODO Check
       
        // TODO Taxon and filter
        List<Feature> features = (List<Feature>)
        							getHibernateTemplate().findByNamedParam("select f from Feature f, FeatureSynonym fs, Synonym s, CvTerm cvt where f=fs.feature and fs.synonym=s and fs.current=true and f.cvTerm=cvt.cvTermId and cvt.name='" + featureType + "' and s.name like :name",
        							"name", lookup);
        return features;
    }

    public FeatureCvTerm findFeatureCvTermByFeatureAndCvTerm(Feature feature, CvTerm cvTerm, boolean not) {
        List<FeatureCvTerm> list = getHibernateTemplate().findByNamedParam("from FeatureCvTerm fct where fct.feature=:feature and fct.cvterm=:cvTerm and fct.not=:not", 
                new String[]{"feature", "cvTerm", "not"}, 
                new Object[]{feature, cvTerm, not});
        if (list == null || list.size() == 0) {
            return null;
        }
        if (list.size() > 1) {
            logger.warn("More than the expected 1 result for findFeatureCvTermByFeatureAndCvTerm. feature='"+feature.getUniquename()+"' cvTerm='"+cvTerm.getCvTermId()+"' not='"+not+"'");
        }
        return list.get(0);
    }
    
}
