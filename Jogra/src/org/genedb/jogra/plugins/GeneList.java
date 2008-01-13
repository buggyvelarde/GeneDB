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

import org.genedb.db.domain.misc.GeneDBMessage;
import org.genedb.jogra.drawing.JograPlugin;
import org.genedb.jogra.drawing.OpenWindowEvent;
import org.jdesktop.swingx.JXList;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventServiceEvent;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

public class GeneList implements JograPlugin {

    public JFrame getMainPanel(final String title, List<String> uniqueNames) {
        JFrame ret = new JFrame();
        ret.setTitle("Gene List");
        
        ret.setLayout(new BorderLayout());
        
        JXList main = new JXList();
        main.setVisibleRowCount(15);
        main.setPrototypeCellValue("123456789012345678901234567890");
        //main.
//        String[] temp = new String[100];
//        for(int i=0; i < 100; i++) {
//        	temp[i]= "Line "+i;
//        }
        String[] names = uniqueNames.toArray(new String[uniqueNames.size()]);
        main.setListData(names);
        //main.setMinimumSize(main.getPreferredScrollableViewportSize());
        JScrollPane mainPane = new JScrollPane(main);
        
        ret.add(mainPane, BorderLayout.CENTER);
        
        JPanel controls = new JPanel();
        controls.add(new JButton("wibble"));
        
        ret.add(controls, BorderLayout.SOUTH);
        
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

}
