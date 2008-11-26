package org.genedb.db.loading;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.ProteinMatch;
import org.gmod.schema.mapped.Analysis;
import org.gmod.schema.mapped.AnalysisFeature;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.jdbc.Work;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

@Component("orthologueTester")
public class OrthologueTester {
    private static final Logger logger = Logger.getLogger(OrthologueTester.class);

    @Resource(name="sessionFactory")
    private SessionFactory sessionFactory;

    @Resource(name="sequenceDao")
    private SequenceDao sequenceDao;


    public void orthologueGroup(String program, String programVersion,
            String algorithm, Double identity,
            String... polypeptideUniqueNames) {

        if (polypeptideUniqueNames.length == 0) {
            throw new IllegalArgumentException("The array polypeptideUniqueNames is empty");
        }

        Polypeptide firstPolypeptide = null;
        Set<Polypeptide> polypeptides = new HashSet<Polypeptide>();
        for (String polypeptideUniqueName: polypeptideUniqueNames) {
            Polypeptide polypeptide = sequenceDao.getFeatureByUniqueName(polypeptideUniqueName, Polypeptide.class);
            if (polypeptide == null) {
                fail(String.format("Polypeptide '%s' not found in database", polypeptideUniqueName));
            }

            if (firstPolypeptide == null) {
                firstPolypeptide = polypeptide;
            }
            polypeptides.add(polypeptide);
        }

        Collection<ProteinMatch> clusters = firstPolypeptide.getOrthologueClusters();
        if (clusters.isEmpty()) {
            fail(String.format("Polypeptide '%s' does not belong to any cluster", firstPolypeptide.getUniqueName()));
        }
        if (clusters.size() > 1) {
            fail(String.format("Polypeptide '%s' belongs to more than one cluster", firstPolypeptide.getUniqueName()));
        }

        ProteinMatch cluster = clusters.iterator().next();
        AnalysisFeature analysisFeature = cluster.getAnalysisFeature();
        if (analysisFeature == null) {
            fail(String.format("Cluster '%s' (ID=%d) has no AnalysisFeature", cluster.getUniqueName(), cluster.getFeatureId()));
        }
        Analysis analysis = analysisFeature.getAnalysis();
        assertNotNull(String.format("Cluster '%s' (ID=%d) has no Analysis", cluster.getUniqueName(), cluster.getFeatureId()),
            analysis);
        assertEquals(program, analysis.getProgram());
        assertEquals(programVersion, analysis.getProgramVersion());
        assertEquals(algorithm, analysis.getAlgorithm());
        assertEquals(identity, analysisFeature.getIdentity());

        assertEquals(polypeptides, cluster.getPolypeptidesInCluster());
    }

    /**
     * Shut down the database.
     * This method should be called once the tests are complete.
     * It should ordinarily be called from an <code>@AfterClass</code> method
     * of the test class.
     */
    public void cleanUp() {
        /*
         * If the database is not shutdown, the changes will not be
         * persisted to the data file. It's useful to be able to inspect
         * the loaded data directly sometimes, so it's important to do
         * this. (The HSLQDB documentation suggests that comitted changes
         * will never be lost even if the database is not shut down. That
         * does not appear to be true.)
         */
        Session session = SessionFactoryUtils.getSession(sessionFactory, true);
        session.doWork(new Work() {
            public void execute(Connection connection) throws SQLException {
                logger.debug("Shutting down database");
                connection.createStatement().execute("shutdown");
            }
        });
        SessionFactoryUtils.releaseSession(session, sessionFactory);
        session = null;
    }

}
