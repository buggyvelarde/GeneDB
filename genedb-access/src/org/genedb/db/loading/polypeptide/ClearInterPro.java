package org.genedb.db.loading.polypeptide;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public class ClearInterPro {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java ClearInterPro <organism common name>");
            System.exit(1);
        }
        String organismCommonName = args[0];
        new ClearInterPro(organismCommonName).clear();
    }


    private static final Logger logger = Logger.getLogger(ClearInterPro.class);
    private static final ResourceBundle config = ResourceBundle.getBundle("project");

    private Connection conn;
    private String organismCommonName;
    private ClearInterPro(String organismCommonName) throws ClassNotFoundException, SQLException {
        String url = String.format("jdbc:postgresql://%s:%s/%s",
            config.getString("dbhost"),
            config.getString("dbport"),
            config.getString("dbname"));
        String username = config.getString("dbuser");
        String password = config.getString("dbpassword");

        Class.forName("org.postgresql.Driver");
        this.conn = DriverManager.getConnection(url, username, password);
        this.organismCommonName = organismCommonName;
    }

    private static final String DELETE_DOMAINS_SQL
    = "delete from feature"
        +" using cvterm feature_type"
        +" join cv feature_type_cv using (cv_id)"
        +"    , organism"
        +" where feature_type.cvterm_id = feature.type_id"
        +" and feature_type.name = 'polypeptide_domain'"
        +" and feature_type_cv.name = 'sequence'"
        +" and feature.organism_id = organism.organism_id"
        +" and organism.common_name = ?";

    private void deleteDomains() throws SQLException {
        PreparedStatement deleteDomains = conn.prepareStatement(DELETE_DOMAINS_SQL);
        try {
            deleteDomains.setString(1, organismCommonName);
            int rowsDeleted = deleteDomains.executeUpdate();
            logger.info(String.format("Deleted %d polypeptide domains", rowsDeleted));
        }
        finally {
            try {deleteDomains.close();} catch (SQLException e) {logger.warn(e);}
        }
    }

    private static final String DELETE_INTERPRO_GO_TERMS_SQL
        = "delete from feature_cvterm"
            +" using feature_cvtermprop"
            +"     join cvterm prop_type on feature_cvtermprop.type_id = prop_type.cvterm_id"
            +" , feature join organism using (organism_id)"
            +" where feature_cvterm.feature_cvterm_id = feature_cvtermprop.feature_cvterm_id"
            +" and feature_cvterm.feature_id = feature.feature_id"
            +" and prop_type.name = 'autocomment'"
            +" and feature_cvtermprop.value = 'From Interpro file'"
            +" and organism.common_name = ?";

    private void deleteInterProGoTerms() throws SQLException {
        PreparedStatement deleteDomains = conn.prepareStatement(DELETE_INTERPRO_GO_TERMS_SQL);
        try {
            deleteDomains.setString(1, organismCommonName);
            int rowsDeleted = deleteDomains.executeUpdate();
            logger.info(String.format("Deleted %d InterPro GO terms", rowsDeleted));
        }
        finally {
            try {deleteDomains.close();} catch (SQLException e) {logger.warn(e);}
        }
    }

    private void clear() throws SQLException {
        deleteDomains();
        deleteInterProGoTerms();
    }
}
