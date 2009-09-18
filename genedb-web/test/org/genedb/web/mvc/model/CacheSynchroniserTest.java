package org.genedb.web.mvc.model;

import java.util.ArrayList;
import java.util.List;

import org.genedb.db.audit.MockChangeSetImpl;
import org.gmod.schema.feature.Gap;
import org.gmod.schema.feature.TopLevelFeature;
import org.gmod.schema.feature.Transcript;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;




@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class CacheSynchroniserTest extends AbstractUpdaterTest{
    Logger logger = Logger.getLogger(CacheSynchroniserTest.class);
    @Autowired
    private CacheSynchroniser cacheSynchroniser;


    /**
     * For the purpose of testing, let the featureId Integer type be used by conversion to generate the featureId and uniqueName String type
     */
    //private Integer featureId;

    @After
    public void clearUpCaches()throws Exception{
        logger.info("Clearing cache");
        MockChangeSetImpl changeSet = (MockChangeSetImpl)
            cacheSynchroniser.getChangeTracker().changes(CacheSynchroniserTest.class.getName());
        changeSet.clearAll();

        //Clear cache
        ((CacheSynchTestDelegate)cacheSynchroniser).getContextMapMap().clear();
        ((CacheSynchTestDelegate)cacheSynchroniser).getContextImageMap().clear();

        //Reset th elog info
        ((CacheSynchTestDelegate)cacheSynchroniser).getChangeSetInfo().setLength(0);
    }

    @Test
    public void testTopLevelFeatureAdding()throws Exception{

        //Set up the ChangeSet
        Integer featureId = 10;
        MockChangeSetImpl changeSet = (MockChangeSetImpl)
            cacheSynchroniser.getChangeTracker().changes(CacheSynchroniserTest.class.getName());
        List<Integer> featureIds = new ArrayList<Integer>();
        featureIds.add(featureId);
        changeSet.getNewMap().put(TopLevelFeature.class, featureIds);

        //Start synching
        boolean noErrors = cacheSynchroniser.updateAllCaches(changeSet);

        //Find context map, using the generated id
        String context = ((CacheSynchTestDelegate)cacheSynchroniser).getContextMapMap().get(featureId);

        //Assert that context is added
        Assert.assertNotNull(context);

        //Assert that Gene unique name is found in context
        String idStr = featureId.toString();
        Assert.assertTrue(context.indexOf(idStr) != -1);

        //Assert the hidden errors were encountered
        Assert.assertTrue("Errors found during processing", noErrors);
    }

    @Test
    public void testTopLevelFeatureChanging()throws Exception{

      //Create changed top level feature
        Integer featureId1 = 20;
        MockChangeSetImpl changeSet = (MockChangeSetImpl)
            cacheSynchroniser.getChangeTracker().changes(CacheSynchroniserTest.class.getName());
        List<Integer> featureIds = new ArrayList<Integer>();
        featureIds.add(featureId1);
        changeSet.getChangedMap().put(TopLevelFeature.class, featureIds);

        //Create changed transcript that causes a TopLevelFeature to be changed
        Integer featureId2 = 30;
        featureIds = new ArrayList<Integer>();
        featureIds.add(featureId2);
        changeSet.getChangedMap().put(Transcript.class, featureIds);

        //Create changed gap that causes a TopLevelFeature to be changed
        Integer featureId3 = 40;
        featureIds = new ArrayList<Integer>();
        featureIds.add(featureId3);
        changeSet.getChangedMap().put(Gap.class, featureIds);

        //This sshould not change since the featureId is 20 like featureId1
        //****processing should be skipped*****
        Integer featureId4 = 20;
        featureIds = new ArrayList<Integer>();
        featureIds.add(featureId3);
        changeSet.getChangedMap().get(Gap.class).add(featureId4);//adding

        //Start synching
        boolean noErrors = cacheSynchroniser.updateAllCaches(changeSet);

        //Find context map, using the generated id
        String context = ((CacheSynchTestDelegate)cacheSynchroniser).getContextMapMap().get(featureId1);
        //Assert that context is added
        Assert.assertNotNull(context);



        //Find context map, using the generated id
        context = ((CacheSynchTestDelegate)cacheSynchroniser).getContextMapMap().get(featureId2);
        //Assert that context is added
        Assert.assertNotNull(context);



        //Find context map, using the generated id
        context = ((CacheSynchTestDelegate)cacheSynchroniser).getContextMapMap().get(featureId3);
        //Assert that context is added
        Assert.assertNotNull(context);

        //Assert that Gene unique name is found in context
        String idStr = featureId1.toString();
        Assert.assertTrue(context.indexOf(idStr) != -1);

        //Assert that size of cache is 3, this is because the 4th entry is a duplicate
        Assert.assertEquals(
                ((CacheSynchTestDelegate)cacheSynchroniser).getContextMapMap().size(), 3);

        //Assert the hidden errors were encountered
        Assert.assertTrue("Errors found during processing", noErrors);
    }


    @Test
    public void testTopLevelFeatureRemoving()throws Exception{

        //Set up the ChangeSet
        Integer featureId = 100;
        MockChangeSetImpl changeSet = (MockChangeSetImpl)
            cacheSynchroniser.getChangeTracker().changes(CacheSynchroniserTest.class.getName());
        List<Integer> featureIds = new ArrayList<Integer>();
        featureIds.add(featureId);
        changeSet.getNewMap().put(TopLevelFeature.class, featureIds);

        //Start synching
        boolean noErrors = cacheSynchroniser.updateAllCaches(changeSet);

        //Find context map, using the generated gene unique name
        String context = ((CacheSynchTestDelegate)cacheSynchroniser).getContextMapMap().get(featureId);

        //Assert that context exists
        Assert.assertNotNull(context);

        //Assert that Gene unique name is found in context
        Assert.assertTrue(context.indexOf(featureId) != -1);

        //Generate the unique name to be used for the Gene in test
        changeSet = (MockChangeSetImpl)cacheSynchroniser.getChangeTracker().changes(CacheSynchroniserTest.class.getName());
        featureIds = new ArrayList<Integer>();
        featureIds.add(featureId);
        changeSet.getDeletedMap().put(TopLevelFeature.class, featureIds);

        //Start synching
      //execute class under test
        cacheSynchroniser.updateAllCaches(changeSet);

        //Now, check again
        context = ((CacheSynchTestDelegate)cacheSynchroniser).getContextMapMap().get(featureId);

        //Assert that context is removed
        Assert.assertNull(context);

        //Assert the hidden errors were encountered
        Assert.assertTrue("Errors found during processing", noErrors);
    }


    @Test
    public void testTranscriptAdding()throws Exception{

        //Set up the ChangeSet
        Integer featureId = 70;
        //Generate the unique name to be used for the Gene in test
        MockChangeSetImpl changeSet = (MockChangeSetImpl)cacheSynchroniser.getChangeTracker().changes(CacheSynchroniserTest.class.getName());
        List<Integer> featureIds = new ArrayList<Integer>();
        featureIds.add(featureId);
        changeSet.getNewMap().put(Transcript.class, featureIds);

        //Start synching
      //execute class under test
        boolean noErrors = cacheSynchroniser.updateAllCaches(changeSet);

        //Find Transcript DTO, using the generated gene unique n  ame
        TranscriptDTO dto = ((CacheSynchTestDelegate)cacheSynchroniser).getDtoMap().get(featureId);

        //Assert that context is added
        Assert.assertNotNull(dto);

        //Assert that Gene unique name is found in context
        Assert.assertEquals(dto.getUniqueName(), featureId.toString());

        //Assert the hidden errors were encountered
        Assert.assertTrue("Errors found during processing", noErrors);
    }


    @Test
    public void testTranscriptRemoving()throws Exception{

        //Set up the ChangeSet
        Integer featureId = 124;

        //Assert that no DTO
        TranscriptDTO dto = ((CacheSynchTestDelegate)cacheSynchroniser).getDtoMap().get(featureId);
        Assert.assertNull(dto);

        MockChangeSetImpl changeSet = (MockChangeSetImpl)
            cacheSynchroniser.getChangeTracker().changes(CacheSynchroniserTest.class.getName());
        List<Integer> featureIds = new ArrayList<Integer>();
        featureIds.add(featureId);
        changeSet.getNewMap().put(Transcript.class, featureIds);
        //Initialise DTO cache
        boolean noErrors = cacheSynchroniser.updateAllCaches(changeSet);

        //Find Transcript DTO, using the generated gene unique name
        dto = ((CacheSynchTestDelegate)cacheSynchroniser).getDtoMap().get(featureId);


        //Assert that DTO exists
        Assert.assertNotNull(dto);

        //Generate the unique name to be used for the Gene in test
        changeSet = (MockChangeSetImpl)cacheSynchroniser.getChangeTracker().changes(CacheSynchroniserTest.class.getName());
        featureIds = new ArrayList<Integer>();
        featureIds.add(featureId);
        changeSet.getDeletedMap().put(Transcript.class, featureIds);

        //Start synching
      //execute class under test
        noErrors = cacheSynchroniser.updateAllCaches(changeSet);

        //Now, check again
        dto = ((CacheSynchTestDelegate)cacheSynchroniser).getDtoMap().get(featureId);

        //Assert that DTO is removed
        Assert.assertNull(dto);

        //Assert the hidden errors were encountered
        Assert.assertTrue("Errors found during processing", noErrors);

    }
}
