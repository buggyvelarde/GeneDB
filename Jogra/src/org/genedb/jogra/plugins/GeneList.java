/*
 * Copyright (c) 2007 Genome Research Limited.
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

package org.genedb.jogra.plugins;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import org.bushe.swing.event.EventBus;
import org.genedb.jogra.domain.GeneDBMessage;
import org.genedb.jogra.drawing.Jogra;
import org.genedb.jogra.drawing.JograPlugin;
import org.genedb.jogra.drawing.OpenWindowEvent;
import org.jdesktop.swingx.JXList;

public class GeneList implements JograPlugin {

    public JFrame getMainPanel(final String title, List<String> uniqueNames) {
        JFrame ret = new JFrame();
        ret.setTitle("Gene List");
        
        ret.setLayout(new BorderLayout());
        
        JPanel center = new JPanel();
        center.setLayout(new BorderLayout());
        center.add(new JLabel("Targets"), BorderLayout.NORTH);
        
        JXList main = new JXList();
        main.setVisibleRowCount(15);
        main.setPrototypeCellValue("123456789012345678901234567890123456789012345678901234567890");
        //main.
        String[] temp = new String[15];
        for(int i=0; i < 15; i++) {
        	temp[i]= " ";
        }
//        String[] names = uniqueNames.toArray(new String[uniqueNames.size()]);
        main.setListData(temp);
        //main.setMinimumSize(main.getPreferredScrollableViewportSize());
        JScrollPane mainPane = new JScrollPane(main);
        
        center.add(main, BorderLayout.CENTER);
        ret.add(center, BorderLayout.CENTER);
        
        //Box controls = Box.createVerticalBox();
        JTabbedPane transferTab = new JTabbedPane();
        transferTab.setBorder(BorderFactory.createEtchedBorder());
        
//        JPanel temp = new JPanel();
//        temp.add(new JLabel("stuff"));
//        temp.add(new JTextField("wibble"));
        JPanel name = new NameNotePanel();
        transferTab.addTab("Name/Misc", name);
        
        JPanel goPanel = new GoPanel();
        //goPanel.add(new JLabel("Evidence code"));
        //goPanel.add(new JComboBox(new String[]{"ISS", "IPP", "IDA"}));
        //goPanel.add(new JTextField("wibble"));
        transferTab.addTab("GO", goPanel);
        
//        JPanel ec = new JPanel();
//        ec.add(new JLabel("stuff"));
//        ec.add(new JTextField("wibble"));
//        transferTab.addTab("EC", ec);
        
        JPanel cc = new CCPanel();
        //cc.add(new JLabel("Evidence code"));
        //cc.add(new JTextField("wibble"));
        transferTab.addTab("Controlled Curation", cc);
        
//        JPanel note = new JPanel();
//        note.add(new JLabel("Note:"));
//        note.add(new JTextArea());
//        transferTab.addTab("note", note);

        //controls.add(transferTab);
        //controls.add(new JButton("wibble"));
        
        ret.add(transferTab, BorderLayout.SOUTH);
        
        ret.pack();
        return ret;
    }

    public JPanel getMainWindowPlugin() {
        final JPanel ret = new JPanel();
        final JButton temp = new JButton("Load Gene List");
        temp.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                System.err.println("Am I on EDT '" + EventQueue.isDispatchThread() + "' 1");
                new SwingWorker<JFrame, Void>() {

                    @Override
                    protected JFrame doInBackground() throws Exception {
                        return getMainPanel("wibble", Collections.<String>emptyList());
                    }

                    @Override
                    public void done() {
                        try {
                            final GeneDBMessage e = new OpenWindowEvent(GeneList.this, get());
                            EventBus.publish(e);
                        } catch (final InterruptedException exp) {
                            exp.printStackTrace();
                        } catch (final ExecutionException exp) {
                            exp.printStackTrace();
                        }
                    }
                }.execute();
            }
        });
        ret.add(temp);
        return ret;
    }

    public String getName() {
        return "Gene list viewer";
    }

    public int getOrder() {
        return 5;
    }

    public boolean isSingletonByDefault() {
        return true;
    }

    public boolean isUnsaved() {
        // TODO Auto-generated method stub
        return false;
    }

	public void process(List<String> newArgs) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void setJogra(Jogra jogra) {
        // TODO Auto-generated method stub
        
    }

}
