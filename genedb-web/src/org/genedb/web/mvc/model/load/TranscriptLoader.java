package org.genedb.web.mvc.model.load;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.genedb.web.mvc.model.types.DtoStringArrayField;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
/**
 * 
 * @author lo2@sangerinstitute
 *
 */
@Transactional
public class TranscriptLoader {
    
    Logger logger = Logger.getLogger(TranscriptLoader.class);

    private SimpleJdbcTemplate template;
    
    public static void main(String args[]){
        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] {"TranscriptLoaderTest-context.xml.xml"});
        TranscriptLoader transcriptLoader = ctx.getBean("transcriptLoader", TranscriptLoader.class);
        transcriptLoader.load("Tbruceibrucei427", 10);
    }
    
    public void loadAll(){           
        template.query(GeneMapper.SQL, new GeneMapper(template));
    
    }
    
    /**
     * Choose organism to load
     * @param organismName
     * @param limit
     * @param offset
     */
    public void load(String organismName, int limit){
        logger.debug(String.format("Enter load(%s)", organismName));
        
        Set<Integer> temp = new HashSet<Integer>();

        //Get the organism
        OrganismMapper organismMapper = template.queryForObject(
               OrganismMapper.SQL_WITH_PARAMS, new OrganismMapper(), organismName);
        
        int offset = 1;
              
        List<FeatureMapper> genes = null;
        do{
            logger.info(String.format("Offset is %s and Limit is %s", offset, limit));
            
            //Create the mapper and get the genes
            genes = template.query(
                    GeneMapper.SQL_WITH_LIMIT_AND_OFFSET_PARAMS, 
                    new GeneMapper(template), organismMapper.getOrganismId(), limit, offset);
            logger.info("Genes size: " + genes.size());            
            for(FeatureMapper geneMapper: genes){          
                
                //Init the toplevelfeature arguments of this transcript
                FeatureMapper topLevelFeatureMapper = template.queryForObject(
                        "select * from feature f where feature_id = ?", new FeatureMapper(), geneMapper.getSourceFeatureId());                          
                
                //get the transcripts
                List<FeatureMapper>transcriptMappers = template.query(
                        TranscriptMapper.SQL, new TranscriptMapper(template), geneMapper.getFeatureId());
                logger.info("Transcripts size: " + transcriptMappers.size());
                for( FeatureMapper transcriptMapper: transcriptMappers){
                    
                    if(temp.contains(transcriptMapper.getFeatureId())){
                        logger.info(transcriptMapper.getFeatureId() + " is duplicated");
                    }
                    logger.info("Adding..." + transcriptMapper.getFeatureId());
                    temp.add(transcriptMapper.getFeatureId());
                    
                    HashMap<String, Object> args = new HashMap<String, Object>();
                    
                    //Init the Organism arguments of this transcript
                    initOrganismArguments(args, organismMapper);
                    
                    //Init the gene arguments of this transcript
                    initGeneArguments(args, geneMapper);
                          
                    //Init the transcript arguments
                    initTranscriptArguments(args, transcriptMapper);
                    
                    if (isProductiveTranscript(transcriptMapper)){
                        FeatureMapper polypeptideMapper = template.queryForObject(
                                PolypeptideMapper.SQL, new PolypeptideMapper(), transcriptMapper.getFeatureId());
                        
                        initPolypeptideArguments(args, polypeptideMapper);            
                    } 
                    initTopLevelArguments(args, topLevelFeatureMapper);        
                    
                    insertDenormalisedTranscript(args);
                    
                }
            }
            
            //increase the offset
            offset = offset + limit;
            
        }while(genes!= null && limit <= genes.size());
            
        logger.debug(String.format("Exit load(%s)", organismName));
    }
    
    private void initOrganismArguments(HashMap<String, Object> args, OrganismMapper organismMapper){ 
        logger.debug("Enter initOrganismArguments");
        args.put("organism_common_name", organismMapper.getCommonName());
        args.put("organism_id", organismMapper.getOrganismId());    
        logger.debug("Exit initOrganismArguments");    
    }
    
    private void initGeneArguments(HashMap<String, Object> args, FeatureMapper geneMapper){
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
        logger.debug("\n<<<<<<<<<<<");
        for(String key : args.keySet()){
            logger.debug(String.format("%s: %s", key, args.get(key)));
        }
        logger.debug("\n");
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

    public SimpleJdbcTemplate getTemplate() {
        return template;
    }

    public void setTemplate(SimpleJdbcTemplate template) {
        this.template = template;
    }
}
