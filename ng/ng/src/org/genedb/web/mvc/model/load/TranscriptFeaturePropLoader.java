package org.genedb.web.mvc.model.load;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.genedb.web.mvc.model.types.DtoStringArrayField;
import org.genedb.web.mvc.model.types.FeaturePropType;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class TranscriptFeaturePropLoader {
    static Logger logger = Logger.getLogger(TranscriptFeaturePropLoader.class);



    /**
     * Load transcript_featureprop records
     * @param pep
     * @param template
     * @throws Exception
     */
    public static void load(int transcriptId, FeatureMapper pep, SimpleJdbcTemplate template)
    throws Exception{
        //Get the comments for polypeptide
        List<FeaturePropType> featurePropTypes = template.query(
                FeaturePropMapper.SQL, new FeaturePropMapper(), pep.getFeatureId());

        for(FeaturePropType featurePropType: featurePropTypes){
            Map<String, Object> args = new HashMap<String, Object>();
            try{

                args.put("transcript_id", transcriptId);
                args.put("feature_prop_id", featurePropType.getFeaturePropId());
                args.put("polypeptide_id", pep.getFeatureId());

                //Get the CV Name
                args.put("cv_name", featurePropType.getCvName());

                //Get the Type Name
                args.put("cvterm_name", featurePropType.getCvtName());

                //Get the value
                args.put("featureprop_value", featurePropType.getValue());

                //Get publications
                logger.debug("Getting ...pubNames");
                List<String> pubNames = template.query(
                        PubNameMapper.FEATURE_PROP_SQL, new PubNameMapper(), featurePropType.getFeaturePropId());
                args.put("pubs", new DtoStringArrayField(pubNames));

                //insert
                logger.debug("Inserting ...");
                insert(args, template);

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
        int update = template.update("insert into transcript_featureprop " +
                "(" +
                "feature_prop_id," +
                "polypeptide_id," +
                "cv_name," +
                "cvterm_name," +
                "featureprop_value," +
                "pubs," +
                "transcript_id" +
                ") values(" +
                ":feature_prop_id," +
                ":polypeptide_id," +
                ":cv_name," +
                ":cvterm_name," +
                ":featureprop_value," +
                ":pubs," +
                ":transcript_id" +
                ") ",  args);

        logger.debug("trans prop loaded......");
        logger.debug("\n");
        return update;
    }
}
