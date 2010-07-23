package org.genedb.db.dao;

import org.genedb.util.SynchronizedTwoKeyMap;
import org.genedb.util.TwoKeyMap;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.cfg.FeatureTypeUtils;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.mapped.Cv;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Db;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.utils.CountedName;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional(readOnly = true)
public class CvDao extends BaseDao {

    private static Logger logger = Logger.getLogger(CvDao.class);

    private static final int CVTERM_MAX_LENGTH = 1024;
    private static final int DBXREF_ACCESSION_MAX_LENGTH = 255;

    private GeneralDao generalDao;

    public Cv getCvById(int id) {
        return (Cv) getSession().load(Cv.class, id);
    }

    public List<Cv> getCvsByNamePattern(String namePattern) {
        @SuppressWarnings("unchecked")
        List<Cv> cvs = getSession().createQuery(
            "from Cv cv where cv.name like :name")
            .setString("name", namePattern).list();
        return cvs;
    }

    private Map<String,Cv> cvByName = new HashMap<String,Cv>();
    public synchronized Cv getCvByName(String name) {
        if (cvByName.containsKey(name)) {
            return cvByName.get(name);
        }

        @SuppressWarnings("unchecked")
        List<Cv> cvs = getSession().createQuery(
            "from Cv cv where cv.name like :name")
            .setString("name", name).list();
        if (cvs.isEmpty()) {
            logger.warn(String.format("Failed to find CV with name '%s'", name));
            return null;
        }
        Cv cv = cvs.get(0);
        cvByName.put(name, cv);
        return cv;
    }

    public CvTerm getCvTermById(int id) {
        logger.trace(String.format("Fetching CvTerm with id %d", id));
        return (CvTerm) getSession().load(CvTerm.class, id);
    }

    public List<CvTerm> getCvTermByNamePatternInCv(String cvTermNamePattern, Cv cv) {
        @SuppressWarnings("unchecked")
        List<CvTerm> cvTermList = getSession().createQuery(
            "from CvTerm cvTerm where cvTerm.name like :cvTermNamePattern and cvTerm.cv = :cv")
            .setString("cvTermNamePattern", cvTermNamePattern).setParameter("cv", cv).list();

        if (cvTermList == null || cvTermList.size() == 0) {
            logger.warn("No cvterms found matching '" + cvTermNamePattern + "' in '" + cv.getName() + "'");
            return null;
        }
        return cvTermList;
    }

    private Db DB_GO;

    public CvTerm getGoCvTermByAcc(String accession) {
        if (DB_GO == null) {
            DB_GO = generalDao.getDbByName("GO");
        }

        @SuppressWarnings("unchecked")
        List<CvTerm> terms = getSession().createQuery(
            "from CvTerm cvTerm where cvTerm.dbxref.db.name='GO' and cvTerm.dbxref.accession=:acc")
            .setString("acc", accession).list();
        return firstFromList(terms, "accession", accession);
    }

    public Map<String, Integer> getGoTermIdsByAcc() {
        if (DB_GO == null) {
            DB_GO = generalDao.getDbByName("GO");
        }

        Map<String, Integer> goTerms = new HashMap<String, Integer>();
        @SuppressWarnings("unchecked")
        Collection<Object[]> results = getSession().createQuery(
            "select cvTerm.dbXRef.accession, cvTerm.id "
                    + "from CvTerm cvTerm "
                    + "where cvTerm.dbXRef.db = :goDb")
                .setParameter("goDb", DB_GO).list();
        for (Object[] result : results) {
            goTerms.put((String) result[0], (Integer) result[1]);
        }
        return goTerms;
    }

    public void setGeneralDao(GeneralDao generalDao) {
        this.generalDao = generalDao;
    }

    public boolean existsNameInOntology(String name, Cv ontology) {
        List<CvTerm> tmp = this.getCvTermByNamePatternInCv(name, ontology);
        if (tmp == null || tmp.size() == 0) {
            return false;
        }
        return true;
    }

