package org.genedb.db.loading.auxiliary;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClearDomains extends Clear {
	
	String analysisProgram;
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
	+" and program = ?"
        +" and organism.common_name = ?)";

    @Override
    protected DeleteSpec[] getDeleteSpecs() {
        return new DeleteSpec[] {
            new DeleteSpec("polypeptide domains", DELETE_DOMAINS_SQL),
        };
    }
}
