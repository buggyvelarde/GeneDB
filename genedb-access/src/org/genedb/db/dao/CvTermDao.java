package org.genedb.db.dao;

import org.genedb.db.hibernate3gen.Cv;
import org.genedb.db.hibernate3gen.CvTerm;
import org.genedb.db.hibernate3gen.CvTermDbXRef;
import org.genedb.db.hibernate3gen.Db;
import org.genedb.db.hibernate3gen.DbXRef;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Set;

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
    
    @SuppressWarnings("unchecked")
    public CvTerm findGoCvTermByAccViaDb(final String id, final DaoFactory daoFactory) {
        final Db DB_GO = daoFactory.getDbDao().findByName("GO");
        
        // Find cvterm for db_xref
        TransactionTemplate tt = new TransactionTemplate(daoFactory.getTransactionManager());
        CvTerm cvTerm = (CvTerm) tt.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus status) {
                        final DbXRef dbxref = daoFactory.getDbXRefDao().findByDbAndAcc(DB_GO, id);
                        if (dbxref == null) {
                            logger.warn("Can't find dbxref for GO id of '"+id+"'");
                            return null;
                        }
                        Set<CvTerm> cvTermDbXRefs = dbxref.getCvterms();
                        if (cvTermDbXRefs.size() > 1) {
                            logger.warn("More than one cvTerm for go id of '"+id+"'");
                            return null;
                        }
                        if (cvTermDbXRefs.size() == 0) {
                            logger.warn("No cvTerm for go id of '"+id+"'");
                            return null;
                        }
                        return cvTermDbXRefs.iterator().next();
                    }
                });
        return cvTerm;
    }
    
}
