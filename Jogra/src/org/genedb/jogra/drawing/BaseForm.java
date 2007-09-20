package org.genedb.jogra.drawing;

import java.awt.Dimension;
import java.awt.GridBagConstraints;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class BaseForm extends JDialog {

    protected GridBagConstraints gbc = new GridBagConstraints();

    protected void addField(JPanel panel, String text, JComponent jc, Dimension preferred) {
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

}

