package org.genedb.web.mvc.model.load;

import java.util.HashMap;
import java.util.List;

import org.genedb.web.mvc.model.types.DBXRefType;
import org.genedb.web.mvc.model.types.DtoObjectArrayField;
import org.genedb.web.mvc.model.types.DtoStringArrayField;
import org.genedb.web.mvc.model.types.FeatureCVTPropType;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class TranscriptFeatureCVTermLoader {
    
    public static void load(FeatureMapper pep, String cvNamePrefix, SimpleJdbcTemplate template){
        HashMap<String, Object> args = new HashMap<String, Object>();
        
        List<FeatureCvtermMapper> featureCvtermMappers= 
            template.query(FeatureCvtermMapper.SQL, new FeatureCvtermMapper(), cvNamePrefix, pep.getFeatureId());
        
        //Get the Feature CVTerm Properties
        for(FeatureCvtermMapper mapper : featureCvtermMappers){            
            
            //Get the Name
            String cvTermName = (String)template.queryForObject(
                    "select name from cvterm where cvterm_id = ?",
                    String.class,
                    new Object[]{new Integer(mapper.getTypeId())} );
            args.put("cvTermName", cvTermName);
            
            //Get the Accession
            String typeAccession = (String)template.queryForObject(
                    " select accession " +
                    " from cvterm cvt, dbxref dbx " +
                    " where cvt.dxref_id = dbx.dbxref_id " +
                    " and cvt.cvterm_id = ?",
                    String.class,
                    new Object[]{new Integer(mapper.getTypeId())});
            args.put("typeAccession", typeAccession);
            
            //Get the withFrom
            String withFrom = (String)template.queryForObject(
                    "select uniquename from pub where pub_id = ?",
                    String.class,
                    new Object[]{new Integer(mapper.getPubId())});
            args.put("withFrom", withFrom);
            
            //Get the feature cvterm count
            int featureCvtermCount = template.queryForInt(
                    " select count(f.*)" +
                    " from feature f, feature_cvterm fcvt" +
                    " where fcvt.cvterm_id = ?" +
                    " and f.organism_id = ?" +
                    " and f.feature_id = fcvt.feature_id",
                    mapper.getTypeId(), pep.getOrganismId());
            args.put("featureCvtermCount", featureCvtermCount);
                      
            //Get publications
            List<String> pubNames = template.query(
                    PubNameMapper.FEATURE_CVTERM_SQL, new PubNameMapper(), mapper.getFeatureCvtId());
            args.put("pubs", new DtoStringArrayField(pubNames));
            
            //Get the dbxref
            List<DBXRefType> dbxRefs = template.query(
                    DbxRefMapper.FEATURE_CVTERM_SQL, new DbxRefMapper(), mapper.getFeatureCvtId());                        
            DtoObjectArrayField objectField = new DtoObjectArrayField("dbxreftype", dbxRefs);
            args.put("dbxref", objectField);            
            
            //Get the pros
            List<FeatureCVTPropType> props = template.query(
                    FeatureCVTermPropMapper.SQL, new FeatureCVTermPropMapper(), mapper.getFeatureCvtId());                        
            objectField = new DtoObjectArrayField("featurecvtproptype", props);
            args.put("props", objectField);            
            
            
        }
    }
}
