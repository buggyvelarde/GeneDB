package org.genedb.web.mvc.model;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.genedb.db.audit.MockChangeSetImpl;
import org.gmod.schema.feature.TopLevelFeature;
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
public class TopLevelFeatureChangeSetTest extends AbstractUpdaterTest{


    @Autowired
    PeriodicUpdater periodicUpdater;

    
    /**
     * Test the adding, replacement and removal of a ToplevelFeature
     * @throws Exception
     */
    @Test
    public void testTopLevelFeatureChangeSet()throws Exception{
        Integer newTopLevelFeature = 1;//Pf3D7_01
        Integer changedTopLevelFeature = 886;//Pf3D7_02
        Integer deletedTopLevelFeature = 9493;//Pf3D7_03
        
        //Clear all the caches
        CacheSynchroniser cacheSynchroniser = (CacheSynchroniser)periodicUpdater.getIndexUpdaters().get(1);
        cacheSynchroniser.getBmf().getDtoMap().clear();
        cacheSynchroniser.getBmf().getContextMapMap().clear();
        
        //prevent excessive log printing
        cacheSynchroniser.setNoPrintResult(true);
        
        //Get the changeset
        MockChangeSetImpl changeSet = 
            (MockChangeSetImpl)periodicUpdater.getChangeTracker().changes(PeriodicUpdaterTest.class.getName());
        
        //Add new Transcript feature to change set
        List<Integer> newFeatureIds = new ArrayList<Integer>();
        changeSet.getNewMap().put(TopLevelFeature.class, newFeatureIds); 
        newFeatureIds.add(newTopLevelFeature);
        
        //Change  transcript feature in change set
        List<Integer> changedFeatureIds = new ArrayList<Integer>();
        changeSet.getChangedMap().put(TopLevelFeature.class, changedFeatureIds); 
        changedFeatureIds.add(changedTopLevelFeature);
        
        //Delete  transcript feature from change set
        cacheSynchroniser.getBmf().getContextMapMap().put(deletedTopLevelFeature, "test test test");
        List<Integer> deletedFeatureIds = new ArrayList<Integer>();
        changeSet.getDeletedMap().put(TopLevelFeature.class, deletedFeatureIds); 
        deletedFeatureIds.add(deletedTopLevelFeature);
        
        

        
        
        /****************************
         * Execute class under test 
         ****************************/
        boolean noErrors = periodicUpdater.processChangeSet();
        
        //ContextMap with featureID 1 is not null
        String contextMap = cacheSynchroniser.getBmf().getContextMapMap().get(newTopLevelFeature);
        Assert.assertNotNull(contextMap);
        Assert.assertTrue(contextMap.contains("Pf3D7_01"));
        
        //ContextMap with featureID 886 is not null
        contextMap = cacheSynchroniser.getBmf().getContextMapMap().get(changedTopLevelFeature);
        Assert.assertNotNull(contextMap);
        Assert.assertTrue(contextMap.contains("Pf3D7_02"));

        
        //Assert Transcript DTO with featureID 19 IS null
        contextMap = cacheSynchroniser.getBmf().getContextMapMap().get(deletedTopLevelFeature);
        Assert.assertNull(contextMap);
        
        //Assert No severe errors found
        Assert.assertTrue(noErrors);
    }

}
