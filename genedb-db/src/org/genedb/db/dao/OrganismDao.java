package org.genedb.db.dao;


import org.gmod.schema.dao.OrganismDaoI;
import org.gmod.schema.organism.Organism;

import java.util.ArrayList;
import java.util.List;

public class OrganismDao extends BaseDao implements OrganismDaoI {

    /* (non-Javadoc)
     * @see org.genedb.db.dao.OrganismDaoI#getOrganismById(int)
     */
    public Organism getOrganismById(int id) {
        return (Organism) getHibernateTemplate().load(Organism.class, id);
    }

    /* (non-Javadoc)
     * @see org.genedb.db.dao.OrganismDaoI#getOrganismByCommonName(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Organism getOrganismByCommonName(String commonName) {
        List<Organism> list = getHibernateTemplate().findByNamedParam(
		"from Organism org where org.commonName like :commonname", "commonname", commonName);
        return firstFromList(list, "commonname", commonName);
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.dao.OrganismDaoI#findAllOrganisms()
     */
    @SuppressWarnings("unchecked")
	public List<String> findAllOrganismCommonNames() {
    	List <String> organisms = new ArrayList<String>();
    	List <Organism> o = getHibernateTemplate().loadAll(Organism.class);
    	for (Organism organism : o) {
			organisms.add(organism.getAbbreviation());
			logger.info(organism.getAbbreviation());
		}
    	return organisms;
    }
    
    
}
