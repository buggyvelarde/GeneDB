package org.genedb.web.mvc.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.genedb.db.audit.ChangeSet;
import org.genedb.db.audit.ChangeTracker;
import org.genedb.db.domain.luceneImpls.BasicGeneServiceImpl;
import org.genedb.db.domain.services.BasicGeneService;
import org.genedb.querying.core.LuceneIndex;
import org.genedb.querying.core.LuceneIndexFactory;
import org.genedb.web.gui.DiagramCache;
import org.genedb.web.gui.RenderedDiagramFactory;
import org.genedb.web.mvc.controller.ModelBuilder;
import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Gap;
import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.TopLevelFeature;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

import com.sleepycat.collections.StoredMap;

/**
 * To synchronise the Berkeley DB cache with Latest RDBMS updates
 * @author L.Oke
 *
 */
public class CacheSynchroniser {
	private static final Logger logger = Logger.getLogger(CacheSynchroniser.class);
    
    
    private RenderedDiagramFactory renderedDiagramFactory;
    private DiagramCache diagramCache;
    private ModelBuilder modelBuilder;
    private LuceneIndexFactory luceneIndexFactory;

    private SessionFactory sessionFactory;
    
    private ChangeTracker changeTracker;    
    private boolean isNoContextMap;
    
    protected BerkeleyMapFactory bmf;
    protected StoredMap<Integer, TranscriptDTO> dtoMap;
    protected StoredMap<Integer, String> contextMapMap;
    protected BasicGeneService basicGeneService;
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			//Get the sychroniser
			ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(
					new String[] {"classpath:applicationContext.xml", "classpath:synchCaches.xml"});
			ctx.refresh();
			CacheSynchroniser cacheSynchroniser = ctx.getBean("cacheSynchroniser", CacheSynchroniser.class);

