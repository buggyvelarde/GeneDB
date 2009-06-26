package org.genedb.db.loading.auxiliary;

import java.sql.SQLException;

public class ClearDGPI extends Clear {
    public static void main(String[] args) throws Exception {
        Clear.main(ClearDGPI.class, args);
    }

    ClearDGPI(String organismCommonName, String analysisProgram) throws ClassNotFoundException, SQLException {
        super(organismCommonName, analysisProgram);
    }

    private static final String DELETE_PROPS_SQL
        = "delete from featureprop"
        +" using cvterm join cv using (cv_id)"
        +"     , feature join organism using (organism_id)"
        +" where featureprop.type_id = cvterm.cvterm_id"
        +" and   featureprop.feature_id = feature.feature_id"
        +" and cv.name = 'genedb_misc'"
        +" and cvterm.name = 'GPI_anchored'"
        +" and organism.common_name = ?";

    private static final String DELETE_CLEAVAGE_SITE_FEATURES_SQL
    = "delete from feature"
    +" using cvterm join cv using (cv_id)"
    +"     , organism"
    +" where feature.type_id = cvterm.cvterm_id"
    +" and   feature.organism_id = organism.organism_id"
    +" and cv.name = 'genedb_feature_type'"
    +" and cvterm.name = 'GPI_anchor_cleavage_site'"
    +" and organism.common_name = ?";


    @Override
    protected DeleteSpec[] getDeleteSpecs() {
        return new DeleteSpec[] {
            new DeleteSpec("GPI_anchored feature properties", DELETE_PROPS_SQL),
            new DeleteSpec("GPI anchor cleavage site features", DELETE_CLEAVAGE_SITE_FEATURES_SQL),
        };
    }
}
