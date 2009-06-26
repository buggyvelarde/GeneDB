package org.genedb.db.loading.auxiliary;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public abstract class Clear {
    private static final Logger logger = Logger.getLogger(Clear.class);
    private static final ResourceBundle config = ResourceBundle.getBundle("project");

    private Connection conn;
    private String organismCommonName;
    private String analysisProgram;
    
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
   
    protected Clear(String organismCommonName, String analysisProgram) throws ClassNotFoundException, SQLException {
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
        this.analysisProgram = null;
        if (analysisProgram != null) {
        	this.analysisProgram = analysisProgram;
        }
    }
    
    protected Clear(Connection conn, String organismCommonName, String analysisProgram) {
        this.conn = conn;
        this.organismCommonName = organismCommonName;
    	this.analysisProgram = analysisProgram;
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
            	if (analysisProgram == null) {
            		st.setString(1, organismCommonName);
            	}
            	else {
            		st.setString(1, analysisProgram);
            		st.setString(2, organismCommonName);
            	}
                int numberDeleted = st.executeUpdate();
                logger.info(String.format("Deleted %d %s", numberDeleted, description));
            }
            finally {
                try { st.close(); } catch (SQLException e) { logger.error(e); }
            }
        }
    }
}