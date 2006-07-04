package org.genedb.db.dao;

import org.genedb.db.hibernate3gen.Organism;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.ArrayList;
import java.util.List;

public class OrganismDao extends HibernateDaoSupport {

    public Organism findById(int id) {
	return (Organism) getHibernateTemplate().load(Organism.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<Organism> findByCommonName(String commonName) {
	return getHibernateTemplate().findByNamedParam(
		"from Organism org where org.commonName like :commonname", "commonname", commonName);
    }
    
    @SuppressWarnings("unchecked")
	public List<String> findAll() {
    	List <String> organisms = new ArrayList<String>();
    	List <Organism> o = (List<Organism>)getHibernateTemplate().loadAll(Organism.class);
    	for (Organism organism : o) {
			organisms.add(organism.getAbbreviation());
			logger.info(organism.getAbbreviation());
		}
    	return organisms;
    }
    
    
}
