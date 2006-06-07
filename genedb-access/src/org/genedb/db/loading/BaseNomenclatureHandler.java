package org.genedb.db.loading;


import org.biojava.bio.Annotation;


/**
 * Helper base class for writing NomenclatureHandlers. It first checks if the feature 
 * is using standard nomenclature - if so it delegates to a StandardNomenclatureHandler, 
 * otherwise it calls the findNamesInternal method in the subclass.  
 * 
 * @author art
 */
public abstract class BaseNomenclatureHandler implements NomenclatureHandler {

    private StandardNomenclatureHandler standardNomenclatureHandler;
    
    public Names findNames(Annotation an) {
	
        String schemeTest = MiningUtils.getProperty("systematic_id", an, null);
        if (schemeTest != null) {
            return this.standardNomenclatureHandler.findNames(an);
        }
	
        return findNamesInternal(an);
    }
    
    /**
     * This method behaves the same as the findNames method, but is only called once it
     * is established that we're not using standard naming.
     * 
     * @param an The biojava annotation for the feature
     * @return The filled out Names object
     */
    public abstract Names findNamesInternal(Annotation an);
    

    public void setStandardNomenclatureHandler(StandardNomenclatureHandler standardNomenclatureHandler) {
        this.standardNomenclatureHandler = standardNomenclatureHandler;
    }

}
