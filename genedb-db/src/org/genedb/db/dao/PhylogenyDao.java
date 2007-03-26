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

    @SuppressWarnings({ "cast", "unchecked" })
    public List<Phylonode> getAllPhylonodes() {
        return (List<Phylonode>) getHibernateTemplate().loadAll(Phylonode.class);
    }
	
	/* returns phylonodes that have depth 'd'
	 * (non-Javadoc)
	 * @see org.gmod.schema.dao.PhylogenyDaoI#getPhylonodeByDepth(double)
	 
	@SuppressWarnings("unchecked")
	public List<Phylonode> getPhylonodeByDepth(double depth) {
		List<Phylonode> nodes = getHibernateTemplate().findByNamedParam(
				"from Phylonode p where p.distance=:depth", 
				"depth", depth);
		return nodes;
	}*/
	

	@SuppressWarnings("unchecked")
	public List<Phylonode> getPhylonodeByDepthAndParent(double depth, Phylonode parent) {
		List<Phylonode> nodes = null;
		if(parent == null) {
			nodes = getHibernateTemplate().findByNamedParam(
					"from Phylonode p where p.distance=:depth",
					new String("depth"),
					depth);

		} else {
			nodes = getHibernateTemplate().findByNamedParam(
				"from Phylonode p where p.distance=:depth and p.phylonode=:parent",
				new String[]{"depth","parent"},
				new Object[]{depth,parent});
		}
		return nodes;
	}

	@SuppressWarnings("unchecked")
	public List<Phylonode> getPhylonodeByName(String name) {
		List<Phylonode> nodes = getHibernateTemplate().findByNamedParam(
				"from Phylonode p where p.label=:name", new String("name"), 
				name);
		return nodes;
	}

	@SuppressWarnings("unchecked")
	public List<Phylonode> getPhylonodesByParent(Phylonode parent) {
		List<Phylonode> nodes = getHibernateTemplate().findByNamedParam(
				"from Phylonode p where p.phylonode=:parent", new String("parent"), 
				parent);
		return nodes;

	}

    
}
