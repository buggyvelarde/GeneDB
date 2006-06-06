package org.genedb.db.dao;

import org.genedb.db.hibernate.Cv;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.List;

public class CvDao extends HibernateDaoSupport {

    public Cv findById(int id) {
	return (Cv) getHibernateTemplate().load(Cv.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<Cv> findByName(String name) {
	List<Cv> cvs = getHibernateTemplate().findByNamedParam(
		"from Cv cv where cv.name like :name", "name", name);
	return cvs;
    }

}
