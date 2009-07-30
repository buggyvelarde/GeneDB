package org.genedb.web.mvc.model.load;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.genedb.db.audit.ChangeSet;
import org.genedb.db.audit.ChangeTracker;
import org.genedb.web.mvc.model.IndexUpdater;
import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.Transcript;
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
public class TranscriptUpdater extends AbstractTranscriptLoader implements IndexUpdater{
    
    Logger logger = Logger.getLogger(TranscriptUpdater.class);

    private ChangeTracker changeTracker;    

    
    public static void main(String args[])throws Exception{
        
        setUpLogging();
        
        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] {"classpath:TranscriptLoader-context.xml"});
        
        TranscriptUpdater transcriptUpdater = ctx.getBean("transcriptUpdater", TranscriptUpdater.class);
        transcriptUpdater.executeAll(TranscriptUpdater.class.getName());        
    }    
    
    public void executeAll(String clientName)throws Exception{
        ChangeSet changeSet = changeTracker.changes(clientName);
        updateAllCaches(changeSet);
    }
    
    
    
    @Override
    public int updateTranscriptCache(ChangeSet changeSet) throws Exception{    
        int updateCount = 0;
        //Processed transcript Ids filter to avoid multiple insert or update of same transcript
        Set<Integer> processedTranscriptsIds = new HashSet<Integer>();

        //Process new genes
        Collection<Integer> featureIds = changeSet.newFeatureIds(AbstractGene.class);
        updateCount = updateCount + batchRequest(featureIds, 50, new GeneChangesProcessor(false, processedTranscriptsIds));

        //Process changed genes
        featureIds = changeSet.changedFeatureIds(AbstractGene.class);
        updateCount = updateCount + batchRequest(featureIds, 50, new GeneChangesProcessor(true, processedTranscriptsIds));

        //Process deleted genes
        featureIds = changeSet.deletedFeatureIds(AbstractGene.class);
        updateCount = updateCount + batchRequest(featureIds, 50, new GenesRemover());




        //Process new Transcript
        featureIds = changeSet.newFeatureIds(Transcript.class);
        updateCount = updateCount + batchRequest(featureIds, 50, new TranscriptChangesProcessor(false, processedTranscriptsIds));

        //Process changed Transcript
        featureIds = changeSet.changedFeatureIds(Transcript.class);
        updateCount = updateCount + batchRequest(featureIds, 50, new TranscriptChangesProcessor(true, processedTranscriptsIds));

        //Process deleted Transcript
        featureIds = changeSet.deletedFeatureIds(Transcript.class);
        updateCount = updateCount + batchRequest(featureIds, 50, new TranscriptsRemover());




        //Process new Polypeptide
        featureIds = changeSet.newFeatureIds(Polypeptide.class);
        updateCount = updateCount + batchRequest(featureIds, 50, new PolypeptideChangesProcessor(false, processedTranscriptsIds));

        //Process changed Polypeptide
        featureIds = changeSet.changedFeatureIds(Polypeptide.class);
        updateCount = updateCount + batchRequest(featureIds, 50, new PolypeptideChangesProcessor(true, processedTranscriptsIds));

        //Process deleted Polypeptide
        featureIds = changeSet.deletedFeatureIds(Polypeptide.class);
        updateCount = updateCount + batchRequest(featureIds, 50, new PolypeptidesRemover());
        
        return updateCount;
    }

    @Override
    public boolean updateAllCaches(ChangeSet changeSet){
        
        try{    
            updateTranscriptCache(changeSet);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
        return true;
    }
    
    /**
     * Batch requests to avoid out of memory issues
     * @param featureIds
     * @param batchSize
     * @param request
     * @throws Exception
     */
    private int batchRequest(Collection<Integer> featureIds, int batchSize, RequestProcessor request)throws Exception{
        int updateCount = 0;
        int currentBatchIndex = 0;
        List<Integer> subset = new ArrayList<Integer>(); 
        List<Integer> ids = new ArrayList<Integer>(featureIds);
        for(ListIterator<Integer> iter = ids.listIterator(); iter.hasNext();){
            if (currentBatchIndex < batchSize){
                subset.add(iter.next());
                ++currentBatchIndex;
                if(iter.hasNext()){
                    continue;
                }
            }                    
            updateCount = updateCount + request.execute(subset);
            subset.clear();
            currentBatchIndex = 0;                
        }
        return updateCount;
    }
    
    /**
     * Class to insert or update the transcripts of a collection of genes
     * @author lo2@sangerinstitute
     *
     */
    private class GeneChangesProcessor implements RequestProcessor{
        private boolean isUpdate;
        private Set<Integer> processedTranscriptsIds = new HashSet<Integer>();
        public GeneChangesProcessor(boolean isUpdate, Set<Integer> processedTranscriptsIds){
            this.isUpdate = isUpdate;
            this.processedTranscriptsIds = processedTranscriptsIds;
        }
        public int execute(Collection<Integer> featureIds)throws Exception{
            Date startTime = new Date();
            int updateCount = 0;
            try{
                //Get the genes
                List<FeatureMapper> genes = findGenes(featureIds);
                for(FeatureMapper geneMapper: genes){  
                    
                    Date geneProcessingStartTime = new Date();                       

                    //get the transcripts      
                    List<FeatureMapper>transcriptMappers = findTranscripts(geneMapper);
                    
                    //Filter out the processed transcript
                    filterOutProcessed(processedTranscriptsIds, transcriptMappers); 
                    
                    //If any unprocessed transcript
                    if(transcriptMappers.size() > 0){

                        //Get the organism
                        OrganismMapper organismMapper = template.queryForObject(
                                OrganismMapper.SQL_WITH_GENE_ID_PARAM, new OrganismMapper(), geneMapper.getFeatureId());

                        //Init the toplevelfeature arguments of this transcript
                        FeatureMapper topLevelFeatureMapper = findTopLevelFeature(geneMapper);

                        //process transcript
                        updateCount = updateCount + processTranscripts(
                                organismMapper, topLevelFeatureMapper, geneMapper, transcriptMappers, isUpdate);

                        //store ids of processed transcripts
                        storeProcessedIds(processedTranscriptsIds, transcriptMappers);
                    }

                    TimerHelper.printTimeLapse(logger, geneProcessingStartTime, "geneProcessingStartTime");
                }
            }catch(Exception e){
                logger.info("Error: ", e);
                throw e;
            }finally{
                logger.info("Update Count: " + updateCount);
            }

            TimerHelper.printTimeLapse(logger, startTime, "Exit updateGenes");
            return updateCount;
        }
    }
    
    /**
     * Class to insert or update a set of transcripts
     * @author lo2@sangerinstitute
     *
     */
    private class TranscriptChangesProcessor implements RequestProcessor{
        private boolean isUpdate;
        private Set<Integer> processedTranscriptsIds = new HashSet<Integer>();
        public TranscriptChangesProcessor(boolean isUpdate, Set<Integer> processedTranscriptsIds){
            this.isUpdate = isUpdate;
            this.processedTranscriptsIds = processedTranscriptsIds;
        }
        public int execute(Collection<Integer> featureIds)throws Exception{
            Date startTime = new Date();
            int updateCount = 0;
            try{
                //Get the transcript from transcript ids
                List<FeatureMapper>transcriptMappers = findTranscriptsFromTranscriptIds(featureIds);
                
                //Process transcripts loaded from the featureIds
                updateCount = processTranscript(transcriptMappers, processedTranscriptsIds, isUpdate);
                
            }catch(Exception e){
                logger.info("Error: ", e);
                throw e;
            }finally{
                logger.info("Update Count: " + updateCount);
            }

            TimerHelper.printTimeLapse(logger, startTime, "Exit updateGenes");
            return updateCount;
        }
    }
    
    /**
     * Class to insert or update a set of transcripts
     * @author lo2@sangerinstitute
     *
     */
    private class PolypeptideChangesProcessor implements RequestProcessor{
        private boolean isUpdate;
        private Set<Integer> processedTranscriptsIds = new HashSet<Integer>();
        public PolypeptideChangesProcessor(boolean isUpdate, Set<Integer> processedTranscriptsIds){
            this.isUpdate = isUpdate;
            this.processedTranscriptsIds = processedTranscriptsIds;
        }
        public int execute(Collection<Integer> featureIds)throws Exception{
            Date startTime = new Date();
            int updateCount = 0;
            try{
                //Get the transcripts from peps ids
                List<FeatureMapper>transcriptMappers = findTranscriptsFromPolypeptideIds(featureIds);
                
                //Process transcripts loaded from the featureIds
                updateCount = processTranscript(transcriptMappers, processedTranscriptsIds, isUpdate);
                
            }catch(Exception e){
                logger.info("Error: ", e);
                throw e;
            }finally{
                logger.info("Update Count: " + updateCount);
            }

            TimerHelper.printTimeLapse(logger, startTime, "Exit updateGenes");
            return updateCount;
        }
    }
    
    /**
     * Process a transcript
     * @param transcriptMappers
     * @param processedTranscriptsIds
     * @param isUpdate
     * @return
     * @throws Exception
     */
    private int processTranscript(List<FeatureMapper>transcriptMappers, Set<Integer> processedTranscriptsIds, boolean isUpdate)
    throws Exception{
        int updateCount = 0;
        
        //Filter out the processed transcript
        filterOutProcessed(processedTranscriptsIds, transcriptMappers);
        
        for(FeatureMapper transcriptMapper: transcriptMappers){  
            
            Date geneProcessingStartTime = new Date();      
            
            FeatureMapper geneMapper = template.queryForObject(
                    GeneMapper.SQL_WITH_TRANSCRIPT_ID_PARAM, new GeneMapper(), transcriptMapper.getFeatureId());

            //Get the organism
            OrganismMapper organismMapper = template.queryForObject(
                    OrganismMapper.SQL_WITH_GENE_ID_PARAM, new OrganismMapper(), geneMapper.getFeatureId());

            //Init the toplevelfeature arguments of this transcript
            FeatureMapper topLevelFeatureMapper = findTopLevelFeature(geneMapper);

            //process transcript 
            List<FeatureMapper> singleItemList = new ArrayList<FeatureMapper>();
            singleItemList.add(transcriptMapper);
            updateCount = updateCount + processTranscripts(
                    organismMapper, topLevelFeatureMapper, geneMapper, singleItemList, isUpdate);

            TimerHelper.printTimeLapse(logger, geneProcessingStartTime, "geneProcessingStartTime");
        }
        
        //store ids of processed transcripts
        storeProcessedIds(processedTranscriptsIds, transcriptMappers);
        
        return updateCount;        
    }
    
    /**
     * Remove a collection of transcripts of a collection of genes
     * @author lo2@sangerinstitute
     *
     */
    private class GenesRemover implements RequestProcessor{
        public int execute(Collection<Integer> featureIds)throws Exception{
            String sql = "delete from transcript where gene_id in (placeholders)";
            sql = sql.replace("placeholders", formatPlaceholders(featureIds.size()));
            return template.update(sql, featureIds.toArray((Object[])new Integer[0]));
        }
    }
    
    /**
     * Remove a collection of transcripts
     * @author lo2@sangerinstitute
     *
     */
    private class TranscriptsRemover implements RequestProcessor{
        public int execute(Collection<Integer> featureIds)throws Exception{
            String sql = "delete from transcript where transcript_id in (:placeholders)";
            sql = sql.replace(":placeholders", formatPlaceholders(featureIds.size()));
            return template.update(sql, featureIds.toArray((Object[])new Integer[0]));
        }
    }
    
    /**
     * Remove a collection of transcripts
     * @author lo2@sangerinstitute
     *
     */
    private class PolypeptidesRemover implements RequestProcessor{
        public int execute(Collection<Integer> featureIds)throws Exception{
            int deletes = 0;
            String placeholders = formatPlaceholders(featureIds.size());
            //Delete the transcript featurecvterm where related polypeptides are found
            String sql = "delete transcript_featurecvterm where polypeptide_id in (:placeholders)";
            sql = sql.replace(":placeholders", placeholders);
            deletes = template.update(sql, featureIds);
            
            //Delete the transcript featureprop where related polypeptides are found
            sql = "delete transcript_featureprop where polypeptide_id in (:placeholders)";
            sql = sql.replace(":placeholders", placeholders);
            deletes = deletes + template.update(sql, featureIds);
            return deletes;
        }
    }
    
    
    private List<FeatureMapper> findGenes(Collection<Integer> featureIds){   
        String sql  = GeneMapper.SQL_WITH_GENE_ID_PARAMS;
        sql =  sql.replace(":placeholders", formatPlaceholders(featureIds.size()));
        //Create the mapper and get the genes
        List<FeatureMapper> genes = template.query(
                sql, new GeneMapper(), featureIds.toArray((Object[])new Integer[0]));
        logger.info("Genes size: " + genes.size());        
        return genes;
    }
    
    
    private List<FeatureMapper> findTranscriptsFromTranscriptIds(Collection<Integer> featureIds){   
        String sql  = TranscriptMapper.SQL_WITH_TRANSCRIPT_ID_PARAMS;
        sql =  sql.replace(":placeholders", formatPlaceholders(featureIds.size()));
        //Create the mapper and get the genes
        List<FeatureMapper> transcripts = template.query(
                sql, new TranscriptMapper(), featureIds.toArray((Object[])new Integer[featureIds.size()]));
        logger.info("Transcript size: " + transcripts.size());        
        return transcripts;
    }
    
    
    private List<FeatureMapper> findTranscriptsFromPolypeptideIds(Collection<Integer> featureIds){   
        String sql  = TranscriptMapper.SQL_WITH_POLYPEPTIDE_ID_PARAMS;
        sql =  sql.replace(":placeholders", formatPlaceholders(featureIds.size()));
        //Create the mapper and get the genes
        List<FeatureMapper> transcripts = template.query(
                sql, new TranscriptMapper(), featureIds.toArray((Object[])new Integer[0]));
        logger.info("Transcript size: " + transcripts.size());        
        return transcripts;
    }

    private interface RequestProcessor{
        public int execute(Collection<Integer> featureIds)throws Exception;
    }
    
    private String formatPlaceholders(int count){
        StringBuffer sb = new StringBuffer();
        for(int i=0; i<count; ++i){
            sb.append("?");
            if(i+1<count){
                sb.append(",");
            }
        }
        return sb.toString();
    }
    
    /**
     * Filter out the processed transcript to avoid re-processing of alreaddy processed transcripts
     * @param transcriptMappers
     * @return
     */
    private void filterOutProcessed(Set<Integer> processedIds, List<FeatureMapper>transcriptMappers){
        for(ListIterator<FeatureMapper> iter = transcriptMappers.listIterator(); iter.hasNext(); ){
            if(processedIds.contains(iter.next().getFeatureId())){
                iter.remove();
            }
        }
    }
    
    /**
     * Add the ids of processed transcript to help prevent re-processing of already processed transcripts
     * @param processedIds
     * @param transcriptMappers
     */
    private void storeProcessedIds(Set<Integer> processedIds, List<FeatureMapper>transcriptMappers){
        for(ListIterator<FeatureMapper> iter = transcriptMappers.listIterator(); iter.hasNext(); ){
           processedIds.add(iter.next().getFeatureId());
        }
    }

    public SimpleJdbcTemplate getTemplate() {
        return template;
    }

    public void setTemplate(SimpleJdbcTemplate template) {
        this.template = template;
    }

    public ChangeTracker getChangeTracker() {
        return changeTracker;
    }

    public void setChangeTracker(ChangeTracker changeTracker) {
        this.changeTracker = changeTracker;
    }
    
}