    public List<CvTerm> getCvTerms() {
        @SuppressWarnings("unchecked")
        List<CvTerm> cvTerms = getSession().createCriteria(CvTerm.class).list();
        return cvTerms;
    }

    private TwoKeyMap<String,String,Integer> cvTermIdsByCvAndLcName = new SynchronizedTwoKeyMap<String,String,Integer>();

    /**
     * Get the CvTerm with the specified CV and name. If there is no such term,
     * log a warning and return <code>null</code>. This is the same as
     * <code>getCvTermByNameAndCvName(cvTermName, cvName, true)</code>.
     *
     * @param cvTermName the term, treated case-insensitively
     * @param cvName the name of the CV
     * @return the CvTerm with the specified CV and name, or <code>null</code> if there is no such term.
     */
    public CvTerm getCvTermByNameAndCvName(String cvTermName, String cvName) {
        return getCvTermByNameAndCvName(cvTermName, cvName, true);
    }
    /**
     * Get the CvTerm with the specified CV and name. If there is no such term,
     * return <code>null</code>. If the parameter <code>complainIfNotFound</code>
     * is true, then also log a warning in this case.
     *
     * @param cvTermName the term, treated case-insensitively
     * @param cvName the name of the CV
     * @param complainIfNotFound whether to log a warning if the term is not found. Only
     *          pass <code>false</code> here if you're genuinely agnostic about whether the
     *          term exists
     * @return the CvTerm with the specified CV and name, or <code>null</code> if there is no such term.
     */
    public CvTerm getCvTermByNameAndCvName(String cvTermName, String cvName, boolean complainIfNotFound) {
        Session session = getSession();

        String lcTermName = cvTermName.toLowerCase();

        synchronized (cvTermIdsByCvAndLcName) {
            if (cvTermIdsByCvAndLcName.containsKey(cvName, lcTermName)) {
                CvTerm cvTerm = (CvTerm) session.get(CvTerm.class, cvTermIdsByCvAndLcName.get(cvName, lcTermName));
                if (cvTerm != null) {
                    /*
                     * It is possible for the ID to be in the cache but the CvTerm not to exist
                     * in the database, even if only a single thread is accessing the database
                     * at a time: the CvTerm might have been added in a session that was later
                     * rolled back.
                     */
                    return cvTerm;
                }
            }
        }

        @SuppressWarnings("unchecked")
        List<CvTerm> cvTermList = session.createQuery(
            "from CvTerm cvTerm where lower(cvTerm.name) = :lcTermName and cvTerm.cv.name = :cvName")
            .setString("lcTermName", lcTermName).setString("cvName", cvName).list();
        if (cvTermList == null || cvTermList.size() == 0) {
            if (complainIfNotFound) {
                logger.warn("No cvterms found for '" + cvTermName + "' in '" + cvName + "'");
            }
            return null;
        }

        if (cvTermList.size() > 1) {
            logger.error(String.format("Found %d CvTerms with cv '%s' and term name '%s'",
                cvTermList.size(), cvName, cvTermName));
        }

        CvTerm cvTerm = cvTermList.get(0);
        cvTermIdsByCvAndLcName.put(cvName, lcTermName, cvTerm.getCvTermId());
        return cvTerm;
    }

