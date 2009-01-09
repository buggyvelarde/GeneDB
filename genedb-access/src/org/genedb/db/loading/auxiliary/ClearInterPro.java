package org.genedb.db.loading.auxiliary;

import java.sql.Connection;
import java.sql.SQLException;

public class ClearInterPro extends Clear {
    public static void main(String[] args) throws Exception {
        Clear.main(ClearInterPro.class, args);
    }

    ClearInterPro(String organismCommonName) throws ClassNotFoundException, SQLException {
        super(organismCommonName);
    }

    ClearInterPro(Connection conn, String organismCommonName) {
        super(conn, organismCommonName);
    }

    private static final String DELETE_DOMAINS_SQL
    = "delete from feature"
        +" where feature_id in ("
        +" select feature.feature_id"
        +" from feature"
        +" join organism on feature.organism_id = organism.organism_id"
        +" join cvterm feature_type on feature_type.cvterm_id = feature.type_id"
        +" join cv feature_type_cv on feature_type.cv_id = feature_type_cv.cv_id"
        +" where feature_type.name = 'polypeptide_domain'"
        +" and feature_type_cv.name = 'sequence'"
        +" and organism.common_name = ?)";

    private static final String DELETE_INTERPRO_GO_TERMS_SQL
        = "delete from feature_cvterm"
            +" where feature_cvterm_id in ("
            +" select feature_cvterm.feature_cvterm_id"
            +" from feature_cvterm"
            +" join feature_cvtermprop on feature_cvterm.feature_cvterm_id = feature_cvtermprop.feature_cvterm_id"
            +" join cvterm prop_type on feature_cvtermprop.type_id = prop_type.cvterm_id"
            +" join feature on feature_cvterm.feature_id = feature.feature_id"
            +" join organism on feature.organism_id = organism.organism_id"
            +" where prop_type.name = 'autocomment'"
            +" and feature_cvtermprop.value = 'From Interpro file'"
            +" and organism.common_name = ?)";

    @Override
    protected DeleteSpec[] getDeleteSpecs() {
        return new DeleteSpec[] {
            new DeleteSpec("polypeptide domains", DELETE_DOMAINS_SQL),
            new DeleteSpec("InterPro GO terms",   DELETE_INTERPRO_GO_TERMS_SQL),
        };
    }
}
