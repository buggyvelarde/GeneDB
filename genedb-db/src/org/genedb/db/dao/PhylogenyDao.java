package org.genedb.db.dao;

import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.dao.PhylogenyDaoI;
import org.gmod.schema.phylogeny.Phylonode;
import org.gmod.schema.phylogeny.Phylotree;

import java.util.List;

public class PhylogenyDao extends BaseDao implements PhylogenyDaoI {

    @SuppressWarnings("unchecked")
    public List<Phylonode> getPhyloNodesByCvTermInTree(CvTerm type, Phylotree tree) {
        List<Phylonode> nodes = getHibernateTemplate().findByNamedParam(
                "from Phylonode pn where pn.type=:type and pn.tree=:tree", 
                new String[]{"type", "tree"},
                new Object[]{type, tree});
        return nodes;
    }

    @SuppressWarnings("unchecked")
    public Phylotree getPhyloTreeByName(String name) {
        List<Phylotree> trees = getHibernateTemplate().findByNamedParam(
                "from Phylotree where name=:name", 
                new String[]{"name"},
                new Object[]{name});
        return firstFromList(trees, "name", name);
    }

    
    
}
