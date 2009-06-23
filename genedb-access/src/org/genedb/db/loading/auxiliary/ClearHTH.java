package org.genedb.db.loading.auxiliary;

import java.sql.SQLException;

/* Deletes any existing helix-turn-helix features for the chosen organism */

public class ClearHTH extends Clear {
    public static void main(String[] args) throws Exception {
        Clear.main(ClearHTH.class, args);
    }

    ClearHTH(String organismCommonName) throws ClassNotFoundException, SQLException {
        super(organismCommonName);
    }

  
    private static final String DELETE_HTH_FEATURES_SQL
        = "delete from feature"
        +" using cvterm feature_type"
        +" join cv feature_type_cv using (cv_id)"
        +" join dbxref feature_type_dbxref using (dbxref_id)"
        +"    , organism"
        +" where feature_type.cvterm_id = feature.type_id"
        +" and feature_type_dbxref.accession = '0001081'" //helix-turn-helix
        +" and feature_type_cv.name = 'sequence'"
        +" and feature.organism_id = organism.organism_id"
        +" and organism.common_name = ?";
    
   
    @Override
    protected DeleteSpec[] getDeleteSpecs() {
        return new DeleteSpec[] {
            new DeleteSpec("Helix turn helix features", DELETE_HTH_FEATURES_SQL),
        };
    }
}
