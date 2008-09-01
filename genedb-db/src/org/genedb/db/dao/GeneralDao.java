package org.genedb.db.dao;

import org.gmod.schema.mapped.Analysis;
import org.gmod.schema.mapped.AnalysisFeature;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Db;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.Synonym;

import java.util.List;

public class GeneralDao extends BaseDao {

    private CvDao cvDao;
    public Db getDbByName(String name) {
        @SuppressWarnings("unchecked")
        List<Db> results = getHibernateTemplate().findByNamedParam(
        "from Db db where upper(db.name)=:name",
        "name", name.toUpperCase());
        return firstFromList(results, "name", name);
    }

    public DbXRef getDbXRefByDbAndAcc(Db db, String accession) {
        @SuppressWarnings("unchecked")
        List<DbXRef> xrefs = getHibernateTemplate().findByNamedParam(
                "from DbXRef dbXRef where dbXRef.db=:db and dbXRef.accession=:accession",
                new String[]{"db", "accession"},
                new Object[]{db, accession});
        return firstFromList(xrefs, "db", db, "accession", accession);
    }

    public Analysis getAnalysisByProgram(String program) {
        @SuppressWarnings("unchecked")
        List<Analysis> temp = getHibernateTemplate().findByNamedParam("from Analysis where program=:program","program",program);
        if (temp.size() > 0) {
            return temp.get(0);
        }
        return null;
    }

    public AnalysisFeature getAnalysisFeatureFromFeature(Feature feature) {
        @SuppressWarnings("unchecked")
        List<AnalysisFeature> results = getHibernateTemplate().findByNamedParam("from AnalysisFeature where feature = :feature",
            "feature", feature);
        return firstFromList(results,"feature",feature);
    }

    public Synonym getOrCreateSynonym(String synonymType, String synonymString) {
        @SuppressWarnings("unchecked")
        List<Synonym> synonyms = getHibernateTemplate().findByNamedParam(
            "from Synonym where type.cv.name='genedb_synonym_type' and type.name=:type and name=:name",
            new String[] {"type", "name"}, new String[] {synonymType, synonymString});
        if (!synonyms.isEmpty()) {
            return synonyms.get(0);
        }

        CvTerm synonymTypeCvTerm = cvDao.findOrCreateCvTermByNameAndCvName(synonymType, "genedb_synonym_type");
        return new Synonym(synonymTypeCvTerm, synonymString, synonymString);
    }

    public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }

}