			//Start synching
			if (!cacheSynchroniser.updateAllCaches()){
			    logger.error("Errors found:");
	            System.exit(64);            
			}
		
		}catch(Exception e){
            logger.error(e);
            System.exit(64);			
		}
	}
	
	
	/**
	 * Update the Context Map and Transcript DTO caches
	 * @return Boolean 
	 *         Is updates to caches successful
	 * @throws Exception
	 */
	@Transactional
	boolean updateAllCaches()throws Exception{
	    
        //Initialise
        init();
        
        //Get the records to update
        ChangeSet changeSet = changeTracker.changes(CacheSynchroniser.class.getName() );
	    
        //Update the Contect Map Cache
	    boolean isContextUpdated = updateContextMapCache(changeSet);
	    
	    //Update the Transcript DTO
	    boolean isTransciptDTOUpdated = updateTranscriptDTOCache(changeSet);
	        
	    //Is all successful
	    boolean isAllSuccessful = isContextUpdated && isTransciptDTOUpdated;
	            
        //Commit to update the cursor
	    if(isAllSuccessful){	
	        changeSet.commit();
	    }
	    
	    return isAllSuccessful;
	}	
		
	
	/**
	 * Update context Map Cache for features such as TopLevelFeatures, Transcripts, and Gaps
	 * @param ChangeSet changeSet
	 *        The ChangeSet to be updated in the cache
	 * @return boolean 
	 *         Is update to context map cache successful
	 */
	private boolean updateContextMapCache(ChangeSet changeSet){
        //A set of processed features to avoid duplication of effort
        Set<Integer> processedFeatures = new HashSet<Integer>();
        
        //Add new top level features        
        boolean isAllAdded = addNewTopLevelFeatures(changeSet, processedFeatures);
        
        //Update changed top level features
        boolean isAllChanged = changeTopLevelFeatures(changeSet, processedFeatures);
        
        //Remove deleted top level features
        boolean isAllDeleted = removeTopLevelFeatures(changeSet, processedFeatures);
        
        //Is all successful
        return isAllAdded && isAllChanged && isAllDeleted;
	}
	
	/**
	 * Add the top level features. 
	 * Features to be added are determined by ToplevelFeatures added.
	 * @param changeSet
     *        The ChangeSet to be updated in the cache
	 * @param processedFeatures
     *        The processed changes to avoid duplication of effort
     * @return boolean Is update successful
	 */
	private boolean addNewTopLevelFeatures(ChangeSet changeSet, Set<Integer> processedFeatures){        
        //Add new top level features
	    Collection<Integer>topLevelFeatures = changeSet.newFeatureIds(TopLevelFeature.class);
	    return updateTopLevelFeatures(topLevelFeatures, TopLevelFeature.class, processedFeatures, false);
	}
    
    /**
     * Update the top level features. 
     * Features to be changed are determined by ToplevelFeatures, transcripts, and gaps just added.
     * @param changeSet
     *        The ChangeSet to be updated in the cache
     * @param processedFeatures
     *        The processed changes to avoid duplication of effort
     * @return boolean Is update successful
     */
    private boolean changeTopLevelFeatures(ChangeSet changeSet, Set<Integer> processedFeatures){        
        //Update top level features
        Collection<Integer>topLevelFeatures = changeSet.changedFeatureIds(TopLevelFeature.class);
        boolean isFindTopLevelUpdated = updateTopLevelFeatures(topLevelFeatures, TopLevelFeature.class, processedFeatures, true);
        
        //Update toplevelfeatures as a result of changed transcripts
        Collection<Integer>transcripts = changeSet.changedFeatureIds(Transcript.class);
        boolean isFindTranscriptUpdated = updateTopLevelFeatures(transcripts, Transcript.class, processedFeatures, true);

        //Update toplevelfeatures as a result of the changed gaps
        Collection<Integer>gaps = changeSet.changedFeatureIds(Gap.class);
        boolean isFindGapsUpdated = updateTopLevelFeatures(gaps, Gap.class, processedFeatures, true);
        
        //Is Find toplevel features updated
        return isFindTopLevelUpdated && isFindTranscriptUpdated && isFindGapsUpdated;
    }
    

    /**
     * Remove the context map from the cache    
     * @param changeSet
     * @param processedFeatures
     * @return
     */
    private boolean removeTopLevelFeatures(ChangeSet changeSet, Set<Integer> processedFeatures){
        boolean isAllRemoved = false;
        try{
            //Remove top level features
            Collection<Integer>topLevelFeatureIds = changeSet.deletedFeatureIds(TopLevelFeature.class);

            //Get the top level features to remove
            for(Integer featureId: topLevelFeatureIds){
                contextMapMap.remove(featureId);
            }
            isAllRemoved =true;
        }catch(Exception e){
            logger.error(e);
        }
        return isAllRemoved;
    }

	
	/**
	 * Populate the context map cache for the top level features
	 * @param featureIds
	 * @param Class
	 *         the class from which the toplevelfeature is to be drawn
     * @param processedFeatures
     *        The processed changes to avoid duplication of effort
     * @param Boolean isChanged
     *        isUpdate is false for new features and true for changed features
	 */
	private boolean updateTopLevelFeatures(Collection<Integer> featureIds, Class<? extends Feature> clazz, Set<Integer> processedFeatures, boolean isChanged){	
		//Get the top level features to add
	    if(featureIds.size()>0){
	        List<Feature> features = findTopLevelFeatures(featureIds, clazz);
	        boolean success = true;
	        for(Feature feature: features){
	            if(!processedFeatures.contains(new Integer(feature.getFeatureId()))){
                    //If updating the cache with a changed feature
                    if(isChanged){
                        contextMapMap.remove(feature.getFeatureId());
                        logger.info("Updating feature id: "+ feature.getFeatureId() + " (" +feature.getUniqueName()+ ")");
                    }
                    
                    //(Re)add the toplevelfeature to cache
	                if(!populateTopLevelFeatures(feature, processedFeatures)){
	                    success = false;//error found
	                }
	            }else{
	                logger.info(
	                        "Duplicate processing avoided for: " 
	                        + feature.getFeatureId() 
	                        +" (" 
	                        + feature.getUniqueName()
	                        +")");
	            }
	        }
	        return success;
	    }	    
	    return true;//No errors found since no processing is done	    
	}
	
	/**
	 * To populate the cache with the top level context map
	 * @param feature
     * @param processedFeatures
     *        The processed changes to avoid duplication of effort
     * @return Boolean
     *          Is processing a success
	 */
	private boolean populateTopLevelFeatures(Feature feature, Set<Integer> processedFeatures){
	    boolean success = false;
		try{
		    logger.debug("populateTopLevelFeatures, feature.getSeqLen:" 
		            + feature.getSeqLen() + " !isNoContextMap()" + !isNoContextMap());
		    
			if (!isNoContextMap() && feature.getSeqLen() > CacheDBHelper.MIN_CONTEXT_LENGTH_BASES) {
			    logger.debug("Trying to populate Top Level Feature ("
			            + feature.getFeatureId()+")"+ feature.getUniqueName());
				CacheDBHelper.populateContextMapCache(
            		feature, basicGeneService, renderedDiagramFactory, diagramCache, contextMapMap);
				
				//Add to the processd list
				processedFeatures.add(new Integer(feature.getFeatureId()));
			}else{
			    logger.warn("Top level Feature ID" 
			            +feature.getFeatureId() 
			            +" not populated due to isNoContextMap= "
			            + isNoContextMap()
			            +" flag or feature.getSeqLen()= "
			            +feature.getSeqLen()
			            +" is less than" 
			            + CacheDBHelper.MIN_CONTEXT_LENGTH_BASES);
			}
            success =true;
		}catch(Exception e){
			logger.error(e);
		}
		return success;
	}

	
	/**
	 * Find the Top Level Features with the uniquenames supplied
	 * @param featureIds
	 *     Ids of the TopLevelFeatures, Transcripts, or Gaps
	 * @return
	 */
	protected List<Feature> findTopLevelFeatures(Collection<Integer> featureIds, Class<? extends Feature> clazz){        
	    List<Feature> features = new ArrayList<Feature>(0);
		Query q = null;
		try{
		    Session session = SessionFactoryUtils.getSession(sessionFactory, false);	
		    if(clazz==TopLevelFeature.class){
		        q = session.createQuery(
		                "select f " +
		                " from Feature f" +
		        " where f.featureId in (:featureIds)")
		        .setParameterList("featureIds", featureIds);
		        
			}else if (clazz == Transcript.class){
                q = session.createQuery(
                        "select fl.sourceFeature " +
                        " from FeatureLoc fl " +
                " where f.feature in (:featureIds)")
                .setParameterList("featureIds", featureIds);
			    
			}else if(clazz == Gap.class){
                q = session.createQuery(
                        "select fl.sourceFeature " +
                        " from FeatureLoc fl " +
                " where f.feature in (:featureIds)")
                .setParameterList("featureIds", featureIds);
			    
			}
			features = q.list();	
			logger.debug("TopLevelFeature size: " + features.size() );
		}catch(Exception e){
			logger.error(e);
		}
        return features;		
	}
	    
    
    /**
     * Update the Transcript DTO for features such as the Genes, Transcripts and Polypeptides
     * @param ChangeSet changeSet
     *        The ChangeSet to be updated in the cache
     * @return boolean 
     *      Is update to DTO cache successful
     */
    private boolean updateTranscriptDTOCache(ChangeSet changeSet){
        //A set of processed features to avoid duplication of effort
        Set<Integer> processedTranscripts = new HashSet<Integer>();
     
        //Add new transcripts
        boolean isFindAnyAdded = addNewTranscriptDTO(changeSet, processedTranscripts);
     
        //Update changed transcripts
        boolean isFindAnyChanged = changeTranscriptDTO(changeSet, processedTranscripts);
     
        //Add new transcripts
        boolean isFindAnyDeleted = removeTranscriptDTO(changeSet, processedTranscripts);
        
        //Is FindAny successful
        return isFindAnyAdded && isFindAnyChanged && isFindAnyDeleted;
    }
	
	/**
	 * Add new transcript DTOs to cache
	 * @param changeSet
	 * @param processedFeatures
     *        The processed changes to avoid duplication of effort
     * @return Boolean
     *          Is processing a success
	 */
	private boolean addNewTranscriptDTO(ChangeSet changeSet, Set<Integer> processedFeatures){       
        //Add new transcripts
        Collection<Integer>transcripts = changeSet.newFeatureIds(Transcript.class);
        return  updateTranscriptDTO(transcripts, Transcript.class, processedFeatures, false);

	}
    
    /**
     * Change transcript DTOs in cache
     * @param changeSet
     * @param processedFeatures
     *        The processed changes to avoid duplication of effort
     * @return Boolean
     *          Is processing a success
     */
    private boolean changeTranscriptDTO(ChangeSet changeSet, Set<Integer> processedFeatures){       
        //Update transcripts
        Collection<Integer>transcripts = changeSet.changedFeatureIds(Transcript.class);
        boolean isFindAnyTranscriptsChanged = updateTranscriptDTO(transcripts, Transcript.class, processedFeatures, true);

        //Update transcripts as a result of new genes
        Collection<Integer>genes = changeSet.newFeatureIds(Gene.class);
        boolean isFindAnyGenesAdded = updateTranscriptDTO(genes, Gene.class, processedFeatures, false);

        //Update transcripts as a result of updated genes
        genes = changeSet.changedFeatureIds(Gene.class);
        boolean isFindAnyGenesChanged = updateTranscriptDTO(genes, Gene.class, processedFeatures, true);

        //Update transcripts as a result of the new polypeptides
        Collection<Integer>polypeptides = changeSet.newFeatureIds(Polypeptide.class);
        boolean isFindAnyPolypeptidesAdded = updateTranscriptDTO(polypeptides, Polypeptide.class, processedFeatures, false);

        //Update transcripts as a result of the new polypeptides
        polypeptides = changeSet.changedFeatureIds(Polypeptide.class);
        boolean isFindAnyPolypeptidesChanged = updateTranscriptDTO(polypeptides, Polypeptide.class, processedFeatures, true);
        
        //Is FindAny toplevel features added
        return isFindAnyTranscriptsChanged 
            && isFindAnyGenesAdded && isFindAnyGenesChanged 
            && isFindAnyPolypeptidesAdded && isFindAnyPolypeptidesChanged;        
    }
	
	/**
	 * Add new transcript
	 * @param feature
	 *     Transcript feature to be added, could be determined from a Transcript itself, a gene, or a polypeptide 
	 * @param clazz
	 * @param processedFeatures
     *        The processed transcript to avoid duplication of effort
	 * @param isChanged
     *        This is false for new transcrits and true for changed transcripts
	 * @return
	 */
	private boolean updateTranscriptDTO(Collection<Integer> featureIds, Class<? extends Feature> clazz, Set<Integer> processedFeatures, boolean isChanged){
	    boolean isUpdated = false;
	    try{
	        List<Transcript> transcripts = findTranscripts(featureIds, clazz);
	        for(Transcript transcript: transcripts){
	            TranscriptDTO dto = modelBuilder.prepareTranscript(transcript);
	            if(!isChanged){
	                dtoMap.put(new Integer(transcript.getFeatureId()), dto);   
	                logger.info("Added transcript ("+transcript.getFeatureId()+"), " + transcript.getUniqueName());
	            }else{
	                dtoMap.replace(new Integer(transcript.getFeatureId()), dto);
                    logger.info("Replaced transcript ("+transcript.getFeatureId()+"), " + transcript.getUniqueName());
	            }
	            //mark transcript as processed
	            processedFeatures.add(new Integer(transcript.getFeatureId()));
	        }
	        isUpdated = true;
	    }catch(Exception e){
	        logger.error(e);
	    }
	    return isUpdated;
	}
	
	/**
	 * Remove transcript
	 * @param featureId
	 */
	private boolean removeTranscriptDTO(ChangeSet changeSet, Set<Integer> processedFeatures){
        boolean isAllRemoved = false;
        try{
            //Remove transcript features
            Collection<Integer>transcriptFeatureIds = changeSet.deletedFeatureIds(Transcript.class);

            //Get the top level features to remove
            for(Integer featureId: transcriptFeatureIds){
                dtoMap.remove(featureId);
            }
            isAllRemoved =true;
        }catch(Exception e){
            logger.error(e);
        }
        return isAllRemoved;
	}
	
	/**
	 * Run the HQL to retrieve the relevant records
	 * @param featureIds
	 * @param clazz
	 * @return
	 */
	protected List<Transcript> findTranscripts(Collection<Integer> featureIds, Class<? extends Feature> clazz){
		List<Transcript> transcripts = new ArrayList<Transcript>(0);
		Query q = null;
		try{
			Session session = SessionFactoryUtils.getSession(sessionFactory, false);	
			if(clazz == Transcript.class ){
			    q = session.createQuery(
			        "select f " +
                    " from Feature f" +
                    " where f.featureId in (:featureIds)")
                    .setParameterList("featureIds", featureIds);
                @SuppressWarnings("unchecked")
                List<Feature> features = q.list();
                for(Feature feature: features){
                    if (feature instanceof Transcript){
                        transcripts.add((Transcript)feature);
                    }else{
                        logger.error("Error in query to find transcript, using Transcript Ids");
                    }
                }
			    
			}else if(clazz == Gene.class){
                q = session.createQuery(
                        "select f " +
                        " from Feature f" +
                        " where f.featureId in (:featureIds)")
                .setParameterList("featureIds", featureIds);
                @SuppressWarnings("unchecked")
                List<Feature> features = q.list();
                for(Feature feature: features){
                    if (feature instanceof AbstractGene){
                        transcripts.addAll(((AbstractGene)feature).getTranscripts());
                    }else{
                        logger.error("Error in query to find transcript, using Gene Ids");
                    }
                }
                
			}else if(clazz == Polypeptide.class){
                q = session.createQuery(
                        "select f " +
                        " from Feature f" +
                        " where f.featureId in (:featureIds)")
                .setParameterList("featureIds", featureIds);
                @SuppressWarnings("unchecked")
                List<Feature> features = q.list();
                for(Feature feature: features){
                    if(feature instanceof Polypeptide){
                        transcripts.add(((Polypeptide)feature).getTranscript());
                    }else{
                        logger.error("Error in query to find transcript, using Polypeptides Ids");
                    }
                }
			}
		}catch(Exception e){
			logger.error(e);
		}
        return transcripts;		
	}
	
	protected void init(){
        dtoMap = bmf.getDtoMap(); // TODO More nicely
        contextMapMap = bmf.getContextMapMap();

        LuceneIndex luceneIndex = luceneIndexFactory.getIndex("org.gmod.schema.mapped.Feature");
        basicGeneService = new BasicGeneServiceImpl(luceneIndex);
	}

	public boolean isNoContextMap() {
		return isNoContextMap;
	}

	public void setNoContextMap(boolean isNoContextMap) {
		this.isNoContextMap = isNoContextMap;
	}

	public BerkeleyMapFactory getBmf() {
		return bmf;
	}

    public void setBerkeleyMapFactory(BerkeleyMapFactory bmf) {
        this.bmf = bmf;
    }

	public BasicGeneService getBasicGeneService() {
		return basicGeneService;
	}

	public void setBasicGeneService(BasicGeneService basicGeneService) {
		this.basicGeneService = basicGeneService;
	}

	public RenderedDiagramFactory getRenderedDiagramFactory() {
		return renderedDiagramFactory;
	}

	public void setRenderedDiagramFactory(
			RenderedDiagramFactory renderedDiagramFactory) {
		this.renderedDiagramFactory = renderedDiagramFactory;
	}

	public ModelBuilder getModelBuilder() {
		return modelBuilder;
	}

	public void setModelBuilder(ModelBuilder modelBuilder) {
		this.modelBuilder = modelBuilder;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public ChangeTracker getChangeTracker() {
		return changeTracker;
	}

	public void setChangeTracker(ChangeTracker changeTracker) {
		this.changeTracker = changeTracker;
	}

    public DiagramCache getDiagramCache() {
        return diagramCache;
    }

    public void setDiagramCache(DiagramCache diagramCache) {
        this.diagramCache = diagramCache;
    }

    public LuceneIndexFactory getLuceneIndexFactory() {
        return luceneIndexFactory;
    }

    public void setLuceneIndexFactory(LuceneIndexFactory luceneIndexFactory) {
        this.luceneIndexFactory = luceneIndexFactory;
    }

}
