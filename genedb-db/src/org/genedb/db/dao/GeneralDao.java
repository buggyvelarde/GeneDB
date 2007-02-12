package org.genedb.db.dao;




import org.gmod.schema.analysis.Analysis;
import org.gmod.schema.dao.GeneralDaoI;
import org.gmod.schema.general.Db;
import org.gmod.schema.general.DbXRef;

import java.util.List;

public class GeneralDao extends BaseDao implements GeneralDaoI {

    /* (non-Javadoc)
     * @see org.genedb.db.dao.GeneralDaoI#getDbByName(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Db getDbByName(String name) {
        List<Db> results = getHibernateTemplate().findByNamedParam(
		"from Db db where upper(db.name)=:name",
        "name", name.toUpperCase());
        return firstFromList(results, "name", name);
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.dao.GeneralDaoI#getDbXRefByDbAndAcc(org.genedb.db.hibernate3gen.Db, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public DbXRef getDbXRefByDbAndAcc(Db db, String accession) {
        List<DbXRef> xrefs = getHibernateTemplate().findByNamedParam(
                "from DbXRef dbXRef where dbXRef.db=:db and dbXRef.accession=:accession", 
                new String[]{"db", "accession"},
                new Object[]{db, accession});
        return firstFromList(xrefs, "db", db, "accession", accession);
    }
    
    public Analysis getAnalysisByProgram(String program) {
    	List<Analysis> temp = getHibernateTemplate().findByNamedParam("from Analysis a where a.program=:program","program",program);
    	if (temp.size() > 0) {
    		return temp.get(0);
    	} 
    	return null;
    }
}
