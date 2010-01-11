package org.genedb.db.dao;

import org.gmod.schema.mapped.Organism;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public class OrganismDao extends BaseDao {

    public Organism getOrganismById(int id) {
        return (Organism) getSession().load(Organism.class, id);
    }

    /**
     * Get the organism with the specified common name.
     *
     * @param commonName the common name of the organism
     * @return the organism with the specified common name,
     *          or <code>null</code> if there is no such organism.
     */
    public Organism getOrganismByCommonName(String commonName) {
        @SuppressWarnings("unchecked")
        List<Organism> list = getSession().createQuery(
            "from Organism org where org.commonName = :commonname")
        .setString("commonname", commonName).list();
        return firstFromList(list, "commonName", commonName);
    }

    /**
     * Get a list of the common names of all organisms in the database.
     * @return a list of the common names of all organisms in the database
     */
    public List<String> findAllOrganismCommonNames() {
        @SuppressWarnings("unchecked")
        List<String> organismNames = getSession().createQuery("select commonName from Organism").list();
        return organismNames;
    }

    /**
     * Get a list of all the organisms in the database.
     * @return a list of all the organisms in the database
     */
    public List<Organism> getOrganisms() {
        @SuppressWarnings("unchecked")
        List<Organism> organisms = getSession().createCriteria(Organism.class).list();
        return organisms;
    }

}
