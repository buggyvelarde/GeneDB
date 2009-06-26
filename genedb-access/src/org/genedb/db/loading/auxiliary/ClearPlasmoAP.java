package org.genedb.db.loading.auxiliary;

import java.sql.SQLException;

public class ClearPlasmoAP extends Clear {
    public static void main(String[] args) throws Exception {
        Clear.main(ClearPlasmoAP.class, args);
    }

    ClearPlasmoAP(String organismCommonName, String analysisProgram) throws ClassNotFoundException, SQLException {
        super(organismCommonName, analysisProgram);
    }

    private static final String DELETE_PLASMOAP_SCORES_SQL
        = "delete from featureprop"
        +" using cvterm join cv using (cv_id)"
        +"     , feature join organism using (organism_id)"
        +" where featureprop.type_id = cvterm.cvterm_id"
        +" and   featureprop.feature_id = feature.feature_id"
        +" and cv.name = 'genedb_misc'"
        +" and cvterm.name = 'PlasmoAP_score'"
        +" and organism.common_name = ?";

    @Override
    protected DeleteSpec[] getDeleteSpecs() {
        return new DeleteSpec[] {
            new DeleteSpec("PlasmoAP scores", DELETE_PLASMOAP_SCORES_SQL),
        };
    }
}


