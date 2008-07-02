package org.genedb.db.loading.polypeptide;

import java.sql.SQLException;

public class ClearInterPro extends Clear {
    public static void main(String[] args) throws Exception {
        Clear.main(ClearInterPro.class, args);
    }

    ClearInterPro(String organismCommonName) throws ClassNotFoundException, SQLException {
        super(organismCommonName);
    }

    private static final String DELETE_DOMAINS_SQL
    = "delete from feature"
        +" using cvterm feature_type"
        +" join cv feature_type_cv using (cv_id)"
        +"    , organism"
        +" where feature_type.cvterm_id = feature.type_id"
        +" and feature_type.name = 'polypeptide_domain'"
        +" and feature_type_cv.name = 'sequence'"
        +" and feature.organism_id = organism.organism_id"
        +" and organism.common_name = ?";

    private static final String DELETE_INTERPRO_GO_TERMS_SQL
        = "delete from feature_cvterm"
            +" using feature_cvtermprop"
            +"     join cvterm prop_type on feature_cvtermprop.type_id = prop_type.cvterm_id"
            +" , feature join organism using (organism_id)"
            +" where feature_cvterm.feature_cvterm_id = feature_cvtermprop.feature_cvterm_id"
            +" and feature_cvterm.feature_id = feature.feature_id"
            +" and prop_type.name = 'autocomment'"
            +" and feature_cvtermprop.value = 'From Interpro file'"
            +" and organism.common_name = ?";

    @Override
    protected DeleteSpec[] getDeleteSpecs() {
        return new DeleteSpec[] {
            new DeleteSpec("polypeptide domains", DELETE_DOMAINS_SQL),
            new DeleteSpec("InterPro GO terms",   DELETE_INTERPRO_GO_TERMS_SQL),
        };
    }
}
