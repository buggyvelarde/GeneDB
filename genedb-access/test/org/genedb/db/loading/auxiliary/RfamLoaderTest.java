package org.genedb.db.loading.auxiliary;

import static org.junit.Assert.*;

import org.genedb.db.loading.RfamLoader;

import org.gmod.schema.feature.Chromosome;
import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Analysis;
import org.gmod.schema.mapped.AnalysisFeature;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.FeatureLoc;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Tests the rfamloader class. At the moment, we have created a fictititous set of rfam results for pfalciparum so 
 * that the tests will work against the hsql pfalciparum database. Once rfam is run against pfalciparum for real, this
 * test file should be replaced.
 * 
 * @author nds
 */

public class RfamLoaderTest {
    //Constants
    private static final Logger logger = Logger.getLogger(RfamLoaderTest.class);
    
    //Configurable
    private static RfamLoader loader;
    private Gene gene;
    private Transcript transcript;
    private static Chromosome chromosome; 
    private Analysis analysis;
    private static SessionFactory sessionFactory; //Is there a benefit of declaring this inside each method like in InterproTest
    private static Session session;
    private static String analysisProgramVersion = "unknown"; //Add valid version when known

    @BeforeClass
    public static void setup() throws IOException, HibernateException, SQLException, ClassNotFoundException {
        
        ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] {"Load.xml", "AuxTest.xml"});
        loader = ctx.getBean("rfamloader", RfamLoader.class);
        assertTrue(loader.processOptionIfValid("rfam-version", analysisProgramVersion)); 
        
        sessionFactory = loader.getSessionFactory();
        session = SessionFactoryUtils.getSession(sessionFactory, true);
        
        new ClearRfam("Pfalciparum", null).clear(); //clears any existing Rfam features for Pfalciparum. 
        /*1.7.2009: Second argument for ClearRfam (analysisProgram) has to be null or else it won't work with updated Clear class */
        logger.info("Deleted any existing Rfam features for Pfalciparum from the test database");
        new Load(loader).load("test/data/Pfalciparum-rfam.fa.o"); //loads the Rfam results in test file
        
        /*Get the chromosome object */
        chromosome = (Chromosome)session.createQuery("from Feature where uniquename='Pf3D7_01'").uniqueResult();
        assertNotNull(chromosome);
        
    }
    
    /**
     * Tests if rfam analysis has been added and, if so, the following properties of the analysis:
     * Program
     * ProgramVersion (N/A at the moment)
     * Time (Not done yet; but will eventually try to check if this time is the current time or relatively reasonable)
     * 
     */
    
    @SuppressWarnings("unchecked") //Remove after sorting out type safety issue below
    @Test
    public void testRfamAnalysis(){
     
        List<Analysis> analysisList = session.createQuery("from Analysis where program='rfam'").list();
        assertEquals(analysisList.size(),1);
        analysis = analysisList.get(0);
        assertEquals(analysis.getProgram(), "rfam");
        assertEquals(analysis.getProgramVersion(), analysisProgramVersion);
        //Check time also - how?
        
        
    }
    
    /**
     * The loading process is expected to result in the creation of four genes. Here, we test the following properties of one
     * such gene:
     * 1. Uniquename
     * 2. Organism ID
     * 3. Dbxref ID 
     * 4. Type ID
     * 5. Associated FeatureLoc - fmin, fmax, strand, rank, sourcefeature
     * 
     */
  
    @SuppressWarnings("unchecked") //TODO: remove after type safety below is sorted out
    @Test
    public void testRfamGenes() {
        
        try {
            logger.info("Doing tests on rfam genes");
            
            /* Check that four genes have been added */
            List<Gene> allRfamGenes = session.createQuery("from Feature where uniquename LIKE 'Pf3D7_01_rfam_____'").list();
            assertEquals(4, allRfamGenes.size()); 
            
            /*Do other tests on one gene*/
            gene  = (Gene)session.createQuery("from Feature where uniquename='Pf3D7_01_rfam_0001'").uniqueResult(); 
            assertNotNull(gene);
            assertEquals(gene.getUniqueName(), "Pf3D7_01_rfam_0001");
            assertEquals(gene.getOrganism().getOrganismId(), 27);
            assertNull(gene.getDbXRef());
            assertEquals(gene.getType().getCvTermId(), 792); //Perhaps this value should not be hard-coded?
            
            /*Tests on featureloc */
            List<FeatureLoc> featureLocList = gene.getFeatureLocs();
            assertEquals(featureLocList.size(), 1); //There should only be one associated feature Loc
            FeatureLoc featureLoc = featureLocList.get(0);
            assertEquals(featureLoc.getFmin().intValue(), 1882443);
            assertEquals(featureLoc.getFmax().intValue(), 1882574);
            assertEquals(featureLoc.getStrand().intValue(), -1);
            assertEquals(featureLoc.getRank(), 0);
            assertEquals(featureLoc.getSourceFeature().getFeatureId(), chromosome.getFeatureId());
              

        } finally {
            
        }
    }
    
    /**
     * The RfamLoader should also add four transcripts. This method checks that there are 4 and for one of them checks the following:
     * Uniquename
     * OrganismId
     * Type Id
     * Associated Dbxref - Dbxref ID, DB ID, Accession, Description
     * Associated FeatureLoc - fmin, fmax, strand, sourcefeature
     * Associated analysisFeature - analysis ID, score
     */
    
    
    @SuppressWarnings("unchecked") //Remove after sorting out type safety issues below
    @Test
    public void testRfamTranscripts() {
        
        try {
            logger.info("Doing tests on rfam transcripts");
          
            /* Check that four transcripts have been added */
            List<Transcript> allRfamTranscripts = session.createQuery("from Feature where uniquename LIKE 'Pf3D7_01_rfam_____:%'").list();
            assertEquals(4, allRfamTranscripts.size()); 
            
            /*Do other tests on one transcript*/
            transcript  = (Transcript)session.createQuery("from Feature where uniquename='Pf3D7_01_rfam_0001:ncRNA'").uniqueResult(); 
            assertNotNull(transcript);
            assertEquals(transcript.getUniqueName(), "Pf3D7_01_rfam_001:ncRNA");
            assertEquals(gene.getOrganism().getOrganismId(), 27);
            assertEquals(gene.getType().getCvTermId(), 743); //Perhaps this value should not be hard-coded?
            
            /* Tests on dbxref */
            DbXRef dbxref = transcript.getDbXRef();
            assertEquals(dbxref.getDb().getDbId(), 199); //Hard-coded value - consider changing
            assertEquals(dbxref.getAccession(), "RF00015");
            assertEquals(dbxref.getDescription(), "U4");
            
            
            /*Tests on featureloc */
            List<FeatureLoc> featureLocList = transcript.getFeatureLocs();
            assertEquals(featureLocList.size(), 1); //There should only be one associated feature Loc
            FeatureLoc featureLoc = featureLocList.get(0);
            assertEquals(featureLoc.getFmin().intValue(), 2004527);
            assertEquals(featureLoc.getFmax().intValue(), 2004566);
            assertEquals(featureLoc.getStrand().intValue(), -1);
            assertEquals(featureLoc.getRank(), 0);
            assertEquals(featureLoc.getSourceFeature().getFeatureId(), chromosome.getFeatureId());
            
            /*Tests on analysisfeature */
            List<AnalysisFeature> analysisFeatureList = (List)transcript.getAnalysisFeatures();
            assertEquals(analysisFeatureList.size(), 4); //There should be 4 analysisfeatures returned
            AnalysisFeature analysisFeature = analysisFeatureList.get(0);
            assertEquals(analysisFeature.getAnalysis().getAnalysisId(), analysis.getAnalysisId());
            assertEquals(analysisFeature.getRawScore().toString(), new Double(77.01).toString());
            
              

        } finally {
            
        }
    }
 
    
    
    

  
    @AfterClass 
    @SuppressWarnings("deprecation")
    public static void shutdownDatabase() throws HibernateException, SQLException {
       
       

        // When session.connection() is deprecated, change to use doWork(Work)
        // and remove the @SuppressWarnings("deprecation").
        session.connection().createStatement().execute("shutdown");
        SessionFactoryUtils.releaseSession(session, sessionFactory);
    }
}
