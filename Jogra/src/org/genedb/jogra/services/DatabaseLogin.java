/*
 * Copyright (c) 2009 Genome Research Limited.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program; see the file COPYING.LIB. If not, write to the Free
 * Software Foundation Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307
 * USA
 */

package org.genedb.jogra.services;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;



/* Helper service for Jogra (and other applications) to get a username & password, and validate them for a given database. It does so by trying to create a
 * JDBC db connection and catching any errors. Sample usage:

        DatabaseLogin dblogin = new DatabaseLogin();

        try {
            dblogin.validateUser();
        } catch (SQLException exp) {
            exp.printStackTrace();
            System.exit(65);
        } catch (AbortException exp) {
            System.exit(65);
        }

        Jogra application = Jogra.instantiate(dblogin.getUsername(), dblogin.getPassword());
 
 *
 * NDS + ART
 * 2009
 */

public class DatabaseLogin {

    private static final Logger logger = Logger.getLogger(DatabaseLogin.class);

    private JTextField hostname = new JTextField(20);
    private JTextField port = new JTextField(20);
    private JTextField dbname = new JTextField(20);
    private JTextField username = new JTextField(20);
    private JPasswordField password = new JPasswordField(20);

    
    public void validateUser() throws SQLException, AbortException {
        
        //Set default values (for internal Sanger users)
        hostname.setText("pgsrv1.internal.sanger.ac.uk");
        port.setText("5432");
        dbname.setText("pathogens");
        username.setText(System.getenv("USER")+"@sanger.ac.uk");

        Object[] array = {"Host", hostname, "Port", port, "Database", dbname, "Username", username, "Password", password };

        while (true) {
            
            password.setText("");
            
            int value = JOptionPane.showOptionDialog(null, array, "Database login",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                        null, null);

            logger.debug(String.format("Entered host '%s', port '%s', dbname '%s', username '%s'", 
                                        hostname.getText(), port.getText(), dbname.getText(), username.getText()));

            if (value != JOptionPane.OK_OPTION) {
                throw new AbortException();
                
            } else {
                if (   !StringUtils.hasText(username.getText())
                    || !StringUtils.hasText(new String(password.getPassword()))
                    || !StringUtils.hasText(hostname.getText())
                    || !StringUtils.hasText(port.getText())
                    || !StringUtils.hasText(dbname.getText())){

                    JOptionPane.showMessageDialog(null,
                            "Sorry, the fields cannot be left empty.",
                            "Try again", JOptionPane.ERROR_MESSAGE);
                } else {
                    if (checkLogin(hostname.getText(), port.getText(), dbname.getText(), username.getText(), new String(password.getPassword())  )) {
                        return;
                    }
                    JOptionPane.showMessageDialog(
                            null,
                            "Sorry, a connection could not be established. Check the details and try again.",
                            "Try again", JOptionPane.ERROR_MESSAGE);
     
                }
            }
        }
    }

    
    /*
     * Method to attempt to create a database connection using the parameters
     * given. Connection closed after test as it will later be created via 
     * Spring.
     */
    private boolean checkLogin(String host, String port, String dbname, String username, String password) throws SQLException {
        try {
            
            String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;
            Connection c = DriverManager.getConnection(url, username, password);
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


    /* Getter methods */
    public String getUsername() {
        return username.getText();
    }

    public char[] getPassword() {
        return password.getPassword();
    }
    
    public String getDBUrl(){
        return "jdbc:postgresql://" + hostname.getText() + ":" + port.getText() + "/" + dbname.getText();
    }



    public class AbortException extends Exception {
        // Deliberately empty - custom checked exception
    }

}
