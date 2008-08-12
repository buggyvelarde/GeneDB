package org.genedb.db.loading.auxiliary;


import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ClearOPIReferences {
    private static final Logger logger = Logger.getLogger(OPIReferenceLoader.class);
    private static final ResourceBundle config = ResourceBundle.getBundle("project");

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        new ClearOPIReferences().clear();
    }

    private Connection conn;

    public ClearOPIReferences() throws ClassNotFoundException, SQLException {
        String url = String.format("jdbc:postgresql://%s:%s/%s",
            config.getString("dbhost"),
            config.getString("dbport"),
            config.getString("dbname"));
        String username = config.getString("dbuser");
        String password = config.getString("dbpassword");

        Class.forName("org.postgresql.Driver");
        logger.info(String.format("Connecting to database '%s' as user '%s'", url, username));
        this.conn = DriverManager.getConnection(url, username, password);
    }

    private void clear() throws SQLException {
        PreparedStatement st = conn.prepareStatement(
            "delete from dbxref"
            +" using db"
            +" where dbxref.db_id = db.db_id"
            +" and db.name = 'OPI'"
        );
        try {
            int numDeleted = st.executeUpdate();
            logger.info(String.format("Deleted %d OPI dbxref entries", numDeleted));
        }
        finally {
            try {st.close();} catch (SQLException e) { logger.error(e); }
        }
    }

}
