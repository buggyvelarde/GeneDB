package org.genedb.db.loading.auxiliary;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Abstract Clear class implemented by Clear classes to delete features or other elements from the database
 *
 *
 */


public abstract class Clear {
    //Fixed
    private static final Logger logger = Logger.getLogger(Clear.class);
    private static final ResourceBundle config = ResourceBundle.getBundle("project");

    private static String getConfigParam(String key) {
        try {
            return config.getString(key);
        } catch (MissingResourceException e) {
            return System.getProperty(key);
        }
    }

    //Configurable variables
    private Connection conn;
    private String organismCommonName;
    protected String analysisProgram;

    /**
     * Main method that deals with arguments sent to clear objects
     * @param <T>
     * @param clazz
     * @param args
     * @throws Exception
     */
    protected static <T extends Clear> void main(Class<T> clazz, String[] args) throws Exception {
        if (args.length != 1 && args.length != 2) {
            System.err.printf("Usage: java %s <organism common name> [<analysis program>]\n", clazz.getName());
            System.exit(1);
        }
        String organismCommonName = args[0];
        String analysisProgram = null;
        if (args.length > 1) {
            analysisProgram = args[1];
        }
        T instance = clazz.getDeclaredConstructor(String.class, String.class).newInstance(organismCommonName, analysisProgram);
        instance.clear();
    }

    /**
     * Use this constructor when it is ok to get the database details from the project.properties file
     *
     * @param organismCommonName
     * @param analysisProgram
     * @throws ClassNotFoundException
     * @throws SQLException
     */

    protected Clear(String organismCommonName, String analysisProgram) throws ClassNotFoundException, SQLException {
        if (organismCommonName.length() == 0)
            throw new IllegalArgumentException("Empty organism name");
        String url = String.format("jdbc:postgresql://%s:%s/%s",
            getConfigParam("dbhost"),
            getConfigParam("dbport"),
            getConfigParam("dbname"));
        String username = getConfigParam("dbuser");
        String password = getConfigParam("dbpassword");

        Class.forName("org.postgresql.Driver");
        logger.debug(String.format("Connecting to database '%s' as user '%s'", url, username));

        this.conn = DriverManager.getConnection(url, username, password);
        this.organismCommonName = organismCommonName;
        this.analysisProgram = null;
        if (analysisProgram != null) {
            this.analysisProgram = analysisProgram;
        }
    }

    /**
     * Use this constructor when you want to specify the connection (e.g. when trying to use clear via the loaders to run tests on the pfalciparum)
     * See some of the loader classes for examples
     *
     * @param conn
     * @param organismCommonName
     * @param analysisProgram
     */
    protected Clear(Connection conn, String organismCommonName, String analysisProgram) {
        this.conn = conn;
        this.organismCommonName = organismCommonName;
        this.analysisProgram = analysisProgram;
    }

    /**
     * Executes all the delete sql commands
     *
     * @throws SQLException
     */
    protected void clear() throws SQLException {

        for (DeleteSpec deleteSpec: getDeleteSpecs())
            deleteSpec.execute();
    }

    /**
     * Needs to be implemented by classes implementing this abstract class
     *
     * @return an array of delete statements
     */

    protected abstract DeleteSpec[] getDeleteSpecs();

    /**
     * Class representing the sql delete statement and any sort of description provided
     *
     */

    protected class DeleteSpec {
        private String description, sql;
        private int numberBindParams;

        public DeleteSpec(String description, String sql) {
            this.description = description;
            this.sql = sql;
            this.numberBindParams = 1;
         }

        public DeleteSpec(String description, String sql, int numberBindParams) {
            this.description = description;
            this.sql = sql;
            this.numberBindParams = numberBindParams;
         }

        private void execute() throws SQLException {
             PreparedStatement st = conn.prepareStatement(sql);
            try {
                if (numberBindParams == 1) {
                    st.setString(1, organismCommonName);
                }
                else if (numberBindParams == 2) {
                    st.setString(1, analysisProgram);
                    st.setString(2, organismCommonName);
                }
                else {
                    logger.error(String.format("Incorrect number of bind parameters: %d", numberBindParams));
                }
                int numberDeleted = st.executeUpdate();
                logger.info(String.format("Deleted %d %s for %s %s using %s", numberDeleted, description, analysisProgram, organismCommonName, sql));
            }
            finally {
                try { st.close(); } catch (SQLException e) { logger.error(e); }
            }

        }
    }
}