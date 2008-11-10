package org.genedb.db.dao;

import org.gmod.schema.mapped.Organism;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public class OrganismDao extends BaseDao {

    public Organism getOrganismById(int id) {
        return (Organism) getSession().load(Organism.class, id);
    }

    public Organism getOrganismByCommonName(String commonName) {
        @SuppressWarnings("unchecked")
        List<Organism> list = getSession().createQuery(
            "from Organism org where org.commonName = :commonname")
        .setString("commonname", commonName).list();
        return firstFromList(list, "commonName", commonName);
    }

    public List<String> findAllOrganismCommonNames() {
        @SuppressWarnings("unchecked")
        List<String> organismNames = getSession().createQuery("select commonName from Organism").list();
        return organismNames;
    }

    public List<Organism> getOrganisms() {
        @SuppressWarnings("unchecked")
        List<Organism> organisms = getSession().createCriteria(Organism.class).list();
        return organisms;
    }

}
