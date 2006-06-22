package org.genedb.db.dao;

import org.biojava.bio.symbol.Location;
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
	public List<Feature> findByRange(int min,int max,String name) {
    	List<Feature> features;
    	 //int min = loc.getMin();
    	//int max = loc.getMax();
    	//String name = "mRNA";
    	features = (List<Feature>)getHibernateTemplate().findByNamedParam("select f " +
    			"from Feature f, FeatureLoc loc, CvTerm cvt where " +
    			"f.featureId=loc.featureByFeatureId and f.cvTerm=cvt.cvTermId and cvt.name='" + name + "' and (" +
    			" loc.fmin<=:min and loc.fmax>=:max)",new String[]{"min","max"},new Object[]{min,max});
     	return features;
    }

}
