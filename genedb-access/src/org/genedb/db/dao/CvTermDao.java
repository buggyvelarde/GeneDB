package org.genedb.db.dao;

import org.genedb.db.hibernate3gen.Cv;
import org.genedb.db.hibernate3gen.CvTerm;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.List;

public class CvTermDao extends HibernateDaoSupport {

    public CvTerm findById(int id) {
	return (CvTerm) getHibernateTemplate().load(CvTerm.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<CvTerm> findByNameInCv(String cvTermName, Cv cv) {
	return getHibernateTemplate().findByNamedParam(
		"from CvTerm cvTerm where cvTerm.name like :cvTermName and cvTerm.cv = :cv",
		new String[]{"cvTermName", "cv"}, new Object[]{cvTermName, cv});
    }

    
    @SuppressWarnings("unchecked")
    public CvTerm findGoCvTermByAcc(String value) {
	List<CvTerm> terms = getHibernateTemplate().findByNamedParam(
		"from CvTerm cvTerm where cvTerm.dbxref.db.name='GO' and cvTerm.dbxref.accession=:acc", 
		"acc", value);
	return terms.get(0);
    }

}
