package org.genedb.db.loading;

import org.biojava.bio.Annotation;

import java.util.Map;

/**
 * Interface to define classes that can parse naming
 *  information (systematic id, synonyms etc) from a CDS's annotation.
 * 
 * @author art
 */
public interface NomenclatureHandler {

    /**
     * Interface for an object which looks through the annotation to 
     * work out the names.
     * 
     * @param an The biojava annotation from a feature
     *
     * @return the filled out Names object
     */
    Names findNames(Annotation an);

    void setOptions(Map<String, String> nomenclatureOptions);

}
