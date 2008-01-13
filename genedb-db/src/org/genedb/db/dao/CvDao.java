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

    /* (non-Javadoc)
     * @see org.genedb.db.dao.CvDaoI#getCvById(int)
     */
    public Cv getCvById(int id) {
    	return (Cv) getHibernateTemplate().load(Cv.class, id);
    }

    /* (non-Javadoc)
     * @see org.genedb.db.dao.CvDaoI#getCvByName(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public List<Cv> getCvByName(String name) {
	List<Cv> cvs = getHibernateTemplate().findByNamedParam(
		"from Cv cv where cv.name like :name", "name", name);
	return cvs;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.dao.CvDaoI#getCvTermById(int)
     */
    public CvTerm getCvTermById(int id) {
        return (CvTerm) getHibernateTemplate().load(CvTerm.class, id);
        }

        /* (non-Javadoc)
         * @see org.genedb.db.dao.CvDaoI#getCvTermByNameInCv(java.lang.String, org.genedb.db.hibernate3gen.Cv)
         */
        @SuppressWarnings("unchecked")
        public List<CvTerm> getCvTermByNameInCv(String cvTermName, Cv cv) {
        	
			List<CvTerm> cvTermList = getHibernateTemplate().findByNamedParam(
		            "from CvTerm cvTerm where cvTerm.name like :cvTermName and cvTerm.cv = :cv",
		            new String[]{"cvTermName", "cv"}, new Object[]{cvTermName, cv});
			if (cvTermList == null || cvTermList.size() == 0) {
				logger.warn("No cvterms found for '"+cvTermName+"' in '"+cv.getName()+"'");
				return null;
			}
			return cvTermList;
        }

        
        /* (non-Javadoc)
         * @see org.genedb.db.dao.CvDaoI#getGoCvTermByAcc(java.lang.String)
         */
        @SuppressWarnings("unchecked")
        public CvTerm getGoCvTermByAcc(String value) {
        List<CvTerm> terms = getHibernateTemplate().findByNamedParam(
            "from CvTerm cvTerm where cvTerm.dbxref.db.name='GO' and cvTerm.dbxref.accession=:acc", 
            "acc", value);
        return firstFromList(terms, "accession", value);
        }
        
        /* (non-Javadoc)
         * @see org.genedb.db.dao.CvDaoI#getGoCvTermByAccViaDb(java.lang.String)
         */
        @SuppressWarnings("unchecked")
        public CvTerm getGoCvTermByAccViaDb(final String id) {
            final Db DB_GO = generalDao.getDbByName("GO");
            
            // Find cvterm for db_xref
            TransactionTemplate tt = new TransactionTemplate(generalDao.getPlatformTransactionManager());
            CvTerm cvTerm = (CvTerm) tt.execute(
                    new TransactionCallback() {
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
			// TODO Auto-generated method stub
			return null;
		}


		public CvTerm getCvTermByNameAndCvName(String cvTermName, String name) {
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
			List<CvTerm> cvTermList = getHibernateTemplate().findByNamedParam(
					"from CvTerm cvt where cvt.dbXRef = :dbXRef","dbXRef" , dbXRef);
			if (cvTermList == null || cvTermList.size() == 0) {
				return null;
			} else {
				return cvTermList.get(0);
			}
		}
		
        
		public CvTerm getCvTermByDbAcc(String db, String acc) {
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
            return getHibernateTemplate().findByNamedParam("select new CountedName(cvt.name,count(f.uniqueName))" +
                    " from CvTerm cvt,FeatureCvTerm fct,Feature f " +
            "where f=fct.feature and cvt=fct.cvTerm and cvt.cv=:cv group by cvt.name",
            new String[]{"cv"}, new Object[]{cv});
        }
        
        
        @SuppressWarnings("unchecked")
        public List<CountedName> getCountedNamesByCvNameAndOrganism(String cvName, List<String> orgList) {
            StringBuilder orgNames = new StringBuilder();
            boolean notFirst = false;
            for (String orgName : orgList) {
            	if (notFirst) {
            		orgNames.append(", ");
            	} else {
            		notFirst = true;
            	}
    			orgNames.append('\'');
    			orgNames.append(orgName);
    			orgNames.append('\'');
    		}
        	
            return getHibernateTemplate().findByNamedParam("select new org.gmod.schema.utils.CountedName(cvt.name,count(f.uniqueName))" +
                    " from CvTerm cvt,FeatureCvTerm fct,Feature f " +
            "where f.organism.commonName in ("+orgNames+") and f=fct.feature and cvt=fct.cvTerm and cvt.cv.name=:cvName group by cvt.name order by cvt.name",
            new String[]{"cvName"}, new Object[]{cvName});
        }


        public List<String> getPossibleMatches(String search, Cv cv, int limit) {
            HibernateTemplate ht = new HibernateTemplate(getSessionFactory());
            ht.setMaxResults(limit);
            return ht.findByNamedParam(
            "select cvTerm.name from CvTerm cvTerm where cvTerm.name like :cvTermName and cvTerm.cv = :cv",
            new String[]{"cvTermName", "cv"}, new Object[]{"%"+search+"%", cv});
        }

}
