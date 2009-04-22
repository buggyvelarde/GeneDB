package org.genedb.web.mvc.model;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.genedb.db.audit.MockChangeSetImpl;
import org.gmod.schema.feature.Gene;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;




/**
 * To see the final results of the method under test, simply set the cacheSynchroniser.setNoPrintResult(false); 
 * @author larry@sangerinstitute
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/org/genedb/web/mvc/model/PeriodicUpdaterTest-context.xml"})
public class GeneChangeSetTest  extends AbstractUpdaterTest{


    @Autowired
    PeriodicUpdater periodicUpdater;

    
    /**
     * 1.Start by clearing all the caches
     * 2.Initialise the ChangeSet with the transcript Ids to be used with various tests
     * @throws Exception
     */
    @Test
    public void testGeneChangeSet()throws Exception{
        Integer newGeneId = 2;//PFA0170c
        Integer changedGeneId = 610;//PFA0005w
        
        //Clear all the caches
        CacheSynchroniser cacheSynchroniser = (CacheSynchroniser)periodicUpdater.getIndexUpdaters().get(1);
        cacheSynchroniser.getBmf().getDtoMap().clear();
        cacheSynchroniser.getBmf().getContextMapMap().clear();
        
        //prevent excessive log printing
        cacheSynchroniser.setNoPrintResult(true);
        
        //Get the changeset
        MockChangeSetImpl changeSet = 
            (MockChangeSetImpl)periodicUpdater.getChangeTracker().changes(PeriodicUpdaterTest.class.getName());
        
        //Add new Gene feature to change set
        List<Integer> newFeatureIds = new ArrayList<Integer>();
        changeSet.getNewMap().put(Gene.class, newFeatureIds); 
        newFeatureIds.add(newGeneId);
        
        //Change Gene feature in change set
        List<Integer> changedFeatureIds = new ArrayList<Integer>();
        changeSet.getChangedMap().put(Gene.class, changedFeatureIds); 
        newFeatureIds.add(changedGeneId);
        
        

        
        
        /****************************
         * Execute class under test 
         ****************************/
        boolean noErrors = periodicUpdater.processChangeSet();
        
        //Assert Transcript DTO with featureID 3 is not null
        //ID 3(PFA0170c:mRNA) is a transcript of Gene ID 2(PFA0170c)
        TranscriptDTO transcriptDTO = cacheSynchroniser.getBmf().getDtoMap().get(3);
        Assert.assertNotNull(transcriptDTO);
        Assert.assertEquals("PFA0170c:mRNA", transcriptDTO.getUniqueName());
        
        //Assert Transcript DTO with featureID 611 is not null
        //ID 611(PFA0005w:mRNA) is a transcript of Gene ID 610(PFA0005w)
        transcriptDTO = cacheSynchroniser.getBmf().getDtoMap().get(611);
        Assert.assertNotNull(transcriptDTO);
        Assert.assertEquals("PFA0005w:mRNA", transcriptDTO.getUniqueName());
        
        //Assert No severe errors found
        Assert.assertTrue(noErrors);
        
    }

}
