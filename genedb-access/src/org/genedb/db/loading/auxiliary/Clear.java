package org.genedb.db.loading.auxiliary;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public abstract class Clear {
    private static final Logger logger = Logger.getLogger(Clear.class);
    private static final ResourceBundle config = ResourceBundle.getBundle("project");

    private Connection conn;
    private String organismCommonName;

    protected static <T extends Clear> void main(Class<T> clazz, String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java ClearInterPro <organism common name>");
            System.exit(1);
        }
        String organismCommonName = args[0];
        T instance = clazz.getDeclaredConstructor(String.class).newInstance(organismCommonName);
        instance.clear();
    }

    protected Clear(String organismCommonName) throws ClassNotFoundException, SQLException {
        if (organismCommonName.length() == 0)
            throw new IllegalArgumentException("Empty organism name");
        String url = String.format("jdbc:postgresql://%s:%s/%s",
            config.getString("dbhost"),
            config.getString("dbport"),
            config.getString("dbname"));
        String username = config.getString("dbuser");
        String password = config.getString("dbpassword");

        Class.forName("org.postgresql.Driver");
        logger.debug(String.format("Connecting to database '%s' as user '%s'", url, username));

        this.conn = DriverManager.getConnection(url, username, password);
        this.organismCommonName = organismCommonName;
    }

    protected Clear(Connection conn, String organismCommonName) {
        this.conn = conn;
        this.organismCommonName = organismCommonName;
    }


    protected void clear() throws SQLException {
        for (DeleteSpec deleteSpec: getDeleteSpecs())
            deleteSpec.execute();
    }

    protected abstract DeleteSpec[] getDeleteSpecs();

    protected class DeleteSpec {
        private String description, sql;

        public DeleteSpec(String description, String sql) {
            this.description = description;
            this.sql = sql;
        }

        private void execute() throws SQLException {
            PreparedStatement st = conn.prepareStatement(sql);
            try {
                st.setString(1, organismCommonName);
                int numberDeleted = st.executeUpdate();
                logger.info(String.format("Deleted %d %s", numberDeleted, description));
            }
            finally {
                try { st.close(); } catch (SQLException e) { logger.error(e); }
            }
        }
    }
}