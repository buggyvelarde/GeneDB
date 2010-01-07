package org.genedb.web.mvc.model.load;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.genedb.web.mvc.model.types.DBXRefType;
import org.genedb.web.mvc.model.types.DtoObjectArrayField;
import org.genedb.web.mvc.model.types.DtoStringArrayField;
import org.genedb.web.mvc.model.types.SynonymType;
import org.genedb.web.mvc.model.types.TranscriptRegionType;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.util.CollectionUtils;


public abstract class AbstractTranscriptLoader {
    private Logger logger = Logger.getLogger(AbstractTranscriptLoader.class);

    /**
     * Set up logging
     */
    protected static void setUpLogging() {
        String log4jprops = "/log4j.TranscriptLoader.properties";
        URL url = TranscriptLoader.class.getResource(log4jprops);
        System.out.printf("Configuring Log4J from '%s'\n", url);
        PropertyConfigurator.configure(url);
    }

    protected SimpleJdbcTemplate template;


    protected int saveOrUpdate(HashMap<String, Object> args, boolean isUpdate) throws Exception {
        if (!isUpdate) {
            return insertDenormalisedTranscript(args);
        }
        return updateDenormalisedTranscript(args);
    }

    /**
     * Insert
     * @param args
     * @return
     * @throws Exception
     */
    public int insertDenormalisedTranscript(HashMap<String, Object> args) throws Exception {
        logger.debug("Enter insertDenormalisedTranscript");
        Date startTime = new Date();

        logger.debug(String.format("Field args size: %s", args.size()));

        int update = 0;
        try {
            logger.info("Loading Transcript: " + args.get("transcript_id"));
            update = template.update("insert into transcript(" +
                    "transcript_id," +
                    "transcript_name," +
                    "transcript_cvterm_name," +
                    "transcript_cv_name," +
                    "transcript_uniquename," +
                    "transcript_time_last_modified," +
                    "gene_id," +
                    "gene_name," +
                    "gene_time_last_modified," +
                    "gene_fmax," +
                    "gene_fmin," +
                    "gene_strand," +
                    "gene_cvterm_name," +
                    "gene_cv_name," +
                    "organism_id," +
                    "organism_common_name," +
                    "top_level_feature_name," +
                    "top_level_feature_seqlen," +
                    "top_level_feature_uniquename," +
                    //"top_level_feature_type," +
                    "top_level_feature_type" +
                    //"cluster_ids," +
                    //"orthologue_names," +
                    //"publications," +
                    //"synonyms," +
                    //"exons," +
                    //"dbx_refs" +
            		")" +
            		" values(" +
                    ":transcript_id," +
                    ":transcript_name," +
                    ":transcript_cvterm_name," +
                    ":transcript_cv_name," +
                    ":transcript_uniquename," +
                    ":transcript_time_last_modified," +

                    ":gene_id," +
                    ":gene_name," +
                    ":gene_time_last_modified," +
                    ":gene_fmax," +
                    ":gene_fmin," +
                    ":gene_strand," +
                    ":gene_cvterm_name," +
                    ":gene_cv_name," +

                    ":organism_id," +
                    ":organism_common_name," +

                    ":top_level_feature_name," +
                    ":top_level_feature_seqlen," +
                    ":top_level_feature_uniquename," +
                    //":top_level_feature_type," +
                    ":top_level_feature_type" +

                    //":cluster_ids," +
                    //":orthologue_names," +
                    //":publications," +
                    //":synonyms," +
                    //":transcript_regions," +
                    //":dbx_refs" +
                    ") ",  args);
        } catch(Exception e) {
            String message = null;
            for (String key : args.keySet()) {
                message = message + String.format("%s: %s\n", key, args.get(key));
            }
            logger.error(message, e);
            throw e;
        }
        TimerHelper.printTimeLapse(logger, startTime, "insertDenormalisedTranscript");
        logger.debug("trans loaded......");
        logger.debug("\n");
        return update;
    }

    /**
     * Update
     * @param args
     * @return
     * @throws Exception
     */
    public int updateDenormalisedTranscript(HashMap<String, Object> args) throws Exception {
        logger.debug("Enter updateDenormalisedTranscript");
        Date startTime = new Date();

        logger.debug(String.format("Field args size: %s", args.size()));
        int update = 0;
        try {
            update = template.update("update transcript values set" +
                    " transcript_name = :transcript_name," +
                    " transcript_cvterm_name = :transcript_cvterm_name," +
                    " transcript_cv_name = :transcript_cv_name," +
                    " transcript_uniquename = :transcript_uniquename," +
                    " transcript_time_last_modified = :transcript_time_last_modified," +

                    " gene_id = :gene_id," +
                    " gene_name= :gene_name," +
                    " gene_time_last_modified = :gene_time_last_modified," +
                    " gene_fmax = :gene_fmax," +
                    " gene_fmin = :gene_fmin," +
                    " gene_strand = :gene_strand," +
                    " gene_cvterm_name = :gene_cvterm_name," +
                    " gene_cv_name = :gene_cv_name," +

                    " organism_id = :organism_id," +
                    " organism_common_name = :organism_common_name," +

                    " top_level_feature_name = :top_level_feature_name," +
                    " top_level_feature_seqlen = :top_level_feature_seqlen," +
                    " top_level_feature_uniquename = :top_level_feature_uniquename," +
                    " top_level_feature_type = :top_level_feature_type," +

                    " cluster_ids = :cluster_ids," +
                    " orthologue_names = :orthologue_names," +
                    " publications = :publications," +
                    " synonyms = :synonyms," +
                    " transcript_regions = :transcript_regions," +
                    " dbx_refs = :dbx_refs" +
                    " where transcript_id = :transcript_id ",  args);
        } catch(Exception e) {
            String message = null;
            for (String key : args.keySet()) {
                message = message + String.format("%s: %s\n", key, args.get(key));
            }
            logger.error(message, e);
            throw e;
        }
        TimerHelper.printTimeLapse(logger, startTime, "updateDenormalisedTranscript");
        logger.debug("trans loaded......");
        logger.debug("\n");
        return update;
    }

