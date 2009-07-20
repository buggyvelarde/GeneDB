package org.genedb.web.mvc.model.load;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.genedb.web.mvc.model.types.DtoStringArrayField;
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
    
    private OrganismMapper organismMapper;
    private FeatureMapper geneMapper;
    
    private SimpleJdbcTemplate template;
    
    public TranscriptMapper(OrganismMapper organismMapper, FeatureMapper geneMapper, SimpleJdbcTemplate template){
        this.organismMapper = organismMapper;
        this.geneMapper = geneMapper;
        this.template = template;
    }
    
    @Override
    public FeatureMapper mapRow(ResultSet rs, int rowNum) throws SQLException {
        logger.debug("Enter mapRow");
        
        HashMap<String, Object> args = new HashMap<String, Object>();
        
        //Get transcript details 
        FeatureMapper transcriptMapper = super.mapRow(rs, rowNum);          
        transcriptMapper.setCvtName(rs.getString("cvtname"));
        transcriptMapper.setCvName(rs.getString("cvname"));
        
        //Init the Organism arguments of this transcript
        initOrganismArguments(args);
        
        //Init the gene arguments of this transcript
        initGeneArguments(args);
              
        //Init the transcript arguments
        initTranscriptArguments(args, transcriptMapper);
        
        if (isProductiveTranscript(transcriptMapper)){
            FeatureMapper polypeptideMapper = template.queryForObject(PolypeptideMapper.SQL, new PolypeptideMapper(), transcriptMapper.getFeatureId());            
            initPolypeptideArguments(args, polypeptideMapper);            
        }        
        
        
        //Init the toplevelfeature arguments of this transcript
        FeatureMapper topLevelFeatureMapper = template.queryForObject(
                "select * from feature f where feature_id = ?", new FeatureMapper(), geneMapper.getSourceFeatureId());
        initTopLevelArguments(args, topLevelFeatureMapper);        
        
        insertDenormalisedTranscript(args);
        
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
    
    private void initOrganismArguments(HashMap<String, Object> args){ 
        logger.debug("Enter initOrganismArguments");
        args.put("organism_common_name", organismMapper.getCommonName());
        args.put("organism_id", organismMapper.getOrganismId());    
        logger.debug("Exit initOrganismArguments");    
    }
    
    private void initGeneArguments(HashMap<String, Object> args){
        logger.debug("Enter initGeneArguments");
        args.put("gene_name", geneMapper.getName());          
        args.put("gene_id", geneMapper.getFeatureId());    
        args.put("gene_time_last_modified", geneMapper.getTimeLastModified());
        args.put("fmax", geneMapper.getFmax());
        args.put("fmin", geneMapper.getFmin());
        args.put("strand", geneMapper.getStrand());
        logger.debug("Exit initGeneArguments");
    }
    
    private void initTopLevelArguments(HashMap<String, Object> args, FeatureMapper topLevelFeature){ 
        logger.debug("Enter initTopLevelArguments");       
        args.put("top_level_feature_displayname", topLevelFeature.getDisplayName());
        args.put("top_level_feature_length", topLevelFeature.getSeqLen());
        //args.put("top_level_feature_type", dto.getTopLevelFeatureType());
        //args.put("type_description", dto.getTypeDescription());
        args.put("top_level_feature_uniquename", topLevelFeature.getUniqueName());     
        logger.debug("Exit initTopLevelArguments");          
    }

    private void initTranscriptArguments(HashMap<String, Object> args, FeatureMapper transcriptMapper){
        logger.debug("Enter initTranscriptArguments");
        args.put("transcript_id", transcriptMapper.getFeatureId());
        args.put("organism_id", transcriptMapper.getOrganismId());        
        args.put("time_last_modified", transcriptMapper.getTimeLastModified());
        args.put("uniquename", transcriptMapper.getUniqueName());      
        args.put("proper_name", transcriptMapper.getName());  
        args.put("type_description", transcriptMapper.getCvtName());
        
        if (isProductiveTranscript(transcriptMapper)){
            args.put("pseudo", "pseudogenic_transcript".equals(transcriptMapper.getCvtName()));
        }else{
            args.put("pseudo", false);
        }
        logger.debug("Exit initTranscriptArguments");
    }
    
    private void initPolypeptideArguments(HashMap<String, Object> args, FeatureMapper polypeptideMapper){
        logger.debug("Enter initPolypeptideArguments");

        if(polypeptideMapper!= null){
            args.put("protein_coding", true);
            args.put("polypeptide_time_last_modified", polypeptideMapper.getTimeLastModified());

            //Get the comments for polypeptide
            List<String> comments = new ArrayList<String>();
            List<FeaturePropMapper> props = template.query(
                    FeaturePropMapper.SQL, new FeaturePropMapper(), polypeptideMapper.getFeatureId(), "comment", "feature_property");
            for(FeaturePropMapper prop : props){
                comments.add(prop.getValue());
            }
            args.put("pep_comments", new DtoStringArrayField(comments));
            
            //Get the curation for polypeptide
            List<String> curation = new ArrayList<String>();
            props = template.query(
                    FeaturePropMapper.SQL, new FeaturePropMapper(), polypeptideMapper.getFeatureId(), "curation", "genedb_misc");
            for(FeaturePropMapper prop : props){
                curation.add(prop.getValue());
            }
            args.put("pep_curation", new DtoStringArrayField(curation));
        }
        logger.debug("Exit initPolypeptideArguments");
    }
    
    private boolean isProductiveTranscript(FeatureMapper transcriptMapper){
        logger.debug("Enter isProductiveTranscript");
        if("sequence".equals(transcriptMapper.getCvName())){
            if ("mRNA".equals(transcriptMapper.getCvtName())
                    || "pseudogenic_transcript".equals(transcriptMapper.getCvtName())){
               return true;                              
            }
        }
        return false;
    }
    
    
    private int insertDenormalisedTranscript(HashMap<String, Object> args){   
        logger.debug("Enter insertDenormalisedTranscript");
        for(String key : args.keySet()){
            logger.debug(String.format("%s: %s", key, args.get(key)));
        }
        logger.debug(String.format("Field args size: %s", args.size()));
        return template.update("insert into transcript_cache values(" +
                ":transcript_id," +
                ":gene_id," +
                ":gene_name," +
                ":gene_time_last_modified," +
                ":fmax," +
                ":fmin," +
                ":strand," +
                ":organism_id," +
                ":organism_common_name," +
//                ":organism_html_short_name," +
                ":proper_name," +
                ":protein_coding," +
                ":pseudo," +
                ":top_level_feature_displayname," +
                ":top_level_feature_length," +
 //               ":top_level_feature_type," +
                ":top_level_feature_uniquename," +
                ":type_description," +
                ":uniquename," +
                
//                ":cluster_ids," +
                ":pep_comments," +
                ":pep_curation" +
//                ":obsolete_names," +
//                ":orthologue_names," +
//                ":publications," +
//                ":synonyms," +
                
//                ":polypeptide_properties," +
//                ":dbx_ref_dtos," +
//                ":algorithm_data," +
//                ":domain_information," +
//                ":synonyms_by_types" +
                ") ",  args);
            
//            int transcriptId = dto.getTranscriptId();
//            updateFeatureCvtermDTO(transcriptId, dto.getControlledCurations());
//            updateFeatureCvtermDTO(transcriptId, dto.getGoBiologicalProcesses());
//            updateFeatureCvtermDTO(transcriptId, dto.getGoCellularComponents());
//            updateFeatureCvtermDTO(transcriptId, dto.getGoMolecularFunctions());
//            updateFeatureCvtermDTO(transcriptId, dto.getProducts());
    }
}
