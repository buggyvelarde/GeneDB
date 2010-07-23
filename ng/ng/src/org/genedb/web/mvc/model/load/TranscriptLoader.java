package org.genedb.web.mvc.model.load;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.annotation.Transactional;
/**
 *
 * @author lo2@sangerinstitute
 *
 */
@Transactional
public class TranscriptLoader extends AbstractTranscriptLoader{

    Logger logger = Logger.getLogger(TranscriptLoader.class);

    public static void main(String args[])throws Exception{

        setUpLogging();

        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] {"classpath:TranscriptLoader-context.xml"});
        TranscriptLoader transcriptLoader = ctx.getBean("transcriptLoader", TranscriptLoader.class);
        transcriptLoader.load("Tbruceibrucei927", Integer.MAX_VALUE);
    }

    /**
     * Load transcripts of all organisms
     * @param limit
     */
//    public int loadAll(int limit)throws Exception{
//        int loadCount = 0;
//        List<OrganismMapper> organisms = template.query(
//                OrganismMapper.GET_ALL_ORGANISMS_SQL, new OrganismMapper());
//        for(OrganismMapper organismMapper: organisms){
//            logger.info("Loading Organism: " + organismMapper.getCommonName());
//            loadCount = loadCount + load(organismMapper.getCommonName(), limit);
//        }
//        return loadCount;
//    }

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
               OrganismMapper.GET_ALL_ORGANISMS_SQL_WITH_COMMON_NAME_PARAM,
               new OrganismMapper(), organismName);

        int loadCount = 0;
        int offset = 1;

        List<FeatureMapper> genes = null;
        try{
            do{
                //Get the genes for this organism
                genes = findGenes(organismMapper, offset, limit);

                for(FeatureMapper geneMapper: genes){
                    Date geneProcessingStartTime = new Date();

                    //Init the toplevelfeature arguments of this transcript
                    FeatureMapper topLevelFeatureMapper = findTopLevelFeature(geneMapper);

                    //get the transcripts
                    List<FeatureMapper>transcriptMappers = findTranscripts(geneMapper);

                    //process transcript
                    loadCount = loadCount + processTranscripts(
                            organismMapper, topLevelFeatureMapper, geneMapper, transcriptMappers, false);

                    TimerHelper.printTimeLapse(logger, geneProcessingStartTime, "geneProcessingStartTime");
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

        TimerHelper.printTimeLapse(logger, startTime, String.format("Exit load(%s)", organismName));
        return loadCount;
    }

    /**
     * Find the genes for the given organism
     * @param organismMapper
     * @param offset
     * @param limit
     * @return
     */
    private List<FeatureMapper> findGenes(OrganismMapper organismMapper, int offset, int limit){
        logger.info(String.format("Offset is %s and Limit is %s", offset, limit));

        //Create the mapper and get the genes
        List<FeatureMapper> genes = template.query(
            GeneMapper.GET_GENES_SQL_WITH_LIMIT_AND_OFFSET_PARAMS,
            new GeneMapper(), organismMapper.getOrganismId(), limit, offset);
        logger.info("Genes size: " + genes.size());
        return genes;
    }
}
