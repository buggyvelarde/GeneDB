package org.genedb.db.loading.auxiliary;

import org.gmod.schema.utils.CvTermUtils;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.engine.SessionFactoryImplementor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * First deletes duplicate GO terms, then redundant GO terms
 * 
 * A GO annotation is a duplicate when it has the same :
 * <ul>
 *   <li>GO accession
 *   <li>evidence code (not case sensistive)
 *   <li>and PMID (in the pub table)
 * </ul>
 * 
 * A GO annotation is redundant when:
 * <ul>
 *  <li> It is inferred from electronic annotation,
 *  <li> There is also a more specific term present.
 * </ul>
 * These redundant terms add no new information, so should
 * be removed. This class removes them. It should be used
 * whenever new IEA terms have been added: for example,
 * after loading InterPro data.
 *
 * Duplicates must be removed before removing redundant terms
 * otherwise DeleteRedundantGOTermsSQL will remove them both 
 *
 * @author rh11
 */
public class DeleteRedundantGOTerms {
    private static final Logger logger = Logger.getLogger(DeleteRedundantGOTerms.class);

    public static void main(String[] args) throws SQLException, IOException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext(
            new String[] {"Load.xml"});

        DataSource dataSource = ctx.getBean("dataSource", DataSource.class);
        deleteRedundantGOTerms(dataSource);
    }

    public static void deleteRedundantGOTerms(DataSource dataSource) throws SQLException, IOException {
        deleteRedundantGOTerms(dataSource.getConnection());
    }

    public static void deleteRedundantGOTerms(Session session) throws SQLException, IOException {
        SessionFactoryImplementor sessionFactoryImplementer = (SessionFactoryImplementor) session.getSessionFactory();
        ConnectionProvider connectionProvider = sessionFactoryImplementer.getConnectionProvider();
        deleteRedundantGOTerms(connectionProvider.getConnection());       
    }

    public static void deleteRedundantGOTerms(Connection conn) throws SQLException, IOException {
        new DeleteRedundantGOTerms(conn)
            .deleteRedundantGOTerms()
            .closeConnection();
    }

    private Connection conn;
    public DeleteRedundantGOTerms (Connection conn) throws SQLException {
        this.conn = conn;
        conn.setAutoCommit(false);
    }

    private DeleteRedundantGOTerms deleteRedundantGOTerms() throws SQLException, IOException {
        CvTermUtils.checkCvTermPath(conn);
        
        PreparedStatement st = conn.prepareStatement(getDeleteDuplicateGOTermsSQL());
        try {
            int numDeleted = st.executeUpdate();
            logger.info(String.format("Deleted %d duplicate GO annotations", numDeleted));
        }
        finally {
            try {st.close();} catch (SQLException e) {logger.error(e);}
        }
        
        PreparedStatement st2 = conn.prepareStatement(getDeleteRedundantGOTermsSQL());
        try {
            int numDeleted = st2.executeUpdate();
            logger.info(String.format("Deleted %d redundant GO annotations", numDeleted));
        }
        finally {
            try {st2.close();} catch (SQLException e) {logger.error(e);}
        }

        return this; // for method chaining
    }

    /**
     * Size of buffer into which the SQL file is read.
     * Must be at least as large as the file (measured in characters).
     */
    private static final int BUF_SIZE = 32768;
    
    private String getDeleteDuplicateGOTermsSQL() throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("/delete_duplicate_GO_terms.sql");

        if (inputStream == null)
            throw new RuntimeException("Could not find 'delete_duplicate_GO_terms.sql' on classpath");

        Reader reader = new InputStreamReader(inputStream);
        CharBuffer sqlBuffer = CharBuffer.allocate(BUF_SIZE);
        int numCharsRead = reader.read(sqlBuffer);
        logger.debug(String.format("Read %d chars from delete_duplicate_GO_terms.sql", numCharsRead));
        reader.close();
        inputStream.close();

        sqlBuffer.position(0);
        return sqlBuffer.subSequence(0, numCharsRead).toString();
    }

    
    private String getDeleteRedundantGOTermsSQL() throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("/delete_redundant_GO_terms.sql");

        if (inputStream == null)
            throw new RuntimeException("Could not find 'delete_redundant_GO_terms.sql' on classpath");

        Reader reader = new InputStreamReader(inputStream);
        CharBuffer sqlBuffer = CharBuffer.allocate(BUF_SIZE);
        int numCharsRead = reader.read(sqlBuffer);
        logger.debug(String.format("Read %d chars from delete_redundant_GO_terms.sql", numCharsRead));
        reader.close();
        inputStream.close();

        sqlBuffer.position(0);
        return sqlBuffer.subSequence(0, numCharsRead).toString();
    }
    
    private void closeConnection() throws SQLException {
        conn.commit();
        conn.close();
    }
}
