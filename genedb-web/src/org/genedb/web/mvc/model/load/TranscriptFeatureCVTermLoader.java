package org.genedb.web.mvc.model.load;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.genedb.web.mvc.model.types.DBXRefType;
import org.genedb.web.mvc.model.types.DtoObjectArrayField;
import org.genedb.web.mvc.model.types.DtoStringArrayField;
import org.genedb.web.mvc.model.types.FeatureCVTPropType;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;


/**
 * 
 * @author lo2@sangerinstitute
 *
 */
public class TranscriptFeatureCVTermLoader {
    static Logger logger = Logger.getLogger(TranscriptFeatureCVTermLoader.class);
    
    /**
     * Load transcript_featurecvterm record
     * @param pep
     * @param cvNamePrefix
     * @param template
     * @throws Exception 
     */
    public static void load(int transcriptId, FeatureMapper pep, String cvNamePrefix, SimpleJdbcTemplate template) 
    throws Exception{
        
        //append
        Date fcvtGetTime = new Date();
        String _cvNamePrefix = cvNamePrefix + "%";
        List<FeatureCvtermMapper> featureCvtermMappers= 
            template.query(FeatureCvtermMapper.SQL, new FeatureCvtermMapper(), _cvNamePrefix, pep.getFeatureId());
        TimerHelper.printTimeLapse(logger, fcvtGetTime, "fcvtGetTime");
        
        //Get the Feature CVTerm Properties
        for(FeatureCvtermMapper mapper : featureCvtermMappers){    
            Map<String, Object> args = new HashMap<String, Object>();
            try{
                Date processFcvtTime = new Date();            

                args.put("transcript_id", transcriptId);
                args.put("cv_name_prefix", cvNamePrefix);

                //Get the Name
                logger.debug("Getting ...cvTermName");
                String cvTermName = (String)template.queryForObject(
                        "select name from cvterm where cvterm_id = ?",
                        String.class,
                        new Object[]{new Integer(mapper.getTypeId())} );
                args.put("cvterm_name", cvTermName);


                //Get the Accession
                logger.debug("Getting ...typeAccession");
                String typeAccession = (String)template.queryForObject(
                        " select accession " +
                        " from cvterm cvt, dbxref dbx " +
                        " where cvt.dbxref_id = dbx.dbxref_id " +
                        " and cvt.cvterm_id = ?",
                        String.class,
                        new Object[]{new Integer(mapper.getTypeId())});
                args.put("type_accession", typeAccession);


                //Get the withFrom
                logger.debug("Getting ...withFromPb");
                String withFromPub = (String)template.queryForObject(
                        "select uniquename from pub where pub_id = ?",
                        String.class,
                        new Object[]{new Integer(mapper.getPubId())});
                args.put("with_from_pub", withFromPub);


                //Get the feature cvterm count
                logger.debug("Getting ...feature_cvterm_count");
                int featureCvtermCount = template.queryForInt(
                        " select count(f.feature_id)" +
                        " from feature f, feature_cvterm fcvt" +
                        " where fcvt.cvterm_id = ?" +
                        " and f.organism_id = ?" +
                        " and f.feature_id = fcvt.feature_id",
                        mapper.getTypeId(), pep.getOrganismId());
                args.put("feature_cvterm_count", featureCvtermCount);


                //Get publications
                logger.debug("Getting ...pubNames");
                List<String> pubNames = template.query(
                        PubNameMapper.FEATURE_CVTERM_SQL, new PubNameMapper(), mapper.getFeatureCvtId());
                args.put("pubs", new DtoStringArrayField(pubNames));


                //Get the dbxref
                logger.debug("Getting ...dbxRefs");
                List<DBXRefType> dbxRefs = template.query(
                        DbxRefMapper.FEATURE_CVTERM_SQL, new DbxRefMapper(), mapper.getFeatureCvtId());                        
                DtoObjectArrayField objectField = new DtoObjectArrayField("dbxreftype", dbxRefs);
                args.put("dbxrefs", objectField);            


                //Get the pros
                logger.debug("Getting ...properties");
                List<FeatureCVTPropType> props = template.query(
                        FeatureCVTermPropMapper.SQL, new FeatureCVTermPropMapper(), mapper.getFeatureCvtId());                        
                objectField = new DtoObjectArrayField("featurecvtproptype", props);
                args.put("properties", objectField);            

                //insert
                logger.debug("Inserting ...");
                insert(args, template);


                TimerHelper.printTimeLapse(logger, processFcvtTime, "processFcvtTime");
            }catch(Exception e){
                String message = null;
                for(String key : args.keySet()){
                    message = message + String.format("%s: %s\n", key, args.get(key));
                }
                logger.error(message, e);
                throw e;
            }
        }
    }
    
    private static int insert(Map<String, Object> args, SimpleJdbcTemplate template){
        if (logger.isInfoEnabled()){
            for(String key : args.keySet()){
                logger.info(String.format("%s: %s", key, args.get(key)));
            }
        }
        int update = template.update("insert into transcript_featurecvterm " +
        		"values(nextval('transcript_featurecvterm_seq')," +
                ":cv_name_prefix," +
                ":cvterm_name," +
                ":type_accession," +
                ":with_from_pub," +
                ":feature_cvterm_count," +
                ":pubs," +
                ":dbxrefs," +
                ":properties," +
                ":transcript_id" +
                ") ",  args);
        
        logger.debug("trans fcvt loaded......");
        logger.debug("\n");
        return update;
    } 
}