    private TwoKeyMap<String,String,Integer> cvTermIdsByAccessionAndCv = new SynchronizedTwoKeyMap<String,String,Integer>();
    public CvTerm getCvTermByAccessionAndCvName(String accession, String cvName) {
        Session session = getSession();

        if (cvTermIdsByAccessionAndCv.containsKey(cvName, accession)) {
            CvTerm cvTerm = (CvTerm) session.get(CvTerm.class, cvTermIdsByAccessionAndCv.get(cvName, accession));
            if (cvTerm != null) {
                /*
                 * It is possible for the ID to be in the cache but the CvTerm not to exist
                 * in the database, even if only a single thread is accessing the database
                 * at a time: the CvTerm might have been added in a session that was later
                 * rolled back.
                 */
                return cvTerm;
            }
        }

        @SuppressWarnings("unchecked")
        List<CvTerm> cvTermList = session.createQuery(
            "from CvTerm cvTerm where cvTerm.dbXRef.accession = :accession and cvTerm.cv.name = :cvName")
            .setString("accession", accession).setString("cvName", cvName).list();

        if (cvTermList == null || cvTermList.size() == 0) {
            logger.warn("No cvterms found for accession '" + accession + "' in '" + cvName + "'");
            return null;
        }

        if (cvTermList.size() > 1) {
            logger.error(String.format("Found %d CvTerms with cv '%s' and accession ID '%s'",
                cvTermList.size(), cvName, accession));
        }

        CvTerm cvTerm = cvTermList.get(0);
        cvTermIdsByAccessionAndCv.put(cvName, accession, cvTerm.getCvTermId());
        return cvTerm;
    }

    /**
     * Get the CvTerm with the specified CV and name, assuming that it exists.
     *
     * @param termName the term
     * @param cvName the name of the CV, treated case-insensitively
     * @return the CvTerm with the specified CV and name
     * @throws RuntimeException if there is no such term
     */
    public CvTerm getExistingCvTermByNameAndCvName(String termName, String cvName) {
        CvTerm cvTerm = getCvTermByNameAndCvName(termName, cvName, false);
        if (cvTerm == null) {
            throw new RuntimeException(String.format("CV term '%s:%s' does not exist",
                cvName, termName));
        }
        return cvTerm;
    }

    public CvTerm getCvTermByNameAndCvNamePattern(String cvTermName, String cvNamePattern) {
        @SuppressWarnings("unchecked")
        List<CvTerm> cvTermList = getSession()
                .createQuery(
                    "from CvTerm cvTerm where cvTerm.name = :cvTermName and cvTerm.cv.name like :cvNamePattern")
                 .setString("cvTermName", cvTermName)
                 .setString("cvNamePattern", cvNamePattern)
                 .list();
        if (cvTermList == null || cvTermList.size() == 0) {
            logger.warn("No cvterms found for '" + cvTermName + "' in CV matching '"
                    + cvNamePattern + "'");
            return null;
        }

        if (cvTermList.size() > 1) {
            logger.error(String.format("Found %d CvTerms with cv matching '%s' and term name '%s'",
                cvTermList.size(), cvNamePattern, cvTermName));
        }

        return cvTermList.get(0);
    }

    private volatile Db nullDb = null;
    private Db nullDb() {
        if (nullDb == null) {
            synchronized(this) {
                if (nullDb == null) {
                    nullDb = generalDao.getDbByName("null");
                }
            }
        }

        return nullDb;
    }

    /**
     * Take a cv and cvterm and look it up, or create it if it doesn't exist
     *
     * @param cv name of the cv, which must already exist
     * @param cvTerm the cvTerm to find/create; case-insensitive, but the supplied
     *                  case will be used if the term is created
     * @return the created or looked-up CvTerm
     */
    public CvTerm findOrCreateCvTermByNameAndCvName(String cvTermName, String cvName) {
        return findOrCreateCvTermByNameAndCvName(cvTermName, cvName, nullDb());
    }

    /**
     * Take a cv and cvterm and look it up, or create it if it doesn't exist
     *
     * @param cv name of the cv, which must already exist
     * @param cvTerm the cvTerm to find/create; case-insensitive, but the supplied
     *                  case will be used if the term is created
     * @param db the name of the database to use for the associated DbXRef, if a new term is created
     * @return the created or looked-up CvTerm
     */
    public CvTerm findOrCreateCvTermByNameAndCvName(String cvTermName, String cvName, String dbName) {
        Db db;
        if (dbName == null) {
            db = nullDb();
        } else {
            db = generalDao.getDbByName(dbName);
        }
        return findOrCreateCvTermByNameAndCvName(cvTermName, cvName, db);
    }

