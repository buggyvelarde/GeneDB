package org.genedb.jogra.drawing;

import org.springframework.beans.factory.InitializingBean;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class PrimerSelectionForm extends JDialog implements InitializingBean {

    private GridBagConstraints gbc = new GridBagConstraints();
    private Jogra application;
    private boolean primer2ok = true;
    private final JButton login = new JButton("Enter Primers");
    private boolean primer1ok = true;
    
    public void setApplication(Jogra application) {
        this.application = application;
    }

    private void addField(JPanel panel, String text, JComponent jc, Dimension preferred) {
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridy++;
        JLabel l = new JLabel(text);
        panel.add(l, gbc);
        if (preferred != null) {
            jc.setPreferredSize(preferred);
        }
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(jc, gbc);
    }
    
    public PrimerSelectionForm() {
        super();
        setResizable(false);
        setModal(true);
        setTitle("Phugushop");
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                System.exit(0);
            }
        });
    }

    public void afterPropertiesSet() throws Exception {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        
        JTextField primer1 = new JTextField("Enter primer 1 here");
        addField(panel, "Primer 1 ", primer1, null);
        
        JTextField primer2 = new JTextField("Enter primer 2 here");
        addField(panel, "Primer 2 ", primer2, null);

//        Dimension preferred = new Dimension(120, jpf.getPreferredSize().height);

        panel.setBorder(BorderFactory.createMatteBorder(10, 10, 10, 20, getBackground()));
        
      add("North", panel);
        
        Box buttons = Box.createHorizontalBox();
        
//        JButton cancel = new JButton("Cancel");
//        cancel.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent arg0) {
//                System.exit(0);
//            }
//        });
//        buttons.add(Box.createHorizontalStrut(35));
//        buttons.add(cancel);
//        buttons.add(Box.createHorizontalStrut(20));

        login.setDefaultCapable(true);
        checkOkStatus();
        login.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
                //application.createPointHolder(user, today, element, stage, Integer.parseInt(embNumField.getText()));
                dispose();
            }
        });
        buttons.add(login);
        
        
        add("South", buttons);
        
        pack();
        
        Dimension screen = getToolkit().getScreenSize();
        int x = getWidth();
        int y = getHeight();
        setLocation((screen.width-x)/2, (screen.height-y)/2);
        
    }

    protected void checkOkStatus() {
        if (primer1ok && primer2ok) {
            login.setEnabled(true);
        } else {
            login.setEnabled(false);
        }
    }
    
}