    /**
     * Process each transcript derived from the gene
     * @param organismMapper
     * @param topLevelFeatureMapper
     * @param geneMapper
     * @param transcriptMappers
     * @param isUpdate or insert (for this method call)
     * @return
     * @throws Exception
     */
    protected int processTranscripts(
            OrganismMapper organismMapper, FeatureMapper topLevelFeatureMapper,
            FeatureMapper geneMapper, List<FeatureMapper> transcriptMappers, boolean isUpdate) throws Exception{
        int loadCount = 0;
        for (FeatureMapper transcriptMapper: transcriptMappers) {
            Date transcriptProcessingStartTime = new Date();

            logger.info("Adding..." + transcriptMapper.getFeatureId());

            HashMap<String, Object> args = new HashMap<String, Object>();

            //Init the Organism arguments of this transcript
            initOrganismArguments(args, organismMapper);

            //Init the toplevelfeature details
            initTopLevelArguments(args, topLevelFeatureMapper);

            //Init the gene arguments of this transcript
            initGeneArguments(args, geneMapper);

            //Init the transcript arguments
            initTranscriptArguments(args, transcriptMapper);

            //Init the synonyms
            initSynonymTypeArguments(args, transcriptMapper);

            //Init the transcript region
            initTranscriptRegionTypeArguments(args, transcriptMapper);

            //Init the derived polypeptides details
            FeatureMapper polypeptideMapper = initTranscriptProteinArguments(
                    args, transcriptMapper);

            //Insert into the transcript_cache table
            loadCount = loadCount + saveOrUpdate(args, isUpdate);

            if(isUpdate){
                //delete transcript child records in transcript_featurecvterm and transcript_prop
                deleteTranscriptChildren(transcriptMapper.getFeatureId());
            }

            if(polypeptideMapper!= null){
              //Insert into the transcript_featurecvterm table
                TranscriptFeatureCVTermLoader.load(
                        transcriptMapper.getFeatureId(), polypeptideMapper, template);

              //Insert into the transcript_featureprop table
                TranscriptFeaturePropLoader.load(
                        transcriptMapper.getFeatureId(), polypeptideMapper, template);
            }


            logger.info("Added..." + transcriptMapper.getFeatureId());
            TimerHelper.printTimeLapse(logger, transcriptProcessingStartTime, "transcriptProcessingTime");
        }
        return loadCount;
    }


    /**
     * Find the Top Level Feature from the Gene
     */
    protected FeatureMapper findTopLevelFeature(FeatureMapper geneMapper){
        //Init the toplevelfeature arguments of this transcript
        Date topLevelFeatureGetStartTime = new Date();
        FeatureMapper topLevelFeatureMapper = template.queryForObject(
                TopLevelFeatureMapper.SQL,
                new TopLevelFeatureMapper(), geneMapper.getSourceFeatureId());
        TimerHelper.printTimeLapse(logger, topLevelFeatureGetStartTime, "topLevelFeatureGetStartTime");
        return topLevelFeatureMapper;
    }

    /**
     * Find the transcript from the Gene
     * @param geneMapper
     * @return
     */
    protected List<FeatureMapper> findTranscripts(FeatureMapper geneMapper){
        //get the transcripts
        Date transcriptGetStartTime = new Date();
        List<FeatureMapper>transcriptMappers = template.query(
                TranscriptMapper.SQL_WITH_GENE_ID_PARAM, new TranscriptMapper(), geneMapper.getFeatureId());
        logger.info("Transcripts size: " + transcriptMappers.size());
        TimerHelper.printTimeLapse(logger, transcriptGetStartTime, "transcriptGetStartTime");
        return transcriptMappers;
    }

    protected void initSynonymTypeArguments(HashMap<String, Object> args, FeatureMapper transcriptMapper){
        logger.debug("Enter initSynonymTypeArguments");
        List<SynonymType> synonyms = template.query(
                SynonymTypeMapper.SQL, new SynonymTypeMapper(), transcriptMapper.getFeatureId());
        DtoObjectArrayField objectField = new DtoObjectArrayField("synonymtype", synonyms);
        args.put("synonyms", objectField);
        logger.debug("Exit initSynonymTypeArguments");

    }