    /**
     * Take a cv and cvterm and look it up, or create it if it doesn't exist
     *
     * @param cv name of the cv, which must already exist
     * @param cvTerm the cvTerm to find/create; case-insensitive, but the supplied
     *                  case will be used if the term is created
     * @param db the database to use for the associated DbXRef, if a new term is created
     * @return the created or looked-up CvTerm
     */
    public CvTerm findOrCreateCvTermByNameAndCvName(String cvTermName, String cvName, Db db) {
        String cvTermNameTruncatedForCvTerm = cvTermName;
        if (cvTermName.length() > CVTERM_MAX_LENGTH) {
            logger.warn(String.format("CV Term name is longer than %d characters: %s\n" +
                                      "Truncating to fit in cvterm.name.\n" +
                                      "(The full name will be kept in the CvTerm definition.)",
                CVTERM_MAX_LENGTH, cvTermName));
            cvTermNameTruncatedForCvTerm = cvTermName.substring(0, CVTERM_MAX_LENGTH);
        }
        CvTerm cvTerm = this.getCvTermByNameAndCvName(cvTermNameTruncatedForCvTerm, cvName, false);
        if (cvTerm == null) {
            logger.trace(String.format("CV term '%s:%s' not found; creating with dbxref in DB '%s'",
                cvName, cvTermNameTruncatedForCvTerm, db == null ? null : db.getName()));
            String cvTermNameTruncatedForDbXRef = cvTermName;
            if (cvTermName.length() > DBXREF_ACCESSION_MAX_LENGTH) {
                logger.warn(String.format("CV Term name is longer than %d characters: %s\n" +
                                          "Truncating to fit in dbxref.accession.\n"+
                                          "(The full name will be kept in the DbXRef description.)",
                    DBXREF_ACCESSION_MAX_LENGTH, cvTermName));
                cvTermNameTruncatedForDbXRef = cvTermName.substring(0, DBXREF_ACCESSION_MAX_LENGTH);
            }

            DbXRef dbXRef = new DbXRef(db, cvTermNameTruncatedForDbXRef, cvTermName);
            persist(dbXRef);
            CvTerm cvterm = new CvTerm(this.getCvByName(cvName), dbXRef, cvTermNameTruncatedForCvTerm, cvTermName);
            persist(cvterm);
            return cvterm;
        }
        return cvTerm;
    }

    public CvTerm getCvTermByDbXRef(DbXRef dbXRef) {
        @SuppressWarnings("unchecked")
        List<CvTerm> cvTermList = getSession().createQuery(
            "from CvTerm cvt where cvt.dbXRef = :dbXRef")
            .setParameter("dbXRef", dbXRef)
            .list();
        if (cvTermList == null || cvTermList.size() == 0) {
            return null;
        } else {
            return cvTermList.get(0);
        }
    }

    public CvTerm getCvTermByDbAcc(String db, String acc) {
        @SuppressWarnings("unchecked")
        List<CvTerm> cvTermList = getSession().createQuery(
            "from CvTerm where dbXRef.db.name= :db and dbXRef.accession = :acc")
            .setParameter("db", db)
            .setParameter("acc", acc)
            .list();

        if (cvTermList == null || cvTermList.size() == 0) {
            return null;
        }

        if (cvTermList.size() > 1) {
            logger.error(String.format("Found %d CvTerms with db '%s' and accession '%s'",
                cvTermList.size(), db, acc));
        }

        return cvTermList.get(0);
    }

    public List<CountedName> getAllTermsInCvWithCount(Cv cv) {
        @SuppressWarnings("unchecked")
        List<CountedName> countedNames = getSession().createQuery(
            "select new org.gmod.schema.utils.CountedName(cvt.name, count(fct.feature))"
                    + " from FeatureCvTerm fct"
                    + " join fct.cvTerm cvt"
                    + " where cvt.cv=:cv"
                    + " group by cvt.name")
            .setParameter("cv", cv).list();
        return countedNames;
    }

