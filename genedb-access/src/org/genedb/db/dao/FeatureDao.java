package org.genedb.db.dao;

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
                "select f from Feature f, FeatureSynonym fs, Synonym s where f=fs.feature and fs.synonym=s and fs.isCurrent=true and s.name=:name",
                "name", name);
        return features;
    }

}
