package org.genedb.web.mvc.model.load;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.genedb.web.mvc.model.types.DBXRefType;
import org.genedb.web.mvc.model.types.DtoObjectArrayField;
import org.genedb.web.mvc.model.types.DtoStringArrayField;
import org.genedb.web.mvc.model.types.PeptidePropertiesType;
import org.gmod.schema.utils.PeptideProperties;
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
    
    public static void main(String args[])throws Exception{
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
     * @return rows loaded
     */
    public int load(String organismName, int limit)throws Exception{
        logger.debug(String.format("Enter load(%s)", organismName));
        
        
        //Get the organism
        OrganismMapper organismMapper = template.queryForObject(
               OrganismMapper.SQL_WITH_PARAMS, new OrganismMapper(), organismName);
        
        int loadCount = 0;
        int offset = 1;
              
        List<FeatureMapper> genes = null;
        do{
            logger.info(String.format("Offset is %s and Limit is %s", offset, limit));
            
            //Create the mapper and get the genes
            genes = template.query(
                    GeneMapper.SQL_WITH_LIMIT_AND_OFFSET_PARAMS, 
                    new GeneMapper(template), organismMapper.getOrganismId(), limit, offset);
            logger.debug("Genes size: " + genes.size());            
            for(FeatureMapper geneMapper: genes){          
                
                //Init the toplevelfeature arguments of this transcript
                FeatureMapper topLevelFeatureMapper = template.queryForObject(
                        "select * from feature f where feature_id = ?", new FeatureMapper(), geneMapper.getSourceFeatureId());                          
                
                //get the transcripts
                List<FeatureMapper>transcriptMappers = template.query(
                        TranscriptMapper.SQL, new TranscriptMapper(template), geneMapper.getFeatureId());
                logger.info("Transcripts size: " + transcriptMappers.size());
                for( FeatureMapper transcriptMapper: transcriptMappers){                    
                   
                    logger.info("Adding..." + transcriptMapper.getFeatureId());
                    
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
                    
                    loadCount = loadCount + insertDenormalisedTranscript(args);
                    logger.info("Added..." + transcriptMapper.getFeatureId());
                }
            }
            
            //increase the offset
            offset = offset + limit;
            
        }while(genes!= null && limit <= genes.size());
            
        logger.debug(String.format("Exit load(%s)", organismName));
        return loadCount;
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
    
    private void initPolypeptideArguments(HashMap<String, Object> args, FeatureMapper polypeptideMapper)
    throws Exception{
        logger.debug("Enter initPolypeptideArguments");

        if(polypeptideMapper!= null){
            args.put("protein_coding", true);
            args.put("polypeptide_time_last_modified", polypeptideMapper.getTimeLastModified());

            //Get the comments for polypeptide
            List<String> props = template.query(
                    FeaturePropMapper.SQL, new FeaturePropMapper(), polypeptideMapper.getFeatureId(), "comment", "feature_property");            
            args.put("pep_comments", new DtoStringArrayField(props));
            
            //Get the curation for polypeptide
            props = template.query(
                    FeaturePropMapper.SQL, new FeaturePropMapper(), polypeptideMapper.getFeatureId(), "curation", "genedb_misc");            
            args.put("pep_curation", new DtoStringArrayField(props));
            
            //Get the dbxref details
            List<DBXRefType> dbxrefs = template.query(
                    DbxRefMapper.SQL, new DbxRefMapper(), polypeptideMapper.getFeatureId());
            DtoObjectArrayField objectField = new DtoObjectArrayField("dbxreftype", dbxrefs);
            args.put("dbx_refs", objectField);
            if(dbxrefs.size()>0){
                logger.info("DbxRef: " + objectField);
            }
            
            //Get publications
            List<String> pubNames = template.query(
                    PubNameMapper.SQL, new PubNameMapper(), polypeptideMapper.getFeatureId());
            args.put("publications", new DtoStringArrayField(pubNames));
            
            //Get polypeptide properties
            PeptideProperties properties = PolypeptidePropertiesHelper.calculateStats(polypeptideMapper);
            args.put("polypeptide_properties", new PeptidePropertiesType(properties));
        
            //Get the clusertIds and orthologueNames
            initPepClusterIdsAndOrthologueNames(args, polypeptideMapper);
        }else{
            args.put("protein_coding", false);
            args.put("polypeptide_time_last_modified",null);
            args.put("pep_comments", null);
            args.put("pep_curation", null);
            args.put("dbx_refs", null);
            args.put("publications", null);
            args.put("polypeptide_properties",null);
            args.put("cluster_ids", null);
            args.put("orthologue_names", null);  
        }
        logger.debug("Exit initPolypeptideArguments");
    }
    
    private void initPepClusterIdsAndOrthologueNames(HashMap<String, Object> args, FeatureMapper polypeptideMapper){
        List<String> cluserIds = new ArrayList<String>();
        List<String> orthorloguesNames = new ArrayList<String>();
        
        
        String termName = "orthologous_to";
        String cvName = "sequence";
        //filter the feature relation for cvname="sequence", cvterm="orthologous_to"
        List<FeatureRelationshipMapper> featRelates = template.query(
                FeatureRelationshipMapper.TYPE_RESTRICTED_SQL, 
                new FeatureRelationshipMapper(), 
                polypeptideMapper.getFeatureId(),
                cvName, termName);
        
        if (featRelates.size()==0){
            logger.error(String.format("Failed to find term '%s' in cv '%s'", termName, cvName));
            args.put("cluster_ids", new DtoStringArrayField(cluserIds));
            args.put("orthologue_names", new DtoStringArrayField(orthorloguesNames));  
            return;
        }
        
        String sql = "select f.uniquename, cvt.name " +
        		" from feature f, cvterm cvt " +
        		" where f.type_id = cvt.cvterm_id" +
        		" and f.feature_id in (placeHolders)";
        
        //Get the list of object_id to be used as placeholders
        String placeholders = "";
        List<Integer> objectIds = new ArrayList<Integer>(); 
        for(Iterator<FeatureRelationshipMapper> iter = featRelates.iterator(); iter.hasNext();){
            FeatureRelationshipMapper featRelate = iter.next();
            objectIds.add(featRelate.getObjectId());
            placeholders = placeholders + "?";
            if(iter.hasNext()){
                placeholders = placeholders + ",";
            }
        }
        
        //put the placeholsers
        sql = sql.replace("placeholders", placeholders);
        
        List<FeatureMapper> featureTypeMappers = template.query(sql, new FeatureTypeMapper(), objectIds);
        for(FeatureMapper featureTypeMapper: featureTypeMappers){
            if (featureTypeMapper.getCvtName().equals("protein_match")){
                cluserIds.add(featureTypeMapper.getUniqueName());
                
            }else if(featureTypeMapper.getCvtName().equals("polypeptide")){
                orthorloguesNames.add(featureTypeMapper.getUniqueName());
            }
        }

        //initialise the fields
        args.put("cluster_ids", new DtoStringArrayField(cluserIds));
        args.put("orthologue_names", new DtoStringArrayField(orthorloguesNames));        
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
                
                ":cluster_ids," +
                ":pep_comments," +
                ":pep_curation," +
//                ":obsolete_names," +
                ":orthologue_names," +
                ":publications," +
//                ":synonyms," +
                
                ":polypeptide_properties," +
                ":dbx_refs" +
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
