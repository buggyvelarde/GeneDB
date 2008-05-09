package org.genedb.db.dao;

import org.gmod.schema.cv.Cv;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.dao.CvDaoI;
import org.gmod.schema.general.Db;
import org.gmod.schema.general.DbXRef;
import org.gmod.schema.utils.CountedName;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.List;

public class CvDao extends BaseDao implements CvDaoI {

    protected GeneralDao generalDao;

    public Cv getCvById(int id) {
    	return (Cv) getHibernateTemplate().load(Cv.class, id);
    }

    public List<Cv> getCvByName(String name) {
        @SuppressWarnings("unchecked")
	List<Cv> cvs = getHibernateTemplate().findByNamedParam(
		"from Cv cv where cv.name like :name", "name", name);
	return cvs;
    }
    
    public CvTerm getCvTermById(int id) {
        return (CvTerm) getHibernateTemplate().load(CvTerm.class, id);
    }

    public List<CvTerm> getCvTermByNameInCv(String cvTermName, Cv cv) {
        @SuppressWarnings("unchecked")
        List<CvTerm> cvTermList = getHibernateTemplate().findByNamedParam(
            "from CvTerm cvTerm where cvTerm.name like :cvTermName and cvTerm.cv = :cv",
            new String[]{"cvTermName", "cv"}, new Object[]{cvTermName, cv});

        if (cvTermList == null || cvTermList.size() == 0) {
            logger.warn("No cvterms found for '"+cvTermName+"' in '"+cv.getName()+"'");
            return null;
        }
        return cvTermList;
    }

    public CvTerm getGoCvTermByAcc(String value) {
        @SuppressWarnings("unchecked")
        List<CvTerm> terms = getHibernateTemplate().findByNamedParam(
            "from CvTerm cvTerm where cvTerm.dbxref.db.name='GO' and cvTerm.dbxref.accession=:acc", 
            "acc", value);
        return firstFromList(terms, "accession", value);
    }

    public CvTerm getGoCvTermByAccViaDb(final String id) {
        @SuppressWarnings("unchecked")
        final Db DB_GO = generalDao.getDbByName("GO");

        // Find cvterm for db_xref
        TransactionTemplate tt = new TransactionTemplate(generalDao.getPlatformTransactionManager());
        CvTerm cvTerm = (CvTerm) tt.execute(
            new TransactionCallback() {
                @SuppressWarnings("unused")
                public Object doInTransaction(TransactionStatus status) {
                    final DbXRef dbxref = generalDao.getDbXRefByDbAndAcc(DB_GO, id);
                    if (dbxref == null) {
                        logger.warn("Can't find dbxref for GO id of '"+id+"'");
                        return null;
                    }
                    Collection<CvTerm> cvTermDbXRefs = dbxref.getCvTerms();
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

        public void setGeneralDao(GeneralDao generalDao) {
            this.generalDao = generalDao;
        }

        public boolean existsNameInOntology(String name, Cv ontology) {
            List<CvTerm> tmp = this.getCvTermByNameInCv(name, ontology);
            if (tmp == null || tmp.size()==0) {
                return false;
            }
            return true;
        }

        public List<CvTerm> getCvTerms() {
            @SuppressWarnings("unchecked")
            List<CvTerm> cvTerms = getHibernateTemplate().loadAll(CvTerm.class);
            return cvTerms;
        }

        public CvTerm getCvTermByNameAndCvName(String cvTermName, String name) {
            @SuppressWarnings("unchecked")
            List<CvTerm> cvTermList = getHibernateTemplate().findByNamedParam(
                "from CvTerm cvTerm where cvTerm.name like :cvTermName and cvTerm.cv.name like :name",
                new String[]{"cvTermName", "name"}, new Object[]{cvTermName, name});
            if (cvTermList == null || cvTermList.size() == 0) {
                logger.warn("No cvterms found for '"+cvTermName+"' in '"+name+"'");
                return null;
            } else {
                return cvTermList.get(0);
            }
        }
     
        public CvTerm getCvTermByDbXRef(DbXRef dbXRef) {
            @SuppressWarnings("unchecked")
            List<CvTerm> cvTermList = getHibernateTemplate().findByNamedParam(
                "from CvTerm cvt where cvt.dbXRef = :dbXRef","dbXRef" , dbXRef);
            if (cvTermList == null || cvTermList.size() == 0) {
                return null;
            } else {
                return cvTermList.get(0);
            }
        }

        public CvTerm getCvTermByDbAcc(String db, String acc) {
            @SuppressWarnings("unchecked")
            List<CvTerm> cvTermList = getHibernateTemplate().findByNamedParam(
                "cvt from CvTerm cvt, DbXRef dbx where cvt.dbXRef = dbx and dbx.db.name= :db and dbx.accession = :acc",
                new String[]{db, acc}, 
                new Object[]{db, acc});
            if (cvTermList == null || cvTermList.size() == 0) {
                return null;
            } else {
                return cvTermList.get(0);
            }
        }

        public List<CountedName> getAllTermsInCvWithCount(Cv cv) {
            @SuppressWarnings("unchecked")
            List<CountedName> countedNames = getHibernateTemplate().findByNamedParam(
                "select new org.gmod.schema.utils.CountedName(cvt.name, count(fct.feature))" +
                " from FeatureCvTerm fct" +
                " join fct.cvTerm cvt" +
                " where cvt.cv=:cv" +
                " group by cvt.name",
                "cv", cv);
            return countedNames;
        }
        
        public List<CountedName> getCountedNamesByCvNameAndOrganism(String cvName, List<String> orgList) {
            StringBuilder orgNames = new StringBuilder();
            boolean first = true;
            for (String orgName : orgList) {
            	if (!first)
            	    orgNames.append(", ");
            	first = false;
            	orgNames.append("'" + orgName.replaceAll("'", "''") + "'");
            }

            @SuppressWarnings("unchecked")
            List<CountedName> countedNames = getHibernateTemplate().findByNamedParam(
                "select new org.gmod.schema.utils.CountedName(cvt.name, count(fct.feature.uniqueName))"+
                " from FeatureCvTerm fct" +
                " join fct.cvTerm cvt" +
                " where fct.feature.organism.commonName in ("+orgNames+")" +
                " and cvt.cv.name=:cvName" +
                " group by cvt.name" +
                " order by cvt.name",
            "cvName", cvName);
            
            return countedNames;
        }

        public List<String> getPossibleMatches(String search, Cv cv, int limit) {
            HibernateTemplate ht = new HibernateTemplate(getSessionFactory());
            ht.setMaxResults(limit);
            
            @SuppressWarnings("unchecked")
            List<String> result = ht.findByNamedParam(
            "select name from CvTerm where name like '%'||:search||'%' and cv = :cv",
            new String[]{"search", "cv"}, new Object[]{search, cv});
            
            return result;
        }

}
