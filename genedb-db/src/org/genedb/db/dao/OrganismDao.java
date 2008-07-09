package org.genedb.db.dao;

import org.gmod.schema.organism.Organism;

import java.util.List;

public class OrganismDao extends BaseDao {

    public Organism getOrganismById(int id) {
        return (Organism) getHibernateTemplate().load(Organism.class, id);
    }

    public Organism getOrganismByCommonName(String commonName) {
        @SuppressWarnings("unchecked")
        List<Organism> list = getHibernateTemplate().findByNamedParam(
        "from Organism org where org.commonName like :commonname", "commonname", commonName);
        return firstFromList(list, "commonname", commonName);
    }

    public List<String> findAllOrganismCommonNames() {
        @SuppressWarnings("unchecked")
        List<String> organismNames = getHibernateTemplate().find("select commonName from Organism");
        return organismNames;
    }

    public List<Organism> getOrganisms() {
        @SuppressWarnings("unchecked")
        List<Organism> organisms = getHibernateTemplate().loadAll(Organism.class);
        return organisms;
    }

}
