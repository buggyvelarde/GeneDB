package org.genedb.db.loading.auxiliary;

import java.sql.SQLException;

public class ClearTMHMM extends Clear {
    public static void main(String[] args) throws Exception {
        Clear.main(ClearTMHMM.class, args);
    }

    ClearTMHMM(String organismCommonName, String analysisProgram)
    		throws ClassNotFoundException, SQLException {
        super(organismCommonName, analysisProgram);
    }
    
    //delete membrane_structure_regions from the tmhmm analysis
    private static final String DELETE_MEMBRANE_STRUCTURE_FEATURES_SQL
    = "delete from feature"
    +" using cvterm feature_type"
    +" join cv feature_type_cv using (cv_id)"
    +" join dbxref feature_type_dbxref using (dbxref_id)"
    +"    , organism"
    +" where feature_type.cvterm_id = feature.type_id"
    +" and feature_type_dbxref.accession = '0001071'" // membrane structure region
    +" and feature_id in ("
    +"      select feature_id from analysisfeature join analysis using (analysis_id) where program = 'tmhmm'"
    +" )"
    +" and feature_type_cv.name = 'sequence'"
    +" and feature.organism_id = organism.organism_id"
    +" and organism.common_name = ?";
 

    //delete transmembrane, cytoplasmic and non-cytoplasmic region features
    //that are not part_of a membrane_structure_region from the tmhmm analysis
    private static final String DELETE_TRANSMEMBRANE_FEATURES_SQL
        = "delete from feature"
        +" using cvterm feature_type"
        +" join cv feature_type_cv using (cv_id)"
        +" join dbxref feature_type_dbxref using (dbxref_id)"
        +"    , organism"
        +" where feature_type.cvterm_id = feature.type_id"
        +" and feature_type_dbxref.accession in ("
        +"     '0001077'" // transmembrane region
        +"   , '0001073'" // cytoplasmic region
        +"   , '0001074'" // non-cytoplasmic region
        +" )"
        +" and feature_id not in ("
        +"      select subject_id from feature_relationship " 
        +"		    where type_id in ("
        +"              select cvterm_id from cvterm where name = 'part_of'"
        +"          )"
        +"          and object_id in ("
        +"              select feature_id from feature join cvterm on feature.type_id = cvterm.cvterm_id and cvterm.dbxref_id in ( "
        +"                   select dbxref_id from dbxref where accession = '0001071'"
        +"              )"
        +"    		)"
        +" )"
        +" and feature_type_cv.name = 'sequence'"
        +" and feature.organism_id = organism.organism_id"
        +" and organism.common_name = ?";

    
    @Override
    protected DeleteSpec[] getDeleteSpecs() {
        return new DeleteSpec[] {
            new DeleteSpec("membrane structures", DELETE_MEMBRANE_STRUCTURE_FEATURES_SQL),        		
            new DeleteSpec("transmembrane helices", DELETE_TRANSMEMBRANE_FEATURES_SQL),
        };
    }
}
