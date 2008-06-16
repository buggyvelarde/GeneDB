package org.genedb.db.dao;

import org.apache.log4j.Logger;
import org.gmod.schema.cv.Cv;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.dao.CvDaoI;
import org.gmod.schema.general.Db;
import org.gmod.schema.general.DbXRef;
import org.gmod.schema.sequence.feature.Polypeptide;
import org.gmod.schema.utils.CountedName;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.List;

public class CvDao extends BaseDao implements CvDaoI {

    private static Logger logger = Logger.getLogger(CvDao.class);

    protected GeneralDao generalDao;

    public Cv getCvById(int id) {
        return (Cv) getHibernateTemplate().load(Cv.class, id);
    }

    public List<Cv> getCvsByNamePattern(String namePattern) {
        @SuppressWarnings("unchecked")
        List<Cv> cvs = getHibernateTemplate().findByNamedParam(
            "from Cv cv where cv.name like :name", "name", namePattern);
        return cvs;
    }

    public Cv getCvByName(String name) {
        @SuppressWarnings("unchecked")
        List<Cv> cvs = getHibernateTemplate().findByNamedParam(
            "from Cv cv where cv.name like :name", "name", name);
        if (cvs.isEmpty()) {
            logger.warn(String.format("Failed to find CV with name '%s'", name));
            return null;
        }
        return cvs.get(0);
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
        final Db DB_GO = generalDao.getDbByName("GO");

        // Find cvterm for db_xref
        TransactionTemplate tt = new TransactionTemplate(generalDao.getPlatformTransactionManager());
        return (CvTerm) tt.execute(
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

        public CvTerm getCvTermByNameAndCvName(String cvTermName, String cvName) {
            @SuppressWarnings("unchecked")
            List<CvTerm> cvTermList = getHibernateTemplate().findByNamedParam(
                "from CvTerm cvTerm where cvTerm.name like :cvTermName and cvTerm.cv.name like :cvName",
                new String[]{"cvTermName", "cvName"}, new Object[]{cvTermName, cvName});
            if (cvTermList == null || cvTermList.size() == 0) {
                logger.warn("No cvterms found for '"+cvTermName+"' in '"+cvName+"'");
                return null;
            } else {
                return cvTermList.get(0);
            }
        }

        /**
         * Take a cv and cvterm and look it up, or create it if it doesn't exist
         *
         * @param cv name of the cv, which must already exist
         * @param cvTerm the cvTerm to find/create
         * @return the created or looked-up CvTerm
         */
        public CvTerm findOrCreateCvTermByNameAndCvName(String cvTermName, String cvName) {
            Cv cv = this.getCvByName(cvName);
            if (cv == null) {
                return null;
            }

            List<CvTerm> cvTerms = this.getCvTermByNameInCv(cvTermName, cv);
            if (cvTerms == null || cvTerms.size() == 0) {
                Db db = generalDao.getDbByName("null");
                DbXRef dbXRef = new DbXRef(db, cvTermName);
                generalDao.persist(dbXRef);
                CvTerm cvterm = new CvTerm(cv, dbXRef, cvTermName, cvTermName);
                this.persist(cvterm);
                return cvterm;
            }
            return cvTerms.get(0);
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

        public List<CountedName> getCountedNamesByCvNameAndOrganism(String cvName, String orgNames) {
            
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

        @SuppressWarnings("unchecked")
        public List<CountedName> getCountedNamesByCvNameAndFeature(String cvName,Polypeptide polypeptide) {

            String query = "select new org.gmod.schema.utils.CountedName( fct.cvTerm.name, count" +
                    " (fct)) from FeatureCvTerm fct where" +
                    " fct.cvTerm.id in " +
                    " (select fct.cvTerm.id from FeatureCvTerm fct, Feature f" +
                    " where f=:polypeptide and fct.cvTerm.cv.name=:cvName" +
                    " and fct.feature=f)" +
                    " group by fct.cvTerm.name" +
                    " order by fct.cvTerm.name";

            List<CountedName> countedNames = getHibernateTemplate().findByNamedParam(query,
                new String[]{"polypeptide","cvName"},
                new Object[]{polypeptide,cvName});

            return countedNames;

        }
        

        @SuppressWarnings("unchecked")
        public List<CountedName> getCountedNamesByCvNameAndFeatureAndOrganism(String cvName, 
                Polypeptide polypeptide) {

            String query = "select new org.gmod.schema.utils.CountedName( fct.cvTerm.name, count" +
                    " (fct)) from FeatureCvTerm fct, Feature f where" +
                    " fct.feature.organism.commonName=:organism " +
                    " fct.cvTerm.id in " +
                    " (select fct.cvTerm.id from FeatureCvTerm fct, Feature f" +
                    " where f=:polypeptide and fct.cvTerm.cv.name=:cvName" +
                    " and fct.feature=f)" +
                    " group by fct.cvTerm.name" +
                    " order by fct.cvTerm.name";

            List<CountedName> countedNames = getHibernateTemplate().findByNamedParam(query,
                new String[]{"polypeptide", "cvName", "organism"},
                new Object[]{polypeptide, cvName, polypeptide.getOrganism().getCommonName()});

            return countedNames;

        }
        
}
