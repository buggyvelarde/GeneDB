package org.genedb.web.mvc.model;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.genedb.db.audit.MockChangeSetImpl;
import org.gmod.schema.feature.Transcript;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class PeriodicUpdaterTest extends AbstractUpdaterTest{

    @Autowired
    PeriodicUpdater periodicUpdater;
    
    @Test
    public void testChangeSetProcess()throws Exception{
        Integer newFeatureId = 7;
        Integer changedFeatureId = 14;
        Integer deletedFeatureId = 19;
        
        //Empty the cache
        CacheSynchroniser cacheSynchroniser = (CacheSynchroniser)periodicUpdater.getIndexUpdaters().get(1);
        cacheSynchroniser.getBmf().getDtoMap().clear();
        
        //Get the changeset
        MockChangeSetImpl changeSet = 
            (MockChangeSetImpl)periodicUpdater.getChangeTracker().changes(PeriodicUpdaterTest.class.getName());
        
        //Add new Transcript feature
        List<Integer> newFeatureIds = new ArrayList<Integer>();
        changeSet.getNewMap().put(Transcript.class, newFeatureIds); 
        newFeatureIds.add(newFeatureId);
        
        //Change  transcript feature
        List<Integer> changedFeatureIds = new ArrayList<Integer>();
        changeSet.getChangedMap().put(Transcript.class, changedFeatureIds); 
        changedFeatureIds.add(changedFeatureId);
        
        //Delete  transcript feature
        cacheSynchroniser.getBmf().getDtoMap().put(19, new TranscriptDTO());
        List<Integer> deletedFeatureIds = new ArrayList<Integer>();
        changeSet.getDeletedMap().put(Transcript.class, deletedFeatureIds); 
        deletedFeatureIds.add(deletedFeatureId);
        
        
        //execute class under test
        boolean noErrors = periodicUpdater.processChangeSet();
        
        //Transcript DTO with featureID 7 is not null
        TranscriptDTO transcriptDTO = cacheSynchroniser.getBmf().getDtoMap().get(newFeatureId);
        Assert.assertNotNull(transcriptDTO);
        
        //Transcript DTO with featureID 14 is not null
        transcriptDTO = cacheSynchroniser.getBmf().getDtoMap().get(changedFeatureId);
        Assert.assertNotNull(transcriptDTO);
        
        //Transcript DTO with featureID 19 IS null
        transcriptDTO = cacheSynchroniser.getBmf().getDtoMap().get(deletedFeatureId);
        Assert.assertNull(transcriptDTO);
        
        //No severe errors found
        Assert.assertTrue(noErrors);
    }
    
    /**
     * We know that the Transcript with feature ID 3 has a source feature, Top Level Feature with feature ID 1.
     * Hence, if the changeset returns a feature ID 3 as a changed transcript, 
     * we'll expect Top level Feature (feature ID 1) to be replaced. 
     * @throws Exception
     */
    public void testChangedTranscript()throws Exception{
        //Initialise changeset
        Integer featureId = 3;
        MockChangeSetImpl changeSet = 
            (MockChangeSetImpl)periodicUpdater.getChangeTracker().changes(PeriodicUpdaterTest.class.getName());
        changeSet.clearAll();    
        
        //Ensure that the TopLevelFeture with id 1 is not present in cache
        CacheSynchroniser cacheSynchroniser = (CacheSynchroniser)periodicUpdater.getIndexUpdaters().get(1);
        cacheSynchroniser.getBmf().getContextMapMap().clear();
        
        List<Integer> featureIds = new ArrayList<Integer>();
        featureIds.add(featureId);
        changeSet.getChangedMap().put(Transcript.class, featureIds); 
        
        //execute class under test
        boolean noErrors = periodicUpdater.processChangeSet();
        
        //Get the context that should have now been updated
        String contextMap = cacheSynchroniser.getBmf().getContextMapMap().get(1);
        
        //Transcript DTO is not null
        Assert.assertNotNull(contextMap);
        
        //No severe errors found
        Assert.assertTrue(noErrors);
        
    }
}
