package org.genedb.web.mvc.model;

import org.genedb.db.audit.MockChangeSetImpl;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class CacheSynchroniserTest{
    @Autowired
    private CacheSynchroniser cacheSynchroniser;
    
    /**
     * For the purpose of testing, let the featureId Integer type be used by conversion to generate the uniqueName String type
     */
    private static Integer featureId;
    
    @BeforeClass
    public static void setup()throws Exception{        
          Long id = new Long(System.currentTimeMillis());
          featureId = id.intValue();
    }
    
    @Test
    public void testTopLevelFeatureAdding()throws Exception{      
        
        //Generate the unique name to be used for the Gene in test
        MockChangeSetImpl changeSet = (MockChangeSetImpl)cacheSynchroniser.getChangeTracker().changes();     
        changeSet.getNewTopLevelIds().add(featureId);        

        //Start synching
        cacheSynchroniser.processRequest();
        
        //Find context map, using the generated gene unique name
        String context = ((CacheSynchTestDelegate)cacheSynchroniser).getContextMapMap().get(String.valueOf(featureId));
        
        //Assert that context is added
        Assert.assertNotNull(context);
        
        //Assert that Gene unique name is found in context
        Assert.assertTrue(context.indexOf(String.valueOf(featureId)) != -1);
    }
    
    
    @Test
    public void testTopLevelFeatureRemoving()throws Exception{ 
        
        //Find context map, using the generated gene unique name
        String context = ((CacheSynchTestDelegate)cacheSynchroniser).getContextMapMap().get(String.valueOf(featureId)); 
        
        //Assert that context exists
        Assert.assertNotNull(context);
        
        //Assert that Gene unique name is found in context
        Assert.assertTrue(context.indexOf(String.valueOf(featureId)) != -1);  
        
        //Generate the unique name to be used for the Gene in test
        MockChangeSetImpl changeSet = (MockChangeSetImpl)cacheSynchroniser.getChangeTracker().changes();     
        changeSet.getDeletedTopLevelIds().add(featureId);        

        //Start synching
        cacheSynchroniser.processRequest();
        
        //Now, check again
        context = ((CacheSynchTestDelegate)cacheSynchroniser).getContextMapMap().get(String.valueOf(featureId)); 
        
        //Assert that context is removed
        Assert.assertNull(context);
        
    }

    
    @Test
    public void testTranscriptAdding()throws Exception{      
        
        //Generate the unique name to be used for the Gene in test
        MockChangeSetImpl changeSet = (MockChangeSetImpl)cacheSynchroniser.getChangeTracker().changes();     
        changeSet.getNewTranscriptIds().add(featureId);        

        //Start synching
        cacheSynchroniser.processRequest();
        
        //Find Transcript DTO, using the generated gene unique n  ame
        TranscriptDTO dto = ((CacheSynchTestDelegate)cacheSynchroniser).getDtoMap().get(String.valueOf(featureId));
        
        //Assert that context is added
        Assert.assertNotNull(dto);
        
        //Assert that Gene unique name is found in context
        Assert.assertTrue(dto.getUniqueName().equals(String.valueOf(featureId)));
    }
    
    
    @Test
    public void testTranscriptRemoving()throws Exception{ 
        
        //Find Transcript DTO, using the generated gene unique name
        TranscriptDTO dto = ((CacheSynchTestDelegate)cacheSynchroniser).getDtoMap().get(String.valueOf(featureId)); 
        
        //Assert that DTO exists  
        Assert.assertNotNull(dto);
        
        //Assert that Gene unique name is found in DTO
        Assert.assertTrue(dto.getUniqueName().equals(String.valueOf(featureId)));
        
        //Generate the unique name to be used for the Gene in test
        MockChangeSetImpl changeSet = (MockChangeSetImpl)cacheSynchroniser.getChangeTracker().changes();     
        changeSet.getDeletedTranscriptIds().add(featureId);        

        //Start synching
        cacheSynchroniser.processRequest();
        
        //Now, check again
        dto = ((CacheSynchTestDelegate)cacheSynchroniser).getDtoMap().get(String.valueOf(featureId)); 
        
        //Assert that DTO is removed
        Assert.assertNull(dto);
        
    }
}
