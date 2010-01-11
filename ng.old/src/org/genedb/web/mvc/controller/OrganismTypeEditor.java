package org.genedb.web.mvc.controller;

import org.genedb.db.dao.OrganismDao;

import org.gmod.schema.mapped.Organism;



public class OrganismTypeEditor extends java.beans.PropertyEditorSupport {

    private OrganismDao organismDao;

    public OrganismDao getOrganismDao() {
        return organismDao;
    }

    public void setOrganismDao(OrganismDao organismDao) {
        this.organismDao = organismDao;
    }
    
    public void setAsOrganism(String org) {
        Organism o = organismDao.getOrganismByCommonName(org);
        setValue(o);
    }
}
