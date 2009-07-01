package org.genedb.db.loading.auxiliary;

import java.sql.SQLException;

/**
 * Deletes any existing rfam results for this organism. At the momemt, we do a text search on the uniquename (Does it have rfam somewhere?)
 * to look for relevant genes and transcripts. Investigate a better way to do.  
 * 
 * @author nds
 * */

public class ClearRfam extends Clear {
    public static void main(String[] args) throws Exception {
        Clear.main(ClearRfam.class, args);
    }

    ClearRfam(String organismCommonName, String analysisProgram) throws ClassNotFoundException, SQLException {
        super(organismCommonName, analysisProgram);
    }

  
    private static final String DELETE_RFAM_FEATURES_SQL
        = "delete from feature"
        +" using cvterm feature_type"
        +" join cv feature_type_cv using (cv_id)"
        +" join dbxref feature_type_dbxref using (dbxref_id)"
        +"    , organism"
        +" where feature_type.cvterm_id = feature.type_id"
        +" and feature_type_cv.name = 'sequence'"
        +" and feature.uniquename LIKE '%rfam%'"
        +" and feature.organism_id = organism.organism_id"
        +" and organism.common_name = ?";
    
   
    @Override
    protected DeleteSpec[] getDeleteSpecs() {
        System.out.println(DELETE_RFAM_FEATURES_SQL);
        return new DeleteSpec[] {
               
            new DeleteSpec("Rfam features", DELETE_RFAM_FEATURES_SQL),
        };
    }
}
