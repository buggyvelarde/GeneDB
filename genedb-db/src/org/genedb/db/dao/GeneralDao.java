package org.genedb.db.dao;

import org.gmod.schema.analysis.Analysis;
import org.gmod.schema.analysis.AnalysisFeature;
import org.gmod.schema.general.Db;
import org.gmod.schema.general.DbXRef;
import org.gmod.schema.sequence.Feature;

import java.util.List;

public class GeneralDao extends BaseDao {

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

}
