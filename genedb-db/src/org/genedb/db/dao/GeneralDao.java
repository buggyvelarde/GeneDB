package org.genedb.db.dao;

import org.genedb.db.hibernate3gen.Db;
import org.genedb.db.hibernate3gen.DbXRef;

import org.gmod.schema.dao.GeneralDaoI;

import java.util.List;

public class GeneralDao extends BaseDao implements GeneralDaoI {

    /* (non-Javadoc)
     * @see org.genedb.db.dao.GeneralDaoI#getDbByName(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Db getDbByName(String name) {
        List<Db> results = getHibernateTemplate().findByNamedParam(
		"from Db db where db.name=:name", "name", name);
        if (results != null && results.size()>0) {
            return results.get(0);
        }
        return null;
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
        if (xrefs != null && xrefs.size()>0) {
            return xrefs.get(0);
        }
        return null;
    }

}
