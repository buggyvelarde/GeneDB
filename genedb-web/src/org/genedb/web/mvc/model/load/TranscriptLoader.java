package org.genedb.web.mvc.model.load;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.genedb.web.mvc.model.types.DBXRefType;
import org.genedb.web.mvc.model.types.DtoObjectArrayField;
import org.genedb.web.mvc.model.types.DtoStringArrayField;
import org.genedb.web.mvc.model.types.PeptidePropertiesType;
import org.genedb.web.mvc.model.types.SynonymType;
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
        Date startTime = new Date();
        
        
        //Get the organism
        OrganismMapper organismMapper = template.queryForObject(
               OrganismMapper.SQL_WITH_PARAMS, new OrganismMapper(), organismName);
        
        int loadCount = 0;
        int offset = 1;
              
        List<FeatureMapper> genes = null;
        try{
            do{
                logger.info(String.format("Offset is %s and Limit is %s", offset, limit));

                //Create the mapper and get the genes
                genes = template.query(
                        GeneMapper.SQL_WITH_LIMIT_AND_OFFSET_PARAMS, 
                        new GeneMapper(template), organismMapper.getOrganismId(), limit, offset);
                logger.info("Genes size: " + genes.size());            
                for(FeatureMapper geneMapper: genes){         
                    Date geneProcessingStartTime = new Date();    

                    //Init the toplevelfeature arguments of this transcript
                    Date topLevelFeatureGetStartTime = new Date();
                    FeatureMapper topLevelFeatureMapper = template.queryForObject(
                            TopLevelFeatureMapper.SQL, 
                            new TopLevelFeatureMapper(), geneMapper.getSourceFeatureId());
                    printTimeLapse(topLevelFeatureGetStartTime, "topLevelFeatureGetStartTime");

                    //get the transcripts      
                    Date transcriptGetStartTime = new Date();
                    List<FeatureMapper>transcriptMappers = template.query(
                            TranscriptMapper.SQL, new TranscriptMapper(template), geneMapper.getFeatureId());
                    printTimeLapse(transcriptGetStartTime, "transcriptGetStartTime");

                    logger.info("Transcripts size: " + transcriptMappers.size());                
                    for( FeatureMapper transcriptMapper: transcriptMappers){       
                        Date transcriptProcessingStartTime = new Date();

                        logger.info("Adding..." + transcriptMapper.getFeatureId());

                        HashMap<String, Object> args = new HashMap<String, Object>();

                        //Init the Organism arguments of this transcript
                        initOrganismArguments(args, organismMapper);

                        //Init the gene arguments of this transcript
                        initGeneArguments(args, geneMapper);

                        //Init the transcript arguments
                        initTranscriptArguments(args, transcriptMapper);
                        
                        //Init the synonyms
                        List<SynonymType> synonyms = template.query(
                                SynonymTypeMapper.SQL, new SynonymTypeMapper(), transcriptMapper.getFeatureId());
                        initSynonymTypeArguments(args, synonyms);

                        if (isProductiveTranscript(transcriptMapper)){
                            FeatureMapper polypeptideMapper = template.queryForObject(
                                    PolypeptideMapper.SQL, new PolypeptideMapper(), transcriptMapper.getFeatureId());                                                
                            initPolypeptideArguments(args, polypeptideMapper);
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

                        initTopLevelArguments(args, topLevelFeatureMapper);        

                        loadCount = loadCount + insertDenormalisedTranscript(args);

                        logger.info("Added..." + transcriptMapper.getFeatureId());
                        printTimeLapse(transcriptProcessingStartTime, "transcriptProcessingTime");
                    }
                    printTimeLapse(geneProcessingStartTime, "geneProcessingStartTime");
                }

                //increase the offset
                offset = offset + limit;

            }while(genes!= null && limit <= genes.size());
        }catch(Exception e){
            logger.info("Error: ", e);
            throw e;
        }finally{
            logger.info("Load Count: " + loadCount);
        }
        
        printTimeLapse(startTime, String.format("Exit load(%s)", organismName));
        return loadCount;
    }
    
    private void initSynonymTypeArguments(HashMap<String, Object> args, List<SynonymType> synonyms ){
        logger.debug("Enter initSynonymTypeArguments");
        DtoObjectArrayField objectField = new DtoObjectArrayField("synonymtype", synonyms);
        args.put("synonyms", objectField);
        logger.debug("Exit initSynonymTypeArguments");
        
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
        args.put("top_level_feature_type", topLevelFeature.getCvtName());
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
    
    /**
     * 
     * @param args
     * @param polypeptideMapper
     * @throws Exception
     */
    private void initPolypeptideArguments(HashMap<String, Object> args, FeatureMapper polypeptideMapper)
    throws Exception{
        Date startTime = new Date();
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
            if (properties!= null){
                logger.info("Polypep properties: Amino "
                        + properties.getAminoAcids() +", " 
                        + properties.getCharge() + ", Charge"
                        + properties.getIsoelectricPoint()+ ", IsoElec"
                        + properties.getMass() + ", Mass In Daltons "
                        + properties.getMassInDaltons() );
                args.put("polypeptide_properties", new PeptidePropertiesType(properties));
            }else{
                args.put("polypeptide_properties", null);
                logger.error("Peptide Properties for (featureid: "+polypeptideMapper.getFeatureId() +")is null");
            }
        
            //Get the clusertIds and orthologueNames
            initPepClusterIdsAndOrthologueNames(args, polypeptideMapper);            
        }
        logger.debug("Exit initPolypeptideArguments");
        printTimeLapse(startTime, "initPolypeptideArguments");
    }
    
    /**
     * 
     * @param args
     * @param polypeptideMapper
     */
    private void initPepClusterIdsAndOrthologueNames(HashMap<String, Object> args, FeatureMapper polypeptideMapper){
        Date startTime = new Date();
        
        List<String> cluserIds = new ArrayList<String>();
        List<String> orthorloguesNames = new ArrayList<String>();
        
        List<ClusterIdAndOrthologueNamesMapper> clusterIdAndOrthologueNamesMappers = template.query(
                ClusterIdAndOrthologueNamesMapper.SQL, 
                new ClusterIdAndOrthologueNamesMapper(), polypeptideMapper.getFeatureId());
        
        for(ClusterIdAndOrthologueNamesMapper mapper: clusterIdAndOrthologueNamesMappers){
            if (mapper.getCvtName().equals("protein_match")){
                cluserIds.add(mapper.getUniqueName());
                
            }else if(mapper.getCvtName().equals("polypeptide")){
                orthorloguesNames.add(mapper.getUniqueName());
            }
        }

        //initialise the fields
        args.put("cluster_ids", new DtoStringArrayField(cluserIds));
        args.put("orthologue_names", new DtoStringArrayField(orthorloguesNames));    
        printTimeLapse(startTime, "initPepClusterIdsAndOrthologueNames");
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
        Date startTime = new Date();
        
        for(String key : args.keySet()){
            logger.info(String.format("%s: %s", key, args.get(key)));
        }
        logger.debug("\n");
        logger.debug(String.format("Field args size: %s", args.size()));
        int update = template.update("insert into transcript_cache values(" +
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
                ":top_level_feature_type," +
                ":top_level_feature_uniquename," +
                ":type_description," +
                ":uniquename," +
                
                ":cluster_ids," +
                ":pep_comments," +
                ":pep_curation," +
//                ":obsolete_names," +
                ":orthologue_names," +
                ":publications," +
                
                ":polypeptide_properties," +
                ":synonyms," +
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
        printTimeLapse(startTime, "insertDenormalisedTranscript");
        return update;
    }

    public SimpleJdbcTemplate getTemplate() {
        return template;
    }

    public void setTemplate(SimpleJdbcTemplate template) {
        this.template = template;
    }
    
    private void printTimeLapse(Date startTime, String queryName){
        Date timeAfter = new Date();
        long timeTaken =  timeAfter.getTime() - startTime.getTime();
        if(timeTaken < 1000){
            logger.info(queryName + " execution time = "+ timeTaken + " millisecs");
            
        }else if(timeTaken > 60000){
            logger.info(queryName + " execution time = "+ timeTaken/60 + "." + (timeTaken%60)/1000 +" mins");
            
        }else{
            logger.info(queryName + " execution time = "+ timeTaken/1000 + "." + timeTaken%1000 +" secs");
        }
    }
}
