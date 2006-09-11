package org.genedb.db.dao;


import org.gmod.schema.dao.PubDaoI;
import org.gmod.schema.pub.Pub;

import java.util.List;

public class PubDao extends BaseDao implements PubDaoI {

    /* (non-Javadoc)
     * @see org.genedb.db.dao.PubDaoI#getPubById(int)
     */
    public Pub getPubById(int id) {
        return (Pub) getHibernateTemplate().load(Pub.class, id);
    }

    /* (non-Javadoc)
     * @see org.genedb.db.dao.PubDaoI#getPubsByUniqueName(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Pub getPubByUniqueName(String uniqueName) {
        List<Pub> list = getHibernateTemplate().findByNamedParam(
                "from Pub pub where pub.uniqueName like :uniqueName", "uniqueName", uniqueName);
        return firstFromList(list, "uniqueName", uniqueName);
    }

//    public Pub findOrCreateByPmid(String pmid) {
//	Dbxref xref = dbXRefDao.findByDbAcc(pmid);
//	Pub pub = null;
//	if (xref == null) {
//	    // No pubmed entry - create everything
//	    xref = new Dbxref();
//	    xref.setDb(DB_PUBMED);
//	    xref.setAccession(dbAcc);
//	    pub = new Pub();
//	    pub.setUniquename("Temporary placeholder for "+pmid);
//	    
//	    
//	    return pub;
//	}
//	// Pubmed entry - try and find corresponding Pub
//	List<PubDbxref> pdxrs = pubDbXRefDao.findByDbXref(xref);
//	if (pdxr != null) {
//	    for (PubDbxref pdxr : pdxrs) {
//		
//	    }
//	    
//	    
//	    PubDbxref pd = new PubDbxref();
//	}
//    }
//	
//	// TODO Auto-generated method stub
//	return null;
//    }

}
