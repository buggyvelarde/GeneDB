package org.genedb.db.dao;

import java.util.List;

import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.general.DbXRef;
import org.gmod.schema.pub.Pub;
import org.gmod.schema.pub.PubDbXRef;
import org.gmod.schema.pub.PubProp;

public class PubDao extends BaseDao {

    public Pub getPubById(int id) {
        return (Pub) getHibernateTemplate().load(Pub.class, id);
    }

    @SuppressWarnings("unchecked")
    public Pub getPubByUniqueName(String uniqueName) {
        List<Pub> list = getHibernateTemplate().findByNamedParam(
            "from Pub pub where pub.uniqueName like :uniqueName",
            "uniqueName", uniqueName);
        return firstFromList(list, "uniqueName", uniqueName);
    }

    public Pub getPubByDbXRef(DbXRef dbXRef) {
        @SuppressWarnings("unchecked")
        List<Pub> list = getHibernateTemplate().findByNamedParam(
            "select pub from PubDbXRef where dbXRef = :dbXRef",
            "dbXRef", dbXRef);
        return firstFromList(list, "dbXRef", dbXRef);
    }

    public List<PubProp> getPubPropByPubAndCvTerm(Pub pub,CvTerm cvTerm){
        @SuppressWarnings("unchecked")
    	List<PubProp> list = getHibernateTemplate().findByNamedParam(
    	    "from PubProp pp where pp.pub=:pub and pp.cvTerm=:cvTerm",
    	    new String[]{"pub","cvTerm"},
    	    new Object[]{pub,cvTerm});
    	return list;
    }


    public List<PubDbXRef> getPubDbXRef() {
        @SuppressWarnings("unchecked")
        List<PubDbXRef> list = getHibernateTemplate().loadAll(PubDbXRef.class);
        return list;
    }

}
