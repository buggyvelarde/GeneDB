package org.genedb.db.loading.auxiliary;

import java.sql.Connection;
import java.sql.SQLException;

/* Deletes any existing helix-turn-helix features for the chosen organism */

public class ClearHTH extends Clear {
    public static void main(String[] args) throws Exception {
        Clear.main(ClearHTH.class, args);
    }

    ClearHTH(String organismCommonName, String analysisProgram) throws ClassNotFoundException, SQLException {
        super(organismCommonName, analysisProgram);
    }
    
    ClearHTH(Connection conn, String organismCommonName, String analysisProgram) {
        super(conn, organismCommonName, analysisProgram);
    }
    
    
    private static final String DELETE_HTH_FEATURES_SQL
    = "delete from feature where feature_id in ("
        +" select feature.feature_id from feature"
        +" join organism on feature.organism_id = organism.organism_id"
        +" join cvterm on cvterm.cvterm_id = feature.type_id"
        +" join cv on cv.cv_id = cvterm.cv_id"
        +" join dbxref on dbxref.dbxref_id=cvterm.dbxref_id "
        +" and dbxref.accession = '0001081'" //Helix-turn-helix
        +" and cv.name = 'sequence'"
        +" and organism.common_name = ?)"; 
    
    
   
    @Override
    protected DeleteSpec[] getDeleteSpecs() {
        return new DeleteSpec[] {
            new DeleteSpec("Helix turn helix features", DELETE_HTH_FEATURES_SQL),
        };
    }
}
