package org.genedb.db.dao;

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
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CvDao extends BaseDao {

    private static Logger logger = Logger.getLogger(CvDao.class);

    private GeneralDao generalDao;

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
            new String[] { "cvTermName", "cv" }, new Object[] { cvTermName, cv });

        if (cvTermList == null || cvTermList.size() == 0) {
            logger.warn("No cvterms found for '" + cvTermName + "' in '" + cv.getName() + "'");
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
        List<CvTerm> terms = getHibernateTemplate().findByNamedParam(
            "from CvTerm cvTerm where cvTerm.dbxref.db.name='GO' and cvTerm.dbxref.accession=:acc",
            "acc", accession);
        return firstFromList(terms, "accession", accession);
    }

    public Map<String, Integer> getGoTermIdsByAcc() {
        if (DB_GO == null) {
            DB_GO = generalDao.getDbByName("GO");
        }

        Map<String, Integer> goTerms = new HashMap<String, Integer>();
        @SuppressWarnings("unchecked")
        Collection<Object[]> results = getHibernateTemplate().findByNamedParam(
            "select cvTerm.dbXRef.accession, cvTerm.id " + "from CvTerm cvTerm "
                    + "where cvTerm.dbXRef.db = :goDb", "goDb", DB_GO);
        for (Object[] result : results) {
            goTerms.put((String) result[0], (Integer) result[1]);
        }
        return goTerms;
    }

    public void setGeneralDao(GeneralDao generalDao) {
        this.generalDao = generalDao;
    }

    public boolean existsNameInOntology(String name, Cv ontology) {
        List<CvTerm> tmp = this.getCvTermByNameInCv(name, ontology);
        if (tmp == null || tmp.size() == 0) {
            return false;
        }
        return true;
    }

    public List<CvTerm> getCvTerms() {
        @SuppressWarnings("unchecked")
        List<CvTerm> cvTerms = getHibernateTemplate().loadAll(CvTerm.class);
        return cvTerms;
    }

    private Map<String,Map<String,CvTerm>> cvTermsByNameByCv = Collections.synchronizedMap(new HashMap<String,Map<String,CvTerm>>());
    public CvTerm getCvTermByNameAndCvName(String cvTermName, String cvName) {
        synchronized (cvTermsByNameByCv) {
            if (!cvTermsByNameByCv.containsKey(cvName)) {
                cvTermsByNameByCv.put(cvName, Collections.synchronizedMap(new HashMap<String,CvTerm>()));
            }
        }
        if (cvTermsByNameByCv.get(cvName).containsKey(cvTermName)) {
            return cvTermsByNameByCv.get(cvName).get(cvTermName);
        }

        @SuppressWarnings("unchecked")
        List<CvTerm> cvTermList = getHibernateTemplate().findByNamedParam(
            "from CvTerm cvTerm where cvTerm.name = :cvTermName and cvTerm.cv.name = :cvName",
            new String[] { "cvTermName", "cvName" }, new Object[] { cvTermName, cvName });
        if (cvTermList == null || cvTermList.size() == 0) {
            logger.warn("No cvterms found for '" + cvTermName + "' in '" + cvName + "'");
            return null;
        }

        if (cvTermList.size() > 1) {
            logger.error(String.format("Found %d CvTerms with cv '%s' and term name '%s'",
                cvTermList.size(), cvName, cvTermName));
        }

        CvTerm cvTerm = cvTermList.get(0);
        cvTermsByNameByCv.get(cvName).put(cvTermName, cvTerm);
        return cvTerm;
    }

    private Map<String,Map<String,CvTerm>> cvTermsByAccessionByCv = Collections.synchronizedMap(new HashMap<String,Map<String,CvTerm>>());
    public CvTerm getCvTermByAccessionAndCvName(String accession, String cvName) {
        synchronized (cvTermsByAccessionByCv) {
            if (!cvTermsByAccessionByCv.containsKey(cvName)) {
                cvTermsByAccessionByCv.put(cvName, Collections.synchronizedMap(new HashMap<String,CvTerm>()));
            }
        }
        if (cvTermsByAccessionByCv.get(cvName).containsKey(accession)) {
            return cvTermsByAccessionByCv.get(cvName).get(accession);
        }

        @SuppressWarnings("unchecked")
        List<CvTerm> cvTermList = getHibernateTemplate().findByNamedParam(
            "from CvTerm cvTerm where cvTerm.dbXRef.accession = :accession and cvTerm.cv.name = :cvName",
            new String[] { "accession", "cvName" }, new Object[] { accession, cvName });
        if (cvTermList == null || cvTermList.size() == 0) {
            logger.warn("No cvterms found for accession '" + accession + "' in '" + cvName + "'");
            return null;
        }

        if (cvTermList.size() > 1) {
            logger.error(String.format("Found %d CvTerms with cv '%s' and accession ID '%s'",
                cvTermList.size(), cvName, accession));
        }

        CvTerm cvTerm = cvTermList.get(0);
        cvTermsByAccessionByCv.get(cvName).put(accession, cvTerm);
        return cvTerm;
    }

    public CvTerm getCvTermByNameAndCvNamePattern(String cvTermName, String cvNamePattern) {
        @SuppressWarnings("unchecked")
        List<CvTerm> cvTermList = getHibernateTemplate()
                .findByNamedParam(
                    "from CvTerm cvTerm where cvTerm.name = :cvTermName and cvTerm.cv.name like :cvNamePattern",
                    new String[] { "cvTermName", "cvNamePattern" },
                    new Object[] { cvTermName, cvNamePattern });
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
            "from CvTerm cvt where cvt.dbXRef = :dbXRef", "dbXRef", dbXRef);
        if (cvTermList == null || cvTermList.size() == 0) {
            return null;
        } else {
            return cvTermList.get(0);
        }
    }

    public CvTerm getCvTermByDbAcc(String db, String acc) {
        @SuppressWarnings("unchecked")
        List<CvTerm> cvTermList = getHibernateTemplate().findByNamedParam(
            "from CvTerm where dbXRef.db.name= :db and dbXRef.accession = :acc",
            new String[] { "db", "acc" }, new Object[] { db, acc });

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
        List<CountedName> countedNames = getHibernateTemplate().findByNamedParam(
            "select new org.gmod.schema.utils.CountedName(cvt.name, count(fct.feature))"
                    + " from FeatureCvTerm fct" + " join fct.cvTerm cvt" + " where cvt.cv=:cv"
                    + " group by cvt.name", "cv", cv);
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
        List<CountedName> countedNames = getHibernateTemplate().findByNamedParam(
            "select new org.gmod.schema.utils.CountedName(cvt.name, count(fct.feature.uniqueName))"
                    + " from FeatureCvTerm fct" + " join fct.cvTerm cvt"
                    + " where fct.feature.organism.commonName in (" + orgNames + ")"
                    + " and cvt.cv.name=:cvName" + " group by cvt.name"
                    + " order by lower(cvt.name), cvt.name", "cvName", cvName);

        return countedNames;
    }

    public List<CountedName> getCountedNamesByCvNamePatternAndOrganism(String cvNamePattern,
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
        List<CountedName> countedNames = getHibernateTemplate().findByNamedParam(
            "select new org.gmod.schema.utils.CountedName(cvt.name, count(fct.feature.uniqueName))"
                    + " from FeatureCvTerm fct" + " join fct.cvTerm cvt"
                    + " where fct.feature.organism.commonName in (" + orgNames + ")"
                    + " and cvt.cv.name like :cvNamePattern" + " group by cvt.name"
                    + " order by cvt.name", "cvNamePattern", cvNamePattern);

        return countedNames;
    }

    public List<String> getPossibleMatches(String search, Cv cv, int limit) {
        HibernateTemplate ht = new HibernateTemplate(getSessionFactory());
        ht.setMaxResults(limit);

        @SuppressWarnings("unchecked")
        List<String> result = ht.findByNamedParam(
            "select name from CvTerm where name like '%'||:search||'%' and cv = :cv", new String[] {
                    "search", "cv" }, new Object[] { search, cv });

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

        List<CountedName> countedNames = getHibernateTemplate().findByNamedParam(query,
            new String[] { "polypeptide", "cvName" }, new Object[] { polypeptide, cvName });

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

        List<CountedName> countedNames = getHibernateTemplate().findByNamedParam(query,
            new String[] { "polypeptide", "cvName", "organism" },
            new Object[] { polypeptide, cvName, polypeptide.getOrganism().getCommonName() });

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

        List<CountedName> countedNames = getHibernateTemplate().findByNamedParam(query,
            new String[] { "polypeptide", "cvNamePattern", "organism" },
            new Object[] { polypeptide, cvNamePattern, polypeptide.getOrganism().getCommonName() });

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

        org.gmod.schema.cfg.FeatureType featureType = FeatureTypeUtils.getFeatureTypeForClass(annotatedClass);
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
        }
        else {
            return this.getCvTermByAccessionAndCvName(featureType.accession(), featureType.cv());
        }
    }

}
