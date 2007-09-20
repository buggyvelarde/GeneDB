/*
 * Copyright (c) 2007 Genome Research Limited.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as published
 * by  the Free Software Foundation; either version 2 of the License or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program; see the file COPYING.LIB.  If not, write to
 * the Free Software Foundation Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307 USA
 */

package org.genedb.jogra.drawing;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventServiceEvent;

import java.awt.EventQueue;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


public class GeneEditor implements JograPlugin {

    public JPanel getMainPanel() {
        
        FormLayout fl = new FormLayout(
                "pref 4dlu pref",
                "pref 2dlu pref 2dlu pref 2dlu pref 2dlu pref 2dlu pref 2dlu pref");
        JPanel ret = new JPanel(fl);
        //ret.add(new JLabel("Hello"));
        //ret.add(new JLabel("World"));
        CellConstraints cc = new CellConstraints();
        
        JTextField instance = new JTextField("wibble");
        ret.add(new JLabel("Systematic id"), cc.xy(1,1));
        ret.add(instance, cc.xy(3, 1));

        JLabel organism = new JLabel("<html><i>Plasmodium falciparum</i> 3D7</html>");
        ret.add(new JLabel("Organism"), cc.xy(1,3));
        ret.add(organism, cc.xy(3, 3));
        
        JTextField username = new JTextField("wobble");
        ret.add(new JLabel("Name"), cc.xy(1,5));
        ret.add(username, cc.xy(3, 5));

        JTextField password = new JTextField("hypothetical protein");
        ret.add(new JLabel("Product"), cc.xy(1,7));
        ret.add(password, cc.xy(3, 7));
        

        JTextField blank = new JTextField(" ");
        ret.add(new JLabel("Orthologues"), cc.xy(1,9));
        ret.add(blank, cc.xy(3, 9));
        
        ret.add(new JLabel("Paralogues"), cc.xy(1,11));
        ret.add(blank, cc.xy(3, 11));
        
        ret.add(new JLabel("Clusters"), cc.xy(1,13));
        ret.add(new JButton("Show others in cluster"), cc.xy(3, 13));
        return ret;
    }

    public boolean isSingletonByDefault() {
        return false;
    }

    public boolean isUnsaved() {
        // TODO Auto-generated method stub
        return false;
    }
    
    private JPanel makeWindow() {
        System.err.println("Am I on EDT '"+EventQueue.isDispatchThread()+"'  x");
        return getMainPanel();
    }

    public JPanel getMainWindowPlugin() {
        JPanel ret = new JPanel();
        JButton temp = new JButton("Gene Editor Search");
        temp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                System.err.println("Am I on EDT '"+EventQueue.isDispatchThread()+"' 1");
                new SwingWorker() {
                    @Override public Object construct() {
                        return makeWindow();
                    }
                    @Override public void finished() {
                        EventServiceEvent e = new OpenWindowEvent(GeneEditor.this, (JPanel) getValue());
                        EventBus.publish(e);
                    }
                }.start();
            }
        });
        ret.add(temp);
        return ret;
    }

    public int getOrder() {
        return 1;
    }

    public String getName() {
        return "Gene Editor";
    }

}