    public List<CountedName> getCountedNamesByCvNameAndOrganism(String cvName,
            Collection<String> orgs) {
        StringBuilder orgNames = new StringBuilder();
        boolean first = true;
        for (String orgName : orgs) {
            if (!first) {
                orgNames.append(", ");
            }
            first = false;
            orgNames.append("'" + orgName.replaceAll("'", "''") + "'");
        }

        @SuppressWarnings("unchecked")
        List<CountedName> countedNames = getSession().createQuery(
            "select new org.gmod.schema.utils.CountedName(cvt.name, count(fct.feature.uniqueName))"
                    + " from FeatureCvTerm fct" + " join fct.cvTerm cvt"
                    + " where fct.feature.organism.commonName in (" + orgNames + ")"
                    + " and cvt.cv.name=:cvName" + " group by cvt.name"
                    + " order by lower(cvt.name), cvt.name")
                .setString("cvName", cvName)
                .list();

        return countedNames;
    }

    public List<CountedName> getCountedNamesByCvNamePatternAndOrganism(String cvNamePattern,
            Collection<String> orgs, boolean justPolypeptides) {
        StringBuilder orgNames = new StringBuilder();

        String orgQuery = "";
        if (orgs != null && orgs.size() > 0) {

        	boolean first = true;
        	for (String orgName : orgs) {
        		if (!first) {
        			orgNames.append(", ");
        		}
        		first = false;
        		orgNames.append("'" + orgName.replaceAll("'", "''") + "'");
        	}
        	orgQuery = " fct.feature.organism.commonName in (" + orgNames + ") and";
        }

        String typeQuery= "";
        if (justPolypeptides) {
            typeQuery = " fct.feature.type.name = 'polypeptide'";
        }

        @SuppressWarnings("unchecked")
        List<CountedName> countedNames = getSession().createQuery(
            "select new org.gmod.schema.utils.CountedName(cvt.name, count(fct.feature.uniqueName))"
                    + " from FeatureCvTerm fct" + " join fct.cvTerm cvt"
                    + " where"
                    + orgQuery
                    + typeQuery
                    + " and cvt.cv.name like :cvNamePattern" + " group by cvt.name"
                    + " order by cvt.name")
                .setString("cvNamePattern", cvNamePattern)
                .list();

        return countedNames;
    }

    public List<String> getPossibleMatches(String search, Cv cv, int limit) {
        @SuppressWarnings("unchecked")
        List<String> result = getSession().createQuery(
            "select name from CvTerm where name like '%'||:search||'%' and cv = :cv")
            .setString("search", search).setParameter("cv", cv)
            .setMaxResults(limit).list();

        return result;
    }

    @SuppressWarnings("unchecked")
    public List<CountedName> getCountedNamesByCvNameAndFeature(String cvName,
            Polypeptide polypeptide) {

        String query = "select new org.gmod.schema.utils.CountedName( fct.cvTerm.name, count"
                + " (fct)) from FeatureCvTerm fct where" + " fct.cvTerm.id in "
                + " (select fct.cvTerm.id from FeatureCvTerm fct, Feature f"
                + " where f=:polypeptide and fct.cvTerm.cv.name=:cvName" + " and fct.feature=f)"
                + " group by fct.cvTerm.name" + " order by fct.cvTerm.name";

        List<CountedName> countedNames = getSession().createQuery(query)
            .setParameter("polypeptide", polypeptide)
            .setString("cvName", cvName)
            .list();

        return countedNames;

    }

