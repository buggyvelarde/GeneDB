package org.genedb.db.dao;

import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.Pub;

import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public class PubDao extends BaseDao {

    public Pub getPubById(int id) {
        return (Pub) getSession().load(Pub.class, id);
    }

    public Pub getPubByUniqueName(String uniqueName) {
        return (Pub) getSession().createQuery(
            "from Pub pub where pub.uniqueName = :uniqueName")
            .setString("uniqueName", uniqueName)
            .uniqueResult();
    }

    public Pub getPubByDbXRef(DbXRef dbXRef) {
        return (Pub) getSession().createQuery(
            "select pub from PubDbXRef pd where pd.dbXRef = :dbXRef")
            .setParameter("dbXRef", dbXRef)
            .uniqueResult();
    }
}
