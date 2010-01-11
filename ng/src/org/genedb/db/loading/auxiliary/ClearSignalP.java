package org.genedb.db.loading.auxiliary;

import java.sql.SQLException;

public class ClearSignalP extends Clear {
    public static void main(String[] args) throws Exception {
        Clear.main(ClearSignalP.class, args);
    }

    ClearSignalP(String organismCommonName, String analysisProgram) throws ClassNotFoundException, SQLException {
        super(organismCommonName, analysisProgram);
    }

    private static final String DELETE_PROPS_SQL
        = "delete from featureprop"
        +" using cvterm prop_type join cv prop_type_cv using (cv_id)"
        +"    ,  feature join organism using (organism_id)"
        +" where featureprop.type_id = prop_type.cvterm_id"
        +" and feature.feature_id = featureprop.feature_id"
        +" and prop_type_cv.name = 'genedb_misc'"
        +" and prop_type.name in ("
        +"       'SignalP_prediction'"
        +"     , 'signal_peptide_probability'"
        +"     , 'signal_anchor_probability'"
        +" )"
        +" and organism.common_name = ?";

    private static final String DELETE_SIGNAL_PEPTIDE_FEATURES_SQL
        = "delete from feature"
        +" using cvterm feature_type"
        +" join cv feature_type_cv using (cv_id)"
        +" join dbxref feature_type_dbxref using (dbxref_id)"
        +"    , organism"
        +" where feature_type.cvterm_id = feature.type_id"
        +" and feature_type_dbxref.accession = '0000418'" // signal_peptide
        +" and feature_type_cv.name = 'sequence'"
        +" and feature.organism_id = organism.organism_id"
        +" and organism.common_name = ?";


    @Override
    protected DeleteSpec[] getDeleteSpecs() {
        return new DeleteSpec[] {
            new DeleteSpec("SignalP feature properties", DELETE_PROPS_SQL),
            new DeleteSpec("Signal peptide features", DELETE_SIGNAL_PEPTIDE_FEATURES_SQL),
        };
    }
}
