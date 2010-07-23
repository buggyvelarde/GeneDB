package org.genedb.db.loading.auxiliary;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Deletes any existing rfam results for the specified organism. At the moment, we do a text search on the uniquename (Does it have rfam somewhere?)
 * to look for relevant genes and transcripts. (Is there a better way to do this?)  
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
    
    ClearRfam(Connection conn, String organismCommonName, String analysisProgram) {
        super(conn, organismCommonName, analysisProgram);
    }

  
    private static final String DELETE_RFAM_FEATURES_SQL
    = "delete from feature"
        +" where feature_id in ("
        +" select feature.feature_id"
        +" from feature"
        +" join organism on feature.organism_id = organism.organism_id"
        +" join cvterm feature_type on feature_type.cvterm_id = feature.type_id"
        +" join cv feature_type_cv on feature_type.cv_id = feature_type_cv.cv_id"
        +" and feature_type_cv.name = 'sequence'"
        +" and feature.uniquename LIKE '%rfam%'"
        +" and organism.common_name = ?)";
    
  
   
    @Override
    protected DeleteSpec[] getDeleteSpecs() {
 
        return new DeleteSpec[] {
          new DeleteSpec("Rfam features", DELETE_RFAM_FEATURES_SQL),
        };
    }
}
