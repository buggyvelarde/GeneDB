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
        //Initialise changeset
        Integer featureId = 10;
        MockChangeSetImpl changeSet = 
            (MockChangeSetImpl)periodicUpdater.getChangeTracker().changes(PeriodicUpdaterTest.class.getName());
        List<Integer> featureIds = new ArrayList<Integer>();
        featureIds.add(featureId);
        changeSet.getNewMap().put(Transcript.class, featureIds); 
        
        //execute class under test
        boolean noErrors = periodicUpdater.processChangeSet();
        
        CacheSynchroniser cacheSynchroniser = (CacheSynchroniser)periodicUpdater.getIndexUpdaters().get(1);
        TranscriptDTO transcriptDTO = cacheSynchroniser.getBmf().getDtoMap().get(featureId);
        
        //Transcript DTO is not null
        Assert.assertNotNull(transcriptDTO);
        
        
        
        //Remove the featureId = 10 from the change set changeset 
        changeSet.clearAll();        
        changeSet.getDeletedMap().put(Transcript.class, featureIds);
        
        //execute class under test
        noErrors = periodicUpdater.processChangeSet();
        transcriptDTO = cacheSynchroniser.getBmf().getDtoMap().get(featureId);
        
        //Transcript DTO should now be null
        Assert.assertNull(transcriptDTO);
        
        //No severe errors found
        Assert.assertTrue(noErrors);
    }
}
