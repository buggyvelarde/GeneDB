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
    
    private static String testingUniqueName;
    
    @BeforeClass
    public static void setup()throws Exception{        
          testingUniqueName = "TestGene" + String.valueOf(System.currentTimeMillis()).substring(5);  
    }
    
    @Test
    public void testTopLevelFeatureAdding()throws Exception{      
        
        //Generate the unique name to be used for the Gene in test
        MockChangeSetImpl changeSet = (MockChangeSetImpl)cacheSynchroniser.getChangeTracker().changes();     
        changeSet.getNewTopLevelNames().add(testingUniqueName);        

        //Start synching
        cacheSynchroniser.processRequest();
        
        //Find context map, using the generated gene unique name
        String context = ((CacheSynchTestDelegate)cacheSynchroniser).getContextMapMap().get(testingUniqueName);
        
        //Assert that context is added
        Assert.assertNotNull(context);
        
        //Assert that Gene unique name is found in context
        Assert.assertTrue(context.indexOf(testingUniqueName) != -1);
    }
    
    
    @Test
    public void testTopLevelFeatureRemoving()throws Exception{ 
        
        //Find context map, using the generated gene unique name
        String context = ((CacheSynchTestDelegate)cacheSynchroniser).getContextMapMap().get(testingUniqueName); 
        
        //Assert that context is added
        Assert.assertNotNull(context);
        
        //Assert that Gene unique name is found in context
        Assert.assertTrue(context.indexOf(testingUniqueName) != -1);  
        
        //Generate the unique name to be used for the Gene in test
        MockChangeSetImpl changeSet = (MockChangeSetImpl)cacheSynchroniser.getChangeTracker().changes();     
        changeSet.getDeletedTopLevelNames().add(testingUniqueName);        

        //Start synching
        cacheSynchroniser.processRequest();
        
        //Now, check again
        context = ((CacheSynchTestDelegate)cacheSynchroniser).getContextMapMap().get(testingUniqueName); 
        
        //Assert that context is added
        Assert.assertNull(context);
        
    }

    
    @Test
    public void testTranscriptAdding()throws Exception{      
        
        //Generate the unique name to be used for the Gene in test
        MockChangeSetImpl changeSet = (MockChangeSetImpl)cacheSynchroniser.getChangeTracker().changes();     
        changeSet.getNewTranscriptNames().add(testingUniqueName);        

        //Start synching
        cacheSynchroniser.processRequest();
        
        //Find context map, using the generated gene unique n  ame
        TranscriptDTO dto = ((CacheSynchTestDelegate)cacheSynchroniser).getDtoMap().get(testingUniqueName);
        
        //Assert that context is added
        Assert.assertNotNull(dto);
        
        //Assert that Gene unique name is found in context
        Assert.assertTrue(dto.getUniqueName().equals(testingUniqueName));
    }
}
