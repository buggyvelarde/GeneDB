package org.genedb.db.loading.auxiliary;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClearDomains extends Clear {

    protected Set<String> getOptionNames() {
	Set<String> options = new HashSet<String>();
	Collections.addAll(options, "program");
        return options;
    }

    protected boolean processOption(String optionName, String optionValue) {

    	if (optionName.equals("program")) {
    		analysisProgram = optionValue;
	    	return true;
    	}
    	return false;
    }

    public static void main(String[] args) throws Exception {
        Clear.main(ClearDomains.class, args);
    }

    ClearDomains(String organismCommonName, String analysisProgram) throws ClassNotFoundException, SQLException {
        super(organismCommonName, analysisProgram);
    }

    ClearDomains(Connection conn, String organismCommonName, String analysisProgram) {
        super(conn, organismCommonName, analysisProgram);
    }

	private static final String DELETE_DOMAINS_SQL
    = "delete from feature"
        +" where feature_id in ("
        +" select feature.feature_id"
        +" from feature"
        +" join organism on feature.organism_id = organism.organism_id"
        +" join cvterm feature_type on feature_type.cvterm_id = feature.type_id"
        +" join cv feature_type_cv on feature_type.cv_id = feature_type_cv.cv_id"
	+" join analysisfeature on analysisfeature.feature_id = feature.feature_id"
	+" join analysis on analysis.analysis_id = analysisfeature.analysis_id"
        +" where feature_type.name = 'polypeptide_domain'"
        +" and feature_type_cv.name = 'sequence'"
	+" and program = ?"
        +" and organism.common_name = ?)";

    private static final String DELETE_PFAM_GO_TERMS_SQL
    = "delete from feature_cvterm"
        +" where feature_cvterm_id in ("
        +" select feature_cvterm.feature_cvterm_id"
        +" from feature_cvterm"
        +" join feature_cvtermprop on feature_cvterm.feature_cvterm_id = feature_cvtermprop.feature_cvterm_id"
        +" join cvterm prop_type on feature_cvtermprop.type_id = prop_type.cvterm_id"
        +" join feature on feature_cvterm.feature_id = feature.feature_id"
        +" join organism on feature.organism_id = organism.organism_id"
        +" where prop_type.name = 'autocomment'"
        +" and feature_cvtermprop.value = 'From Pfam2GO mapping'"
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
        +" and feature_cvtermprop.value = 'From iprscan'"
        +" and organism.common_name = ?)";

    @Override
    protected DeleteSpec[] getDeleteSpecs() {

    	if (analysisProgram.equals("pfam_scan")) {
    		return new DeleteSpec[] {
    				new DeleteSpec("polypeptide domains", DELETE_DOMAINS_SQL, 2),
    				new DeleteSpec("Pfam GO terms",   DELETE_PFAM_GO_TERMS_SQL, 1),
    		};
    	}
    	else if (analysisProgram.equals("iprscan")) {
    		return new DeleteSpec[] {
    				new DeleteSpec("polypeptide domains", DELETE_DOMAINS_SQL, 2),
    				new DeleteSpec("InterPro GO terms",   DELETE_INTERPRO_GO_TERMS_SQL, 1),
    		};
    	}

    	return new DeleteSpec[] {
    		new DeleteSpec("polypeptide domains", DELETE_DOMAINS_SQL, 2),
    	};

    }
}
