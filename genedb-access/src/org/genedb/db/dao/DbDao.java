package org.genedb.db.dao;

import org.genedb.db.hibernate3gen.Db;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.List;

public class DbDao extends HibernateDaoSupport {

    @SuppressWarnings("unchecked")
    public Db findByName(String name) {
        List<Db> results = getHibernateTemplate().findByNamedParam(
		"from Db db where db.name=:name", "name", name);
        if (results != null && results.size()>0) {
            return results.get(0);
        }
        return null;
    }

}