    protected void initTranscriptRegionTypeArguments(HashMap<String, Object> args, FeatureMapper transcriptMapper){
        logger.debug("Enter initSynonymTypeArguments");
        List<TranscriptRegionType> transcriptRegions = template.query(
                TranscriptRegionMapper.SQL, new TranscriptRegionMapper(), transcriptMapper.getFeatureId());
        DtoObjectArrayField objectField = new DtoObjectArrayField("transcriptregiontype", transcriptRegions);
        args.put("transcript_regions", objectField);
        logger.debug("Exit initTranscriptRegionTypeArguments");
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
        args.put("gene_fmax", geneMapper.getFmax());
        args.put("gene_fmin", geneMapper.getFmin());
        args.put("gene_strand", geneMapper.getStrand());
        args.put("gene_cvterm_name", geneMapper.getCvName());
        args.put("gene_cv_name", geneMapper.getCvtName());
        logger.debug("Exit initGeneArguments");
    }

    private void initTopLevelArguments(HashMap<String, Object> args, FeatureMapper topLevelFeature){
        logger.debug("Enter initTopLevelArguments");
        args.put("top_level_feature_name", topLevelFeature.getName());
        args.put("top_level_feature_seqlen", topLevelFeature.getSeqLen());
        args.put("top_level_feature_type", topLevelFeature.getCvtName());
        args.put("top_level_feature_uniquename", topLevelFeature.getUniqueName());
        logger.debug("Exit initTopLevelArguments");
    }

    private void initTranscriptArguments(HashMap<String, Object> args, FeatureMapper transcriptMapper){
        logger.debug("Enter initTranscriptArguments");
        args.put("transcript_id", transcriptMapper.getFeatureId());
        args.put("transcript_time_last_modified", transcriptMapper.getTimeLastModified());
        args.put("transcript_uniquename", transcriptMapper.getUniqueName());
        args.put("transcript_name", transcriptMapper.getName());
        args.put("transcript_cvterm_name", transcriptMapper.getCvtName());
        args.put("transcript_cv_name", transcriptMapper.getCvName());
        args.put("organism_id", transcriptMapper.getOrganismId());
        logger.debug("Exit initTranscriptArguments");
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

    private FeatureMapper initTranscriptProteinArguments(HashMap<String, Object> args, FeatureMapper transcriptMapper)
    throws Exception{
        //Init the derived polypeptides details
        FeatureMapper polypeptideMapper = null;
        if (isProductiveTranscript(transcriptMapper)){
            polypeptideMapper = template.queryForObject(
                    PolypeptideMapper.SQL, new PolypeptideMapper(), transcriptMapper.getFeatureId());
            initPolypeptideArguments(args, polypeptideMapper);
        }else{
            args.put("dbx_refs", new DtoObjectArrayField("dbxreftype", new ArrayList<DBXRefType>()));
            DtoStringArrayField emptyArr = new DtoStringArrayField(null);
            args.put("publications", emptyArr);
            args.put("cluster_ids", emptyArr);
            args.put("orthologue_names", emptyArr);
        }
        return polypeptideMapper;
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
            args.put("polypeptide_time_last_modified", polypeptideMapper.getTimeLastModified());

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
//            PeptideProperties properties = PolypeptidePropertiesHelper.calculateStats(polypeptideMapper);
//            if (properties!= null){
//                logger.info("Polypep properties: Amino "
//                        + properties.getAminoAcids() +", "
//                        + properties.getCharge() + ", Charge"
//                        + properties.getIsoelectricPoint()+ ", IsoElec"
//                        + properties.getMass() + ", Mass In Daltons "
//                        + properties.getMassInDaltons() );
//                args.put("polypeptide_properties", new PeptidePropertiesType(properties));
//            }else{
//                args.put("polypeptide_properties", null);
//                logger.error("Peptide Properties for (featureid: "+polypeptideMapper.getFeatureId() +")is null");
//            }

            //Get the clusertIds and orthologueNames
            initPepClusterIdsAndOrthologueNames(args, polypeptideMapper);
        }
        logger.debug("Exit initPolypeptideArguments");
        TimerHelper.printTimeLapse(logger, startTime, "initPolypeptideArguments");
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
        TimerHelper.printTimeLapse(logger, startTime, "initPepClusterIdsAndOrthologueNames");
    }

    public int deleteTranscriptChildren(Integer transcriptId)throws Exception{
        String sql = "delete from transcript_featurecvterm where transcript_id = ?";
        int count = template.update(sql, transcriptId);
        sql = "delete from transcript_featureprop where transcript_id = ?";
        count = count + template.update(sql, transcriptId);
        return count;
    }

    public SimpleJdbcTemplate getTemplate() {
        return template;
    }

    public void setTemplate(SimpleJdbcTemplate template) {
        this.template = template;
    }
}
