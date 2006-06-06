package org.genedb.db.dao;

import org.genedb.db.hibernate.Cv;
import org.genedb.db.hibernate.Cvterm;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.List;

public class CvTermDao extends HibernateDaoSupport {

    public Cvterm findById(int id) {
	return (Cvterm) getHibernateTemplate().load(Cvterm.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<Cvterm> findByNameInCv(String cvTermName, Cv cv) {
	return getHibernateTemplate().findByNamedParam(
		"from Cvterm cvTerm where cvTerm.name like :cvTermName and cvTerm.cv = :cv",
		new String[]{"cvTermName", "cv"}, new Object[]{cvTermName, cv});
    }

    
    @SuppressWarnings("unchecked")
    public Cvterm findGoCvTermByAcc(String value) {
	List<Cvterm> terms = getHibernateTemplate().findByNamedParam(
		"from Cvterm cvTerm where cvTerm.dbxref.db.name='GO' and cvTerm.dbxref.accession=:acc", 
		"acc", value);
	return terms.get(0);
    }

}
