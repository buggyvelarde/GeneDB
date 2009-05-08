package org.genedb.db.dao;

import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.mapped.Phylonode;
import org.gmod.schema.mapped.Phylotree;

import org.hibernate.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public class PhylogenyDao extends BaseDao {

    public List<Phylonode> getPhyloNodesByCvTermInTree(CvTerm type, Phylotree tree) {
        @SuppressWarnings("unchecked")
        List<Phylonode> nodes = getSession().createQuery(
                "from Phylonode pn where pn.type=:type and pn.tree=:tree")
                .setParameter("type", type)
                .setParameter("tree", tree)
                .list();
        return nodes;
    }

    public Phylotree getPhyloTreeByName(String name) {
        @SuppressWarnings("unchecked")
        List<Phylotree> trees = getSession().createQuery(
                "from Phylotree where name=:name")
                .setString("name", name)
                .list();
        return firstFromList(trees, "name", name);
    }

    @SuppressWarnings("unchecked")
    public List<Phylonode> getAllPhylonodes() {
        return getSession().createCriteria(Phylonode.class).list();
    }

    public List<Phylonode> getPhylonodesByDepthAndParent(double depth, Phylonode parent) {
        final Query query;
        if(parent == null) {
            query = getSession().createQuery(
                "from Phylonode p where p.distance=:depth")
                .setDouble("depth", depth);

        } else {
            query = getSession().createQuery(
                "from Phylonode p where p.distance=:depth and p.phylonode=:parent")
                .setDouble("depth", depth)
                .setParameter("parent", parent);
        }

        @SuppressWarnings("unchecked")
        List<Phylonode> nodes = query.list();
        return nodes;
    }

    public List<Phylonode> getPhylonodesByName(String name) {
        @SuppressWarnings("unchecked")
        List<Phylonode> nodes = getSession().createQuery(
            "from Phylonode p where p.label=:name")
            .setString("name", name)
            .list();
        return nodes;
    }

    public List<Phylonode> getPhylonodesByParent(Phylonode parent) {
        @SuppressWarnings("unchecked")
        List<Phylonode> nodes = getSession().createQuery(
            "from Phylonode p where p.parent=:parent")
            .setParameter("parent", parent)
            .list();
        return nodes;
    }
    
    public boolean isPhylonodeWithOrganismFeature(Organism orga){
    	return getSession().createQuery(
    			"select o.organismId from Organism o where o = :orga and exists elements(o.features)")
    			.setEntity("orga", orga).list().size()>0;
    }
    
    public boolean isPhylonodeWithOrganismFeature(Phylonode phylonode){
    	return getSession().createQuery(
    			"select p.phylonodeId " +
    			"from Phylonode p " +
    			"inner join p.phylonodeOrganisms po " +
    			"where p = :phylonode " +
    			"and exists elements(po.organism.features)")
    			.setEntity("phylonode", phylonode)
    			.list().size()>0;
    }
}
