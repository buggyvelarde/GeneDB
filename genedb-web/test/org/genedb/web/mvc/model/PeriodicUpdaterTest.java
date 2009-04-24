package org.genedb.web.mvc.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.genedb.db.audit.MockChangeSetImpl;
import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Gap;
import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.TopLevelFeature;
import org.gmod.schema.feature.Transcript;
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
@ContextConfiguration
public class PeriodicUpdaterTest extends AbstractUpdaterTest{


    @Autowired
    PeriodicUpdater periodicUpdater;
    
    
    @Test
    public void testTest() throws Exception{
        
        Integer changedPep = 614;//PFA0005w:pep
        Integer changedGeneId = 610;//PFA0005w
        String indexFilename = "test/data/lucene" + File.separatorChar + "org.gmod.schema.mapped.Feature";
        
        //Ensure those features that ought to be deleted by the Class Under Test are present in the Index
        IndexWriter destination = new IndexWriter(indexFilename, new SimpleAnalyzer());
        try{
            destination.deleteDocuments(new Term("featureId", Integer.toString(changedPep)));
            destination.deleteDocuments(new Term("featureId", Integer.toString(changedGeneId)));
        }finally{
            destination.close();            
        }
        
        //Get the changeset
        MockChangeSetImpl changeSet = 
            (MockChangeSetImpl)periodicUpdater.getChangeTracker().changes(PeriodicUpdaterTest.class.getName());
        
        //Change Polypeptide feature
        List<Integer> changedPolyPeps = new ArrayList<Integer>();
        changeSet.getChangedMap().put(Polypeptide.class, changedPolyPeps); 
        changedPolyPeps.add(changedPep);
        
        //Changed Gene feature to change set
        List<Integer> changedGeneIds = new ArrayList<Integer>();
        changeSet.getChangedMap().put(Gene.class, changedGeneIds); 
        changedGeneIds.add(changedGeneId);
        
        /****************************
         * Execute class under test 
         ****************************/
        boolean noErrors = periodicUpdater.processChangeSet();
    }
    
