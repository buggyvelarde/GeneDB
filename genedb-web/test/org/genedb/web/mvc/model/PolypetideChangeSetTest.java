package org.genedb.web.mvc.model;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.genedb.db.audit.MockChangeSetImpl;
import org.gmod.schema.feature.Polypeptide;
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
public class PolypetideChangeSetTest  extends AbstractUpdaterTest{


    @Autowired
    PeriodicUpdater periodicUpdater;

    
    /**
     * 1.Start by clearing all the caches
     * 2.Initialise the ChangeSet with the polypeptide Ids to be used with various tests
     * @throws Exception
     */
    @Test
    public void testPolypeptideChangeSet()throws Exception{
        Integer newPolyPep = 810;//PFA0010c:pep
        Integer changedPep = 614;//PFA0005w:pep
        
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
        changeSet.getNewMap().put(Polypeptide.class, newFeatureIds); 
        newFeatureIds.add(newPolyPep);
        
        //Change Gene feature
        List<Integer> changedFeatureIds = new ArrayList<Integer>();
        changeSet.getChangedMap().put(Polypeptide.class, changedFeatureIds); 
        changedFeatureIds.add(changedPep);

        
        
        /****************************
         * Execute class under test 
         ****************************/
        boolean noErrors = periodicUpdater.processChangeSet();
        
        //Assert Transcript DTO with featureID 807 is not null
        //ID 807(PFA0010c:mRNA) is a transcript of Polypeptide 810(PFA0010c:pep)
        TranscriptDTO transcriptDTO = cacheSynchroniser.getBmf().getDtoMap().get(807);
        Assert.assertNotNull(transcriptDTO);
        Assert.assertEquals("PFA0010c:mRNA", transcriptDTO.getUniqueName());
        
        //Assert Transcript DTO with featureID 611 is not null
        //ID 611(PFA0005w:mRNA) is a transcript of Polypeptide 614(PFA0005w:pep)
        transcriptDTO = cacheSynchroniser.getBmf().getDtoMap().get(611);
        Assert.assertNotNull(transcriptDTO);
        Assert.assertEquals("PFA0005w:mRNA", transcriptDTO.getUniqueName());
        
        //Assert No severe errors found
        Assert.assertTrue(noErrors);
    }

}
