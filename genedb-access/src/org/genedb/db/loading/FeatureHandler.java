package org.genedb.db.loading;

import org.genedb.db.dao.DaoFactory;
import org.genedb.db.hibernate3gen.Organism;
import org.genedb.db.jpa.Feature;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.utils.ChangeVetoException;

public interface FeatureHandler {

    public void setDaoFactory(DaoFactory daoFactory);
    
    public void setOrganism(Organism organism);
    
    public abstract Feature processSources(Sequence seq)
	    throws ChangeVetoException, BioException;

    public abstract void processCDS(Sequence seq, Feature topLevel, int offset);

    public void setFeatureUtils(FeatureUtils utils);

    public void setNomenclatureHandler(NomenclatureHandler handler);
    
    public void afterPropertiesSet();

}