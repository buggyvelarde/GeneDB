package org.genedb.db.loading.auxiliary;

import static org.junit.Assert.*;

import org.gmod.schema.feature.HelixTurnHelix;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.mapped.Analysis;
import org.gmod.schema.mapped.AnalysisFeature;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.FeatureProp;
import org.gmod.schema.mapped.Organism;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.jdbc.Work;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/****************************************************************************************************************************************
 * Tests the hthloader class. First hit in the file is expected to be the following. 
   
    Feature: 1
    Name: PFA0610c..pep
    Start: 151
    End: 172
    Length: 22
    Score: 988.000
    Strand: +
    Maximum_score_at: 151
    Standard_deviations: 2.55
    
 * If this changes, change the constants below.
  
 * SAMPLE USAGE: ant hth-test -Dconfig=bigtest5
 * 
 * The config argument is not really used by the test but the build.xml file complains if it is not included.
 * TODO: Re-implement this class using FeatureTester
 *  
 * @author nds
 ****************************************************************************************************************************************/

public class HTHLoaderTest {
    
    private static final Logger logger = Logger.getLogger(HTHLoaderTest.class);
    
    //Constants. These should be changed if testing results for a different organism and/or if the first expected hit is different 
    private static final String FILE_NAME = "HTHPfalciparum.hth";
    private static final int EXPECTED_NUMBER_OF_HITS = 33;
    private static final String ORGANISM_COMMON_NAME = "Pfalciparum";
    private static final String VERSION = "unknown";
    private final int HTH_TYPE_ID = 1168; /*cvterm id */
    private final String POLYPEPTIDE_NAME = "PFA0610c:pep"; 
    private final int START = 151;
    private final int END = 172;
    private final String SCORE = "988.000";
    private final String MAX_SCORE_AT = "151";
    private final String STD_DEVIATIONS = "2.55";
    
    //Configured during test
    private static int organism_id;
    private static HTHLoader loader;
    private static Analysis analysis;
    private static SessionFactory sessionFactory; 
    private static Session session;
   

    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void setup() throws IOException, HibernateException, SQLException, ClassNotFoundException {
        
        /* Here we set the application context and extract the hthloader bean. Then we delete any existing hth features from the test database 
         * for this organism and load the test results. */
        
        ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] {"Load.xml", "AuxTest.xml"});
        loader = ctx.getBean("hthloader", HTHLoader.class);
        assertTrue(loader.processOptionIfValid("hth-version", VERSION)); 
        sessionFactory = loader.getSessionFactory();
        session = SessionFactoryUtils.getSession(sessionFactory, true);
        loader.clear("Pfalciparum", null); 
        new Load(loader).load("test/data/" + FILE_NAME); 

        /*Get the organism */
        Organism organism = (Organism)session.createQuery("from Organism where common_name='" + ORGANISM_COMMON_NAME + "'").uniqueResult();
        assertNotNull(organism); //Sanity check
        organism_id = organism.getOrganismId();
        
        
        /*Get the analysis object */
        List<Analysis> analysisList = session.createQuery("from Analysis where program='helixturnhelix'").list();
        assertEquals(analysisList.size(),1);
        analysis = analysisList.get(0);
        assertEquals(analysis.getProgram(), "helixturnhelix");
        assertEquals(analysis.getProgramVersion(), VERSION);
        //Check the time of the analysis also - how?
        
    }
    
   
    
    /**
     * The loading process is expected to create a helixturnfeature for each valid hit in the results file. Here, we test if it has added the correct
     * number of features and the following properties of one
     * such gene:
     * 1. Uniquename
     * 2. Organism ID
     * 3. Dbxref ID 
     * 4. Type ID
     * 5. Associated FeatureLoc - fmin, fmax, strand, rank, sourcefeature
     */
  
    @SuppressWarnings("unchecked") //TODO: remove after type safety below is sorted out
    @Test
    public void testHTHFeatures() {
      
        logger.info("Doing tests on helix-turn-helix features");
        
        /* Check that the right number of features have been added */
        List<HelixTurnHelix> allHTHFeatures = session.createQuery("from Feature where type_id=" + HTH_TYPE_ID + " and organism_id=" + organism_id).list();
        assertEquals(EXPECTED_NUMBER_OF_HITS, allHTHFeatures.size()); 
        
        /*Do other tests on the first feature */
        HelixTurnHelix helixTurnHelix  = allHTHFeatures.get(0);
        assertNotNull(helixTurnHelix);
        assertEquals(helixTurnHelix.getUniqueName(), String.format("%s:%d-%d", POLYPEPTIDE_NAME, START, END));
        assertEquals(helixTurnHelix.getOrganism().getOrganismId(), organism_id);
        assertEquals(helixTurnHelix.getType().getCvTermId(), HTH_TYPE_ID); //repeat test but do anyway
        
        /* Tests on dbxref - should a HTH feature have a dbxref? At the moment it does not.
     
        
        /*Tests on featureloc */
        List<FeatureLoc> featureLocList = helixTurnHelix.getFeatureLocs();
        assertEquals(featureLocList.size(), 1); //There should only be one associated feature Loc
        FeatureLoc featureLoc = featureLocList.get(0);
        assertEquals(featureLoc.getFmin().intValue(), START);
        assertEquals(featureLoc.getFmax().intValue(), END);
        assertEquals(featureLoc.getRank(), 0);
        assertEquals(featureLoc.getStrand().toString(), "0");
        Polypeptide polypeptide = (Polypeptide) session.createQuery("from Feature where uniquename='" + POLYPEPTIDE_NAME + "'").uniqueResult();
        assertNotNull(polypeptide);     
        assertEquals(featureLoc.getSourceFeature().getFeatureId(), polypeptide.getFeatureId());
     
        
        /*Tests on analysisfeature */
        Collection<AnalysisFeature> analysisFeatureList = helixTurnHelix.getAnalysisFeatures();
        assertEquals(analysisFeatureList.size(), 1); //There should only be one associated analysisFeature
        AnalysisFeature analysisFeature = analysisFeatureList.iterator().next();
        assertEquals(analysisFeature.getAnalysis().getAnalysisId(), analysis.getAnalysisId());
        assertEquals(analysisFeature.getRawScore().toString(), new Double(SCORE).toString());   
        
        /* Tests on featureprops */
        Collection<FeatureProp> featurePropList = helixTurnHelix.getFeatureProps();
        assertEquals(featurePropList.size(), 2); //Each hth has two feature props
        for (FeatureProp fp: featurePropList){
            if(fp.getType().getName().equals("Maximum_score_at")){
                assertEquals(fp.getValue(), MAX_SCORE_AT);
            }else if(fp.getType().getName().equals("Standard_deviations")){
                assertEquals(fp.getValue(), STD_DEVIATIONS);
            }
        }
        
             
    }
    
    
  
    @AfterClass 
    public static void shutdownDatabase() throws HibernateException, SQLException {
        /* Close database and release session */
        session.doWork(new Work() {
            public void execute(Connection connection) throws SQLException {
                connection.createStatement().execute("shutdown");
            }
        }); 
        SessionFactoryUtils.releaseSession(session, sessionFactory);
    }
}
