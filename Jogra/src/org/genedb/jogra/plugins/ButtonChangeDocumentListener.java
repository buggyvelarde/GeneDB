/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.genedb.jogra.plugins;

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author art
 */
public class ButtonChangeDocumentListener implements DocumentListener {
    
    private JButton button;

    public ButtonChangeDocumentListener(JButton button) {
        this.button = button;
    }
    
    public void changedUpdate(DocumentEvent e) {
        // Deliberately empty
    }
    
    public void insertUpdate(DocumentEvent e) {
        checkButton(e);
    }
    
    public void removeUpdate(DocumentEvent e) {
        checkButton(e);
    }
    
    void checkButton(DocumentEvent e) {
        final boolean state;
        if (e.getDocument().getLength()==0) {
            state = false;
        } else {
            state = true;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                button.setEnabled(state);
            }
        });
    }
}
