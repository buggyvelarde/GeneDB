package org.genedb.db.loading.aux;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.genedb.db.loading.PropertyOverrideHolder;
import org.hibernate.Session;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.engine.SessionFactoryImplementor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
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
 * @author rh11
 */
public class DeleteRedundantGOTerms {
    private static final Logger logger = Logger.getLogger(DeleteRedundantGOTerms.class);

    public static void main(String[] args) throws SQLException, IOException {
        PropertyOverrideHolder.setProperties("dataSourceMunging", new Properties());
        ApplicationContext ctx = new ClassPathXmlApplicationContext(
            new String[] {"AuxContext.xml"});

        DataSource dataSource = (DataSource) ctx.getBean("dataSource", DataSource.class);
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

    private void checkCvTermPath() throws SQLException {
        PreparedStatement st = conn.prepareStatement(
            " select cv.name, count(cvtermpath.*)"
            +" from cv left join cvtermpath using (cv_id)"
            +" where cv.name in ("
            +"       'biological_process'"
            +"     , 'molecular_function'"
            +"     , 'cellular_component'"
            +" )"
            +" group by cv.name"
        );
        try {
            boolean addedToCvTermPath = false;
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String cvName = rs.getString(1);
                int count = rs.getInt(2);
                logger.debug(String.format("There are %d cvtermpath entries for '%s'", count, cvName));

                if (count == 0) {
                    populateCvTermPath(conn, cvName);
                    addedToCvTermPath = true;
                }
            }
            if (addedToCvTermPath)
                analyzeCvTermPath(conn);
        }
        finally {
            try {st.close(); conn.commit();} catch (SQLException e) {logger.error(e);}
        }
    }

    private void populateCvTermPath(Connection conn, String cvName) throws SQLException {
        CallableStatement st = conn.prepareCall("{call fill_cvtermpath(?)}");
        try {
            st.setString(1, cvName);
            logger.info(String.format("Populating cvtermpath for cv '%s'", cvName));
            st.execute();
        }
        finally {
            try {st.close();} catch (SQLException e) {logger.error(e);}
        }
    }

    private void analyzeCvTermPath(Connection conn) throws SQLException {
        logger.info("Analyzing cvtermpath table");
        Statement st = conn.createStatement();
        try {
            st.execute("analyze cvtermpath");
        }
        finally {
            try { st.close(); } catch (SQLException e) {logger.error(e);}
        }
    }

    private DeleteRedundantGOTerms deleteRedundantGOTerms() throws SQLException, IOException {
        checkCvTermPath();

        PreparedStatement st = conn.prepareStatement(getDeleteRedundantGOTermsSQL());
        try {
            int numDeleted = st.executeUpdate();
            logger.info(String.format("Deleted %d redundant GO annotations", numDeleted));
        }
        finally {
            try {st.close();} catch (SQLException e) {logger.error(e);}
        }

        return this; // for method chaining
    }

    /**
     * Size of buffer into which the SQL file is read.
     * Must be at least as large as the file (measured in characters).
     */
    private static final int BUF_SIZE = 32768;

    private String getDeleteRedundantGOTermsSQL() throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("delete_redundant_GO_terms.sql");

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
