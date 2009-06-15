package org.genedb.db.loading.auxiliary;

import java.sql.Connection;
import java.sql.SQLException;

public class ClearPfam extends Clear {
    public static void main(String[] args) throws Exception {
        Clear.main(ClearPfam.class, args);
    }

    ClearPfam(String organismCommonName) throws ClassNotFoundException, SQLException {
        super(organismCommonName);
    }

    ClearPfam(Connection conn, String organismCommonName) {
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
	+" join analysisfeature using (feature_id)"
	+" join analysis using (analysis_id)"
        +" where feature_type.name = 'polypeptide_domain'"
        +" and feature_type_cv.name = 'sequence'"
	+" and program = 'pfam_scan'"
        +" and organism.common_name = ?)";

    @Override
    protected DeleteSpec[] getDeleteSpecs() {
        return new DeleteSpec[] {
            new DeleteSpec("polypeptide domains", DELETE_DOMAINS_SQL),
        };
    }
}
