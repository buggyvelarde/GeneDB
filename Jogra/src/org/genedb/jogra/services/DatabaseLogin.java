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

package org.genedb.jogra.services;

import org.apache.log4j.Logger;
import java.sql.Connection;
import java.sql.DriverManager;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;


public class DatabaseLogin{
    private JTextField textfield1;
    private JPasswordField textfield2;
    private String dbname;
    private String username;
    private String password;
    private static final Logger logger = Logger.getLogger(DatabaseLogin.class);
    
    public DatabaseLogin(String dbname){ //URL of database e.g. "jdbc:postgresql://pgsrv2.internal.sanger.ac.uk:5432/pathdev"
        this.dbname = dbname;
    }
    
    public boolean validateUser(){
        
        textfield1 = new JTextField(10);
        textfield2 = new JPasswordField(10);
        Object[] array = {"Username", textfield1, "Password", textfield2};
        int value = JOptionPane.showOptionDialog(null, array,"Database login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
        username = textfield1.getText();
        password = new String(textfield2.getPassword());
        System.out.println("Entered username: " + username + " password: " + password);
        if(value == JOptionPane.OK_OPTION){
            if (username==null || username.equals("") || password==null || password.equals("")){
                JOptionPane.showMessageDialog(null, "Sorry, username and password cannot be empty.", "Try again", JOptionPane.ERROR_MESSAGE);
                username = null; //Re-set values
                password = null;
               
                //JOptionPane.showOptionDialog(null, array,"Database login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
              
            }else{
                boolean valid = check(username,password);
                if(!valid){
                    JOptionPane.showMessageDialog(null, "Sorry, that username and password does not exist.", "Try again", JOptionPane.ERROR_MESSAGE);
                    username = null;
                    password = null;
                  
                    //JOptionPane.showOptionDialog(null, array,"Database login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
                    
                }else{
                   
                    return true;
                }
            }
        }
        return false;
     
    }
    
    /* Method to attempt to create a database connection using the parameters given. Connection closed after test. */
    
    private boolean check(String username, String password){
        try{        
            Class.forName("org.postgresql.Driver");
            Connection c = DriverManager.getConnection(dbname,username, password);
            c.close();
            return true;
        }catch (Exception e){
            System.out.println(e);
            e.printStackTrace();
            return false;
        }
    }
    
    /* Getter methods */

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }


}

    

