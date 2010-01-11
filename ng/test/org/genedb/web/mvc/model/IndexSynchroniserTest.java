package org.genedb.web.mvc.model;

import org.genedb.db.audit.MockChangeSetImpl;

import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.Transcript;

import org.apache.log4j.PropertyConfigurator;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
/**
 *
 * @author larry@sangerinstitute
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class IndexSynchroniserTest {
    @Autowired
    IndexSynchroniser indexSynchroniser;

    @Before
    public void setUpLogging() {
        String log4jprops = "/log4j.periodicUpdaterTest.properties";
        URL url = this.getClass().getResource(log4jprops);
        System.out.printf("Configuring Log4J from '%s'\n", url);
        PropertyConfigurator.configure(url);
    }

    @Before
    public void purgeIndex(){
        indexSynchroniser.purgeAll();
    }


    @Test
    public void testLuceneIndexPopulation()throws Exception{

        Integer newPolyPep = 810;//PFA0010c:pep
        Integer changedPep = 614;//PFA0005w:pep
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
        //PopulateLuceneIndices populateLuceneIndices = (PopulateLuceneIndices)periodicUpdater.getIndexUpdaters().get(0);
        //populateLuceneIndices.indexSingle(deletedTranscriptId);
        indexSynchroniser.indexSingle(deletedTranscriptId);

        //Create the changeset
        MockChangeSetImpl changeSet = new MockChangeSetImpl();

        //Add new Polypeptide feature to change set
        List<Integer> newPolyPeps = new ArrayList<Integer>();
        changeSet.getNewMap().put(Polypeptide.class, newPolyPeps);
        newPolyPeps.add(newPolyPep);

        //Change Polypeptide feature
        List<Integer> changedPolyPeps = new ArrayList<Integer>();
        changeSet.getChangedMap().put(Polypeptide.class, changedPolyPeps);
        changedPolyPeps.add(changedPep);

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




        /****************************
         * Execute class under test
         ****************************/
        boolean noErrors = indexSynchroniser.updateAllCaches(changeSet);

        //Access the Index Reader
        reader = IndexReader.open(indexFilename);
        try{
            //Assert new Polypeptide is present
            TermDocs termDocs = reader.termDocs(new Term("featureId", Integer.toString(newPolyPep)));
            Assert.assertTrue(termDocs.next());
            Assert.assertFalse(termDocs.next());

            //Assert changed Polypeptide is present
            termDocs = reader.termDocs(new Term("featureId", Integer.toString(changedPep)));
            Assert.assertTrue(termDocs.next());
            Assert.assertFalse(termDocs.next());

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

}