    //@Test
    public void testLuceneIndexPopulation()throws Exception{
        
        Integer newPolyPep = 810;//PFA0010c:pep
        //Integer changedPep = 614;//PFA0005w:pep
        Integer newGeneId = 2;//PFA0170c
        Integer changedGeneId = 610;//PFA0005w
        Integer newTranscriptId = 7;//PFA0315w:mRNA
        Integer changedTranscriptId = 14;//PFA0380w:mRNA
        Integer deletedTranscriptId = 19;//PFA0440w:mRNA
        
        //Ensure that documents to be added by the Class Under Test are absent from the Index
        //PopulateLuceneIndices pli = (PopulateLuceneIndices)periodicUpdater.getIndexUpdaters().get(0);
        //String indexFilename = pli.getIndexBaseDirectory() + File.separatorChar + "org.gmod.schema.mapped.Feature";
        String indexFilename = "test/data/lucene" + File.separatorChar + "org.gmod.schema.mapped.Feature";
        
        //Ensure those features that ought to be deleted by the Class Under Test are present in the Index
        IndexWriter destination = new IndexWriter(indexFilename, new SimpleAnalyzer());
        try{
            destination.deleteDocuments(new Term("featureId", Integer.toString(newPolyPep)));
            //destination.deleteDocuments(new Term("featureId", Integer.toString(changedPep)));
            destination.deleteDocuments(new Term("featureId", Integer.toString(newGeneId)));
            destination.deleteDocuments(new Term("featureId", Integer.toString(changedGeneId)));
            destination.deleteDocuments(new Term("featureId", Integer.toString(newTranscriptId)));
            destination.deleteDocuments(new Term("featureId", Integer.toString(changedTranscriptId)));
            
            Document doc = new Document();
            Fieldable field = new Field("featureId", Integer.toString(deletedTranscriptId), Field.Store.YES, Field.Index.UN_TOKENIZED);
            doc.add(field);
            destination.addDocument(doc);            
        }finally{
            destination.close();            
        }
        
        //Get the changeset
        MockChangeSetImpl changeSet = 
            (MockChangeSetImpl)periodicUpdater.getChangeTracker().changes(PeriodicUpdaterTest.class.getName());
        
        //Add new Polypeptide feature to change set
        List<Integer> newPolyPeps = new ArrayList<Integer>();
        changeSet.getNewMap().put(Polypeptide.class, newPolyPeps); 
        newPolyPeps.add(newPolyPep);
        
        //Change Polypeptide feature
        List<Integer> changedPolyPeps = new ArrayList<Integer>();
        changeSet.getChangedMap().put(Polypeptide.class, changedPolyPeps); 
        //changedPolyPeps.add(changedPep);
        
        //Add new Gene feature to change set
        List<Integer> newGeneIds = new ArrayList<Integer>();
        changeSet.getNewMap().put(Gene.class, newGeneIds); 
        newGeneIds.add(newGeneId);
        
        //Changed Gene feature to change set
        List<Integer> changedGeneIds = new ArrayList<Integer>();
        changeSet.getChangedMap().put(Gene.class, changedGeneIds); 
        changedGeneIds.add(changedGeneId);
        
        //Add new Transcript feature to change set
        List<Integer> newTranscriptIds = new ArrayList<Integer>();
        changeSet.getNewMap().put(Transcript.class, newTranscriptIds); 
        newTranscriptIds.add(newTranscriptId);
        
        //Changed  transcript feature to change set
        List<Integer> changedTranscriptIds = new ArrayList<Integer>();
        changeSet.getChangedMap().put(Transcript.class, changedTranscriptIds); 
        changedTranscriptIds.add(changedTranscriptId);
        
        //Deleted  transcript feature to change set
        List<Integer> deletedTranscriptIds = new ArrayList<Integer>();
        changeSet.getDeletedMap().put(Transcript.class, deletedTranscriptIds); 
        deletedTranscriptIds.add(deletedTranscriptId);
        
        
        //Assert that the transcript to be deleted by the Class Under Test is present in the Index
        IndexReader reader = IndexReader.open(indexFilename);
        try{
            TermDocs termDocs = reader.termDocs(new Term("featureId", Integer.toString(deletedTranscriptId)));
            Assert.assertTrue(termDocs.next());
        }finally{
            reader.close();
        }
        
        //Clear all the caches
        //prevent excessive log printing
        //CacheSynchroniser cacheSynchroniser = (CacheSynchroniser)periodicUpdater.getIndexUpdaters().get(1);
        //cacheSynchroniser.setNoPrintResult(true);
        
        
        
        
        /****************************
         * Execute class under test 
         ****************************/
        boolean noErrors = periodicUpdater.processChangeSet();
        
        //Access the Index Reader
        reader = IndexReader.open(indexFilename);
        try{
            //Assert new Polypeptide is present 
            TermDocs termDocs = reader.termDocs(new Term("featureId", Integer.toString(newPolyPep)));
            Assert.assertTrue(termDocs.next());
            Assert.assertFalse(termDocs.next());

            //Assert changed Polypeptide is present 
            //termDocs = reader.termDocs(new Term("featureId", Integer.toString(changedPep)));
            //Assert.assertTrue(termDocs.next());
            //Assert.assertFalse(termDocs.next());

            //Assert new gene is present 
            termDocs = reader.termDocs(new Term("featureId", Integer.toString(newGeneId)));
            Assert.assertTrue(termDocs.next());
            Assert.assertFalse(termDocs.next());

            //Assert changed gene is present 
            termDocs = reader.termDocs(new Term("featureId", Integer.toString(changedGeneId)));
            Assert.assertTrue(termDocs.next());
            Assert.assertFalse(termDocs.next());

            //Assert new transcript is present 
            termDocs = reader.termDocs(new Term("featureId", Integer.toString(newTranscriptId)));
            Assert.assertTrue(termDocs.next());
            Assert.assertFalse(termDocs.next());


            //Assert changed transcript is present 
            termDocs = reader.termDocs(new Term("featureId", Integer.toString(changedTranscriptId)));
            Assert.assertTrue(termDocs.next());
            Assert.assertFalse(termDocs.next());


            //Assert deleted transcript is NOT present 
            termDocs = reader.termDocs(new Term("featureId", Integer.toString(deletedTranscriptId)));
            Assert.assertFalse(termDocs.next());
        }finally{
            reader.close();
        }

        //Assert No severe errors found
        Assert.assertTrue(noErrors);

    }
    
    /**
     * Test the adding, replacement and removal of a ToplevelFeature
     * @throws Exception
     */
    //@Test
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
    
    /**
     * A changed Gap triggers a TopLevelFeature to be replaced
     * @throws Exception
     */
    //@Test
    public void testGapChangeSet()throws Exception{
        //Gap feature ID 17620's Unique name is 'gap116670-116769:corrected'
        //The corresponding TopLevelFeature for this is 15901(Pf3D7_07)
        Integer changedGap = 17620;
        
        //Clear all the caches
        CacheSynchroniser cacheSynchroniser = (CacheSynchroniser)periodicUpdater.getIndexUpdaters().get(1);
        cacheSynchroniser.getBmf().getDtoMap().clear();
        cacheSynchroniser.getBmf().getContextMapMap().clear();
        
        //prevent excessive log printing
        cacheSynchroniser.setNoPrintResult(true);
        
        //Get the changeset
        MockChangeSetImpl changeSet = 
            (MockChangeSetImpl)periodicUpdater.getChangeTracker().changes(PeriodicUpdaterTest.class.getName());
        
        //Change  Gap feature in change set
        List<Integer> changedFeatureIds = new ArrayList<Integer>();
        changeSet.getChangedMap().put(Gap.class, changedFeatureIds); 
        changedFeatureIds.add(changedGap);
        
        

        
        
        /****************************
         * Execute class under test 
         ****************************/
        boolean noErrors = periodicUpdater.processChangeSet();
        
        //ContextMap with featureID 15901 is not null
        String contextMap = cacheSynchroniser.getBmf().getContextMapMap().get(15901);
        Assert.assertNotNull(contextMap);
        Assert.assertTrue(contextMap.contains("Pf3D7_07"));
        
        //Assert No severe errors found
        Assert.assertTrue(noErrors);
        
    }
}
