package org.genedb.web.mvc.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
 * @author sangerinstitute
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
    protected StoredMap<String, TranscriptDTO> dtoMap;
    protected StoredMap<String, String> contextMapMap;
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
			cacheSynchroniser.processRequest();
		
		}catch(Exception e){
			e.printStackTrace();
            System.err.println("Unable to run:");
            System.exit(64);			
		}
	}
	
	/**
	 * Find the audits for the 
	 */
	@Transactional
	void processRequest()throws SQLException{
		//Initialise
		init();
		
		//Get the records to update
		ChangeSet changeSet = changeTracker.changes();
		
		//Add new top level features
		List<String>topLevelFeatures = changeSet.newTopLevelFeatures();
		if(topLevelFeatures.size()>0){
			addNewTopLevelFeatures(topLevelFeatures);
		}
		
		//Update existing top level features
		List<String>changedLevelFeatures =changeSet.changedTopLevelFeatures(); 
		if(changedLevelFeatures.size()>0){
			updateTopLevelFeatures(changedLevelFeatures);
		}
		
		//Remove deleted top level features
		List<String>deletedTopLevelFeatures =changeSet.deletedTopLevelFeatures();
		if(deletedTopLevelFeatures.size()>0){
			removeTopLevelFeatures(deletedTopLevelFeatures);
		}
		
		//Add new transcripts
		List<String>newTranscripts = changeSet.newTranscripts();
		if(newTranscripts.size()>0){
			addNewTranscripts(newTranscripts);
		}
		
		//Update existing transcript
		List<String>changedTranscripts = changeSet.changedTranscripts();
		if(changedTranscripts.size()>0){
			updateTranscripts(changeSet.changedTranscripts());
		}
		
		//Remove deleted transcript
		List<String>deletedTranscripts = changeSet.deletedTranscripts();
		if(deletedTranscripts.size()>0){
			removeTranscripts(changeSet.deletedTranscripts());
		}
		
		//Commit to update the cursor
		changeSet.commit();
	}
	
	/**
	 * Populate the context map cache for the top level features
	 * @param uniqueNames
	 */
	private void addNewTopLevelFeatures(List<String> uniqueNames){	
		//Get the top level features to add
		List<Feature> features = findTopLevelFeatures(uniqueNames);
		for(Feature feature: features){
			populateTopLevelFeatures(feature);
		}
	}
	
	/**
	 * RE-populate the context map cache for the top level features
	 * @param uniqueNames
	 */
	private void updateTopLevelFeatures(List<String> uniqueNames){
		//Get the top level features to add
		List<Feature> features = findTopLevelFeatures(uniqueNames);
		for(Feature feature: features){
			contextMapMap.remove(feature.getUniqueName());
			populateTopLevelFeatures(feature);  
		}	

	}
	
	/**
	 * Remove the context map from the cache
	 * @param uniqueNames
	 */
	private void removeTopLevelFeatures(List<String> uniqueNames){

		//Get the top level features to add
		List<Feature> features = findTopLevelFeatures(uniqueNames);
		for(Feature feature: features){
			contextMapMap.remove(feature.getUniqueName());
		}

	}
	
	/**
	 * To populate the cache with the top level context map
	 * @param feature
	 */
	private void populateTopLevelFeatures(Feature feature){
		try{
			if (!isNoContextMap() && feature.getSeqLen() > CacheDBHelper.MIN_CONTEXT_LENGTH_BASES) {
				CacheDBHelper.populateContextMapCache(
            		feature, basicGeneService, renderedDiagramFactory, diagramCache, contextMapMap);
			}
		}catch(Exception e){
		    e.printStackTrace();
			logger.error("populateTopLevelFeatures: " + e.getMessage());
		}
	}

	
	/**
	 * Find the Top Level Features with the uniquenames supplied
	 * @param uniqueNames
	 * @return
	 */
	protected List<Feature> findTopLevelFeatures(List<String> uniqueNames){
		List<Feature> features = new ArrayList<Feature>(0);
		try{
			Session session = SessionFactoryUtils.getSession(sessionFactory, false);	
			Query q = session.createQuery(
					"select fp.feature" +
					" from FeatureProp fp" +
					" where fp.cvTerm.name = 'top_level_seq'" +
					" and fp.cvTerm.cv.name = 'genedb_misc'" +
			" and fp.feature.uniquename in (:uniqueNames)")
			.setString("uniqueNames", concatNames(uniqueNames));			
			features = q.list();			
		}catch(Exception e){
		    e.printStackTrace();
			logger.error("findTopLevelFeatures: " + e.getMessage());
		}
        return features;		
	}
	
	/**
	 * 
	 * @param names
	 * @return
	 */
	private String concatNames(List<String> names){
		String nameConcat = "";
		for(String name: names){
			nameConcat = nameConcat + ", " +  name;
		}
		return nameConcat;
	}
	
	/**
	 * Add new transcript
	 * @param uniqueNames
	 */
	private void addNewTranscripts(List<String> uniqueNames){
		List<Transcript> transcripts = findTranscripts(uniqueNames);
		for(Transcript transcript: transcripts){
            TranscriptDTO dto = modelBuilder.prepareTranscript(transcript);
            dtoMap.put(transcript.getUniqueName(), dto);
		}
	}
	
	/**
	 * Update transcript
	 * @param uniqueNames
	 */
	private void updateTranscripts(List<String> uniqueNames){
		List<Transcript> transcripts = findTranscripts(uniqueNames);
		for(Transcript transcript: transcripts){
            TranscriptDTO dto = modelBuilder.prepareTranscript(transcript);
            dtoMap.replace(transcript.getUniqueName(), dto);
		}
	}
	
	/**
	 * Remove transcript
	 * @param uniqueNames
	 */
	private void removeTranscripts(List<String> uniqueNames){
		List<Transcript> transcripts = findTranscripts(uniqueNames);
		for(Transcript transcript: transcripts){
            dtoMap.remove(transcript.getUniqueName());
		}
	}
	
	
	protected List<Transcript> findTranscripts(List<String> uniqueNames){
		List<Transcript> transcripts = new ArrayList<Transcript>(0);
		try{
			Session session = SessionFactoryUtils.getSession(sessionFactory, false);	
			Query q = session.createQuery(
                "select fp.feature" +
                " from FeatureProp fp" +
                " where fp.cvTerm.name = 'top_level_seq'" +
                " and fp.cvTerm.cv.name = 'genedb_misc'" +
                " and fp.feature.uniquename in (:uniqueNames)")
            .setString("uniqueNames", concatNames(uniqueNames));
		
			transcripts = q.list();
		}catch(Exception e){
		    e.printStackTrace();
			logger.error("findTranscripts: " + e.getMessage());
		}
        return transcripts;		
	}
	
	protected void init(){
        dtoMap = bmf.getDtoMap(); // TODO More nicely
        contextMapMap = bmf.getContextMapMap();

        LuceneIndex luceneIndex = luceneIndexFactory.getIndex("org.gmod.schema.mapped.Feature");
        basicGeneService = new BasicGeneServiceImpl(luceneIndex);
	}
	
	/**
	 * 
	 * @param geneUniqueName
	 * @return
	 */
	private Collection<Transcript> findTranscripts(String geneUniqueName){

		AbstractGene gene = CacheDBHelper.findGene(geneUniqueName, sessionFactory);
        if (gene == null) {
            logger.error("Could not find gene with uniqueName '"
                + geneUniqueName + "'");
        } 
		return gene.getTranscripts();
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

}
