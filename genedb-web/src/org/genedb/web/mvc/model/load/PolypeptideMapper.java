package org.genedb.web.mvc.model.load;

import org.apache.log4j.Logger;



public class PolypeptideMapper extends FeatureMapper {
    private Logger logger = Logger.getLogger(PolypeptideMapper.class);

    public static final String SQL = "select f.* " +
    		" from feature f, feature_relationship fr, cvterm cvt, cv" +
    		" where fr.object_id = ?" +
    		" and fr.subject_id = f.feature_id" +
    		" and f.type_id = cvt.cvterm_id" +
    		" and cvt.name = 'polypeptide'" +
    		" and cv.name = 'sequence'";
}
