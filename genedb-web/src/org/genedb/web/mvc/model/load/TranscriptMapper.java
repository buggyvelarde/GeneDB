package org.genedb.web.mvc.model.load;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import common.Logger;

public class TranscriptMapper extends FeatureMapper {
    Logger logger = Logger.getLogger(TranscriptMapper.class);
    
    public static final String SQL = " select f.*, cvt.name as cvtname, cv.name as cvname " +
    "from feature f, feature_relationship fr, cvterm cvt, cv " +
    " where fr.subject_id = f.feature_id " +
    " and f.type_id = cvt.cvterm_id" +
    " and cvt.cv_id = cv.cv_id" +    
    " and fr.object_id = ?";
    
    
    private SimpleJdbcTemplate template;
    
    public TranscriptMapper(SimpleJdbcTemplate template){
        this.template = template;
    }
    
    @Override
    public FeatureMapper mapRow(ResultSet rs, int rowNum) throws SQLException {
        logger.debug("Enter mapRow");
        
        //Get transcript details 
        FeatureMapper transcriptMapper = super.mapRow(rs, rowNum);          
        transcriptMapper.setCvtName(rs.getString("cvtname"));
        transcriptMapper.setCvName(rs.getString("cvname"));
        
        logger.debug("Transcript Name: " + rs.getString("name"));
        
        //args.put("organism_html_short_name", rs.getInt("")dto.getOrganismHtmlShortName());
//        args.put("cluster_ids", new DtoStringArrayField(dto.getClusterIds()));


//        args.put("obsolete_names", new DtoStringArrayField(dto.getObsoleteNames()));
//        args.put("orthologue_names", new DtoStringArrayField(dto.getOrthologueNames()));
//        args.put("publications", new DtoStringArrayField(dto.getPublications()));
//        args.put("synonyms", new DtoStringArrayField(dto.getSynonyms()));
//        
//        args.put("dbx_ref_dtos", createArrayField(dto.getDbXRefDTOs(), DBXRefType.class, "dbxreftype")); 
//        args.put("algorithm_data", getBytes(dto.getAlgorithmData()));     
//        
//        if (dto.getPolypeptideProperties()!= null){
//            args.put("polypeptide_properties", new PeptidePropertiesType(dto.getPolypeptideProperties()));
//        }else{
//            args.put("polypeptide_properties", null);
//        }

        logger.debug("Exit mapRow");
        return transcriptMapper;
    }
}
