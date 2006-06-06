package org.genedb.db.loading;

import org.biojava.bio.Annotation;

/**
 * Interface to define classes that can parse naming
 *  information from a CDS's annotation.
 * 
 * @author art
 */
public interface NomenclatureHandler {

    Names findNames(Annotation an);

}
