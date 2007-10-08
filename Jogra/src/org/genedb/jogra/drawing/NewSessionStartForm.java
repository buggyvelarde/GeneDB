package org.genedb.jogra.drawing;

import org.springframework.beans.factory.InitializingBean;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;


public class NewSessionStartForm extends BaseForm implements InitializingBean {
    
    private Jogra application;
    boolean audio = true;
    private boolean embryoOK = true;
    private final JButton login = new JButton("Login");
    private boolean elementOK = true; // FIXME - Make false once stopped testing and field is blank
    
    
    public NewSessionStartForm() {
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
        
        Dimension preferred = null;
        
        ButtonGroup experimentType = new ButtonGroup();
        Box experimentTypeBox = Box.createVerticalBox();
        JRadioButton jb = new JRadioButton("Single PCR");
        jb.setActionCommand("Single PCR");
//        jb.addActionListener(selectionChange);
//        if (model == null) {
//            model = jb.getModel();
//            expressionZone = zone;
//        }
        experimentType.add(jb);
        jb.setSelected(true);
        experimentTypeBox.add(jb);
        jb = new JRadioButton("Multi-injection");
        jb.setActionCommand("Multi-injection");
//        jb.addActionListener(selectionChange);
//        if (model == null) {
//            model = jb.getModel();
//            expressionZone = zone;
//        }
        experimentType.add(jb);
        experimentTypeBox.add(jb);
        addField(panel, "Exp. Type ", experimentTypeBox, 
                new Dimension(160, experimentTypeBox.getPreferredSize().height));
        
        final int count;
        Integer[] counts = new Integer[]{1,2,3,4,5};
        final JComboBox cneCount = new JComboBox(counts);
        cneCount.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                //count = ((Integer)cneCount.getSelectedItem()).intValue();
            }
        });
        addField(panel, "Num. of CNE ", cneCount, preferred);
        
        panel.setBorder(BorderFactory.createMatteBorder(10, 10, 10, 20, getBackground()));
        
        add("North", panel);
        
        Box buttons = Box.createHorizontalBox();
        
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                System.exit(0);
            }
        });
        buttons.add(Box.createHorizontalStrut(35));
        buttons.add(cancel);
        buttons.add(Box.createHorizontalStrut(20));

        JButton next = new JButton("Next");
        next.setDefaultCapable(true);
        checkOkStatus();
        next.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
                //application.createPointHolder(user, today, element, stage, Integer.parseInt(embNumField.getText()));
                dispose();
            }
        });
        buttons.add(next);
        
        
        add("South", buttons);
        
        pack();
        
        Dimension screen = getToolkit().getScreenSize();
        int x = getWidth();
        int y = getHeight();
        setLocation((screen.width-x)/2, (screen.height-y)/2);
        
    }

    protected void checkOkStatus() {
        if (embryoOK && elementOK) {
            login.setEnabled(true);
        } else {
            login.setEnabled(false);
        }
    }

    public void setApplication(Jogra application) {
        this.application = application;
    }
    
}

