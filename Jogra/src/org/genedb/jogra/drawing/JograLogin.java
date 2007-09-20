package org.genedb.jogra.drawing;

import org.genedb.jogra.controller.ImageUtils;

import org.springframework.beans.factory.InitializingBean;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;


public class JograLogin extends BaseForm implements InitializingBean {
    
    public JograLogin() throws Exception {
        super();
        setResizable(false);
        setModal(true);
        setTitle("Jogra Login");
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                System.exit(0);
            }
        });
        setLayout(new BorderLayout());
        afterPropertiesSet();
    }

    public void afterPropertiesSet() throws Exception {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        
        BufferedImage bi = ImageUtils.makeBackgroundFromClasspath("jogra.jpg");
        ImagePanel imagePanel = new ImagePanel(bi);
        
        panel.setBorder(BorderFactory.createMatteBorder(10, 10, 10, 20, getBackground()));
        
        add("North", imagePanel);
        
        FormLayout layout = new FormLayout(
                "pref, 3dlu, pref", // 1st major column
                "");                                         // add rows dynamically
        JPanel loginDetails = new JPanel(layout);
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        
        JTextField instance = new JTextField("build2");
        builder.append("GeneDB Instance", instance);
        builder.nextLine();
        
        JTextField username = new JTextField(System.getProperty("user.name"));  
        builder.append("Username",   username);
        builder.nextLine();

        JPasswordField password = new JPasswordField(12);
        builder.append("Password",   password);
        //builder.nextLine();
            

        
        JButton login = new JButton("Login");
        login.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                processLogin();
                setVisible(false);
            }
        });
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                System.exit(0);
            }
        });
        
        Box form = Box.createVerticalBox();
        form.add(loginDetails);
        Box buttons = Box.createHorizontalBox();
        buttons.add(Box.createHorizontalGlue());
        buttons.add(login);
        buttons.add(Box.createHorizontalStrut(40));
        buttons.add(cancel);
        buttons.add(Box.createHorizontalGlue());
        form.add(buttons);
        
        add("South", form);
        
        pack();
        
        Dimension screen = getToolkit().getScreenSize();
        setLocation((screen.width-getWidth())/2, (screen.height-getHeight())/2);
        
    }

    protected void processLogin() {
        // TODO Auto-generated method stub
    }

}