    /**
     * Given a Cv name and Polypeptide feature, find all the cvterms in this
     * polypeptide for Cv along with their count for the organism the
     * polypeptide belongs
     *
     * @param cvName the Cv name
     * @param polypeptide the Polypeptide feature
     * @return a (possibly empty) List<CountedName> of matches
     */
    @SuppressWarnings("unchecked")
    public List<CountedName> getCountedNamesByCvNameAndFeatureAndOrganism(String cvName,
            Polypeptide polypeptide) {

        /**
         * the distinct clause in the query counts only once if there is more
         * than FeatureCvTerm for a Feature with a particular CvTerm
         */
        String query = "select new org.gmod.schema.utils.CountedName( fct.cvTerm.name, count"
                + " (distinct fct.feature)) from FeatureCvTerm fct where"
                + " fct.feature.organism.commonName=:organism and " + " fct.cvTerm.id in "
                + " (select fct.cvTerm.id from FeatureCvTerm fct, Feature f"
                + " where f=:polypeptide and fct.cvTerm.cv.name=:cvName" + " and fct.feature=f)"
                + " group by fct.cvTerm.name" + " order by fct.cvTerm.name";

        List<CountedName> countedNames = getSession().createQuery(query)
            .setParameter("polypeptide", polypeptide)
            .setString("cvName", cvName)
            .setString("organism", polypeptide.getOrganism().getCommonName())
            .list();

        return countedNames;

    }

    /**
     * Given a Cv name and Polypeptide feature, find all the cvterms in this
     * polypeptide for Cv along with their count for the organism the
     * polypeptide belongs
     *
     * @param cvNamePattern a pattern (HQL/SQL syntax) to match against the CV
     *                name
     * @param polypeptide the Polypeptide feature
     * @return a (possibly empty) List<CountedName> of matches
     */
    @SuppressWarnings("unchecked")
    public List<CountedName> getCountedNamesByCvNamePatternAndFeatureAndOrganism(
            String cvNamePattern, Polypeptide polypeptide) {

        /**
         * the distinct clause in the query counts only once if there is more
         * than FeatureCvTerm for a Feature with a particular CvTerm
         */
        String query = "select new org.gmod.schema.utils.CountedName( fct.cvTerm.name, count"
                + " (distinct fct.feature)) from FeatureCvTerm fct where"
                + " fct.feature.organism.commonName=:organism and " + " fct.cvTerm.id in "
                + " (select fct.cvTerm.id from FeatureCvTerm fct, Feature f"
                + " where f=:polypeptide and fct.cvTerm.cv.name LIKE :cvNamePattern"
                + " and fct.feature=f)" + " group by fct.cvTerm.name" + " order by fct.cvTerm.name";

        List<CountedName> countedNames = getSession().createQuery(query)
            .setParameter("polypeptide", polypeptide)
            .setString("cvNamePattern", cvNamePattern)
            .setString("organism", polypeptide.getOrganism().getCommonName())
            .list();

        return countedNames;
    }

    private Map<Class<? extends Feature>, CvTerm> cvTermsByClass = Collections.synchronizedMap(new HashMap<Class<? extends Feature>, CvTerm>());

    /**
     * Get the CvTerm that represents the type of a particular feature class.
     *
     * @param annotatedClass the feature class
     * @return the corresponding CV term
     */
    public CvTerm getCvTermForAnnotatedClass(Class<? extends Feature> annotatedClass) {
        if (cvTermsByClass.containsKey(annotatedClass)) {
            return cvTermsByClass.get(annotatedClass);
        }

        FeatureType featureType = FeatureTypeUtils.getFeatureTypeForClass(annotatedClass);
        if (featureType == null) {
            throw new IllegalArgumentException(String.format("The class '%s' has no @FeatureType annotation", annotatedClass.getName()));
        }
        CvTerm cvTerm = getCvTermForFeatureType(featureType);
        cvTermsByClass.put(annotatedClass, cvTerm);
        return cvTerm;
    }

    private CvTerm getCvTermForFeatureType(FeatureType featureType) {
        if (!"".equals(featureType.term())) {
            return this.getCvTermByNameAndCvName(featureType.term(), featureType.cv());
        } else {
            return this.getCvTermByAccessionAndCvName(featureType.accession(), featureType.cv());
        }
    }

}
