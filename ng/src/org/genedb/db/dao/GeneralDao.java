package org.genedb.db.dao;

import org.gmod.schema.mapped.Analysis;
import org.gmod.schema.mapped.AnalysisFeature;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Db;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.Synonym;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public class GeneralDao extends BaseDao {
    private static final Logger logger = Logger.getLogger(GeneralDao.class);

    private CvDao cvDao;
    public Db getDbByName(String name) {
        @SuppressWarnings("unchecked")
        List<Db> results = getSession().createQuery(
            "from Db db where upper(db.name)=:name")
        .setString("name", name.toUpperCase())
        .list();
        return firstFromList(results, "name", name);
    }

    public DbXRef getDbXRefByDbAndAcc(Db db, String accession) {
        @SuppressWarnings("unchecked")
        List<DbXRef> xrefs = getSession().createQuery(
                "from DbXRef dbXRef where dbXRef.db=:db and dbXRef.accession=:accession")
                .setParameter("db", db)
                .setString("accession", accession)
                .list();
        return firstFromList(xrefs, "db", db, "accession", accession);
    }

    public Analysis getAnalysisByProgram(String program) {
        @SuppressWarnings("unchecked")
        List<Analysis> temp = getSession().createQuery("from Analysis where program=:program")
            .setString("program",program).list();
        if (temp.size() > 0) {
            return temp.get(0);
        }
        return null;
    }

    public AnalysisFeature getAnalysisFeatureFromFeature(Feature feature) {
        @SuppressWarnings("unchecked")
        List<AnalysisFeature> results = getSession().createQuery("from AnalysisFeature where feature = :feature")
            .setParameter("feature", feature).list();
        return firstFromList(results,"feature",feature);
    }

    public Synonym getSynonym(String synonymType, String synonymString) {
        @SuppressWarnings("unchecked")
        List<Synonym> synonyms = getSession().createQuery(
            "from Synonym where type.cv.name='genedb_synonym_type' and type.name=:type and name=:name")
            .setString("type", synonymType).setString("name", synonymString).list();
        return super.firstFromList(synonyms, "type", synonymType, "name", synonymString);
    }

    public Synonym getSynonym(int synonymTypeId, String synonymString) {
        @SuppressWarnings("unchecked")
        List<Synonym> synonyms = getSession().createQuery(
            "from Synonym where type.cvTermId=:typeId and name=:name")
            .setInteger("typeId", synonymTypeId).setString("name", synonymString).list();
        return firstFromList(synonyms, "typeId", synonymTypeId, "name", synonymString);
    }

    public Synonym getSynonym(CvTerm synonymType, String synonymString) {
        @SuppressWarnings("unchecked")
        List<Synonym> synonyms = getSession().createQuery(
            "from Synonym where type=:type and name=:name")
            .setParameter("type", synonymType).setString("name", synonymString).list();
        return super.firstFromList(synonyms, "type", synonymType, "name", synonymString);
    }

    public Synonym getOrCreateSynonym(String synonymType, String synonymString) {
        logger.trace(String.format("Looking for synonym '%s' of type '%s'", synonymString, synonymType));
        Synonym synonym = getSynonym(synonymType, synonymString);
        if (synonym != null) {
            logger.trace("Synonym found in database");
            return synonym;
        }

        logger.trace("Creating new synonym");
        CvTerm synonymTypeCvTerm = cvDao.getCvTermByNameAndCvName(synonymType, "genedb_synonym_type");
        return new Synonym(synonymTypeCvTerm, synonymString, synonymString);
    }

    public Synonym getOrCreateSynonym(int synonymTypeId, String synonymString) {
        logger.trace(String.format("Looking for synonym '%s' of type %d", synonymString, synonymTypeId));
        Synonym synonym = getSynonym(synonymTypeId, synonymString);
        if (synonym != null) {
            logger.trace("Synonym found in database");
            return synonym;
        }

        logger.trace("Creating new synonym");
        CvTerm synonymTypeCvTerm = cvDao.getCvTermById(synonymTypeId);
        return new Synonym(synonymTypeCvTerm, synonymString, synonymString);
    }

    public Synonym getOrCreateSynonym(CvTerm synonymType, String synonymString) {
        logger.trace(String.format("Looking for synonym '%s' of type '%s'", synonymString, synonymType.getName()));
        Synonym synonym = getSynonym(synonymType, synonymString);
        if (synonym != null) {
            logger.trace("Synonym found in database");
            return synonym;
        }

        logger.trace("Creating new synonym");
        return new Synonym(synonymType, synonymString, synonymString);
    }


    public List<Db> getAllDbs() {
        @SuppressWarnings("unchecked")
        List<Db> dbs = getSession().createQuery(
                "from Db")
                .list();
        return dbs;
    }


    public List<String> getAllDbNames() {
        @SuppressWarnings("unchecked")
        List<String> names = getSession().createQuery(
                "select name from Db")
                .list();
        return names;
    }

    public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }

}
