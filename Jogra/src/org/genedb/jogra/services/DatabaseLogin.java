package org.genedb.jogra.services;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedHashMap;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.google.common.collect.Maps;


/* Helper service for Jogra (and other applications) to get a username & password, and validate them for a given database. It does so by trying to create a
 * JDBC db connection and catching any errors. Sample usage:

        DatabaseLogin dblogin = new DatabaseLogin("jdbc:postgresql://localhost:5432/dbname);
        boolean validUser = dblogin.validateUser();
        if(validUser){
            String username = dblogin.getUsername();
            String password = dblogin.getPassword();
        }
 *
 *
 *
 * NDS
 * 12-May-2009
 */
public class DatabaseLogin {

    private static final Logger logger = Logger.getLogger(DatabaseLogin.class);

    private JTextField userField;
    private JPasswordField passwordField;
    private LinkedHashMap<String, String> instances = Maps.newLinkedHashMap();


    public void validateUser() throws SQLException, AbortException {

        userField = new JTextField(10);
        String defaultUsername = System.getenv("USER")+"@sanger.ac.uk";
        userField.setText(defaultUsername);
        passwordField = new JPasswordField(10);
        Object[] array = { "Username", userField, "Password", passwordField };

        while (true) {
            int value = JOptionPane.showOptionDialog(null, array, "Database login",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                    null, null);

            logger.debug(String.format("Entered username '%s', password '%s'", userField.getText(), passwordField.getPassword()));

            if (value != JOptionPane.OK_OPTION) {
                throw new AbortException();
            } else {
                if (!StringUtils.hasText(userField.getText())
                        || !StringUtils.hasText(new String(passwordField.getPassword()))) {

                    JOptionPane.showMessageDialog(null,
                            "Sorry, username and password cannot be empty.",
                            "Try again", JOptionPane.ERROR_MESSAGE);
                } else {
                    if (checkLogin()) {
                        return;
                    }
                    JOptionPane.showMessageDialog(
                            null,
                            "Sorry, that username and password does not exist.",
                            "Try again", JOptionPane.ERROR_MESSAGE);
                    passwordField.setText("");
                }
            }
        }
    }

    /*
     * Method to attempt to create a database connection using the parameters
     * given. Connection closed after test.
     */
    private boolean checkLogin() throws SQLException {
        try {
            String dbName = instances.get("pathogens");
            Connection c = DriverManager.getConnection(dbName, userField.getText(),
                    new String(passwordField.getPassword()));
            c.close();
            return true;
        } catch (SQLException exp) {
            if ("08001".equals(exp.getSQLState())) { // eg missing db jar
                throw exp;
            }
            logger.error("Login test failed", exp);
            return false;
        }
    }

    public void addInstance(String name, String jdbcConnection) {
        instances.put(name, jdbcConnection);
    }


    /* Getter methods */
    public String getUsername() {
        return userField.getText();
    }

    public char[] getPassword() {
        return passwordField.getPassword();
    }


    public class AbortException extends Exception {
        // Deliberately empty - custom checked exception
    }

}
