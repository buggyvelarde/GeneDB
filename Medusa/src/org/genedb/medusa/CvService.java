package org.genedb.medusa;

import org.gmod.schema.mapped.CvTerm;

public interface CvService {

    CvTerm findCvTermByCvAndName(String string, String string2);

}
