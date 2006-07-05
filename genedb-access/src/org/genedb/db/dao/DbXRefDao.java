package org.genedb.db.dao;

import org.genedb.db.hibernate3gen.Db;
import org.genedb.db.hibernate3gen.DbXRef;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.List;

public class DbXRefDao extends HibernateDaoSupport {

    @SuppressWarnings("unchecked")
    public DbXRef findByDbAndAcc(Db db, String accession) {
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
