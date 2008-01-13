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
import org.genedb.db.domain.objects.ExtendedOrganism;
import org.genedb.db.domain.objects.ExtendedOrganismManager;
import org.genedb.jogra.drawing.Jogra;
import org.genedb.jogra.drawing.JograPlugin;
import org.genedb.jogra.drawing.OpenWindowEvent;
import org.genedb.jogra.drawing.Pair;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventServiceEvent;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;

public class OrganismTree implements JograPlugin {

    private JFrame frame;

    private MutableTreeNode root;

    private ExtendedOrganismManager organismManager;

    @SuppressWarnings("unchecked")
    public JFrame getMainPanel(final String title) {

        final JFrame ret = new JFrame();
        ret.setLayout(new BorderLayout());
        ret.setTitle(title);

        final Dimension size = new Dimension(200, 250);
        ret.setMinimumSize(size);

        root = new DefaultMutableTreeNode("GeneDB");
        // final MutableTreeNode child1 = new
        // DefaultMutableTreeNode("Apicomplexa");
        // final MutableTreeNode child2 = new DefaultMutableTreeNode("Malaria");
        // final MutableTreeNode child3 = new DefaultMutableTreeNode("P.
        // falciparum");
        // final MutableTreeNode child4 = new DefaultMutableTreeNode("P.
        // knowlesi");
        // final MutableTreeNode child5 = new DefaultMutableTreeNode("P.
        // vivax");
        // root.insert(child1, 0);
        // child1.insert(child2, 0);
        // child2.insert(child3, 0);
        // child2.insert(child4, 1);
        // child2.insert(child5, 2);

        final TreeModel model = new DefaultTreeModel(root);

        final JTree tree = new JTree(model);

        final JScrollPane scrollPane = new JScrollPane(tree);
        //ret.getContentPane().add(scrollPane);
        ret.add(scrollPane, BorderLayout.CENTER);
        
        Box buttons = Box.createHorizontalBox();
        buttons.add(Box.createHorizontalGlue());
        JCheckBox box1 = new JCheckBox("Loaded", true);
        buttons.add(box1);
        buttons.add(Box.createHorizontalStrut(10));
        JCheckBox box2 = new JCheckBox("Heirachy", false);
        buttons.add(box2);
        buttons.add(Box.createHorizontalGlue());
        
        ret.add(buttons, BorderLayout.SOUTH);
        
        new SwingWorker<Void, Pair<MutableTreeNode, MutableTreeNode>>() {
            @Override
            protected Void doInBackground() throws Exception {
                final ExtendedOrganism rootOrg = organismManager.getByName("Home");
                // TODO Set user object of root node to root of extended org.
                // heirachy
                traverseNodes(rootOrg, root);

                return null;
            }

            protected void process(final Pair<MutableTreeNode, MutableTreeNode>... pairs) {
                for (final Pair<MutableTreeNode, MutableTreeNode> pair : pairs) {
                    final int index = pair.getSecond().getChildCount();
                    pair.getSecond().insert(pair.getFirst(), index);
                }
            }

            private void traverseNodes(final ExtendedOrganism rootOrg, final MutableTreeNode parent) {
                for (final ExtendedOrganism org : rootOrg.getChildren()) {
                    final MutableTreeNode node = new DefaultMutableTreeNode(org.getFullName());
                    node.setUserObject(org);
                    final Pair<MutableTreeNode, MutableTreeNode> pair = new Pair(node, parent);
                    publish(pair);
                    traverseNodes(org, node);
                }
            }

        }.execute();

        return ret;
    }

    // public JPanel getMainWindowPlugin() {
    // return null;
    // }
    public JPanel getMainWindowPlugin() {
        final JPanel ret = new JPanel();
        final JButton temp = new JButton("Organism Tree");
        temp.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                System.err.println("Am I on EDT '" + EventQueue.isDispatchThread() + "' 1");
                new SwingWorker<JFrame, Void>() {

                    @Override
                    protected JFrame doInBackground() throws Exception {
                        return getMainPanel("Organism Heirachy");
                    }

                    @Override
                    protected void done() {
                        try {
                            final JFrame result = get();
                            final GeneDBMessage e = new OpenWindowEvent(OrganismTree.this, result);
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
        return "Organism Tree";
    }

    public int getOrder() {
        return 1;
    }

    public boolean isSingletonByDefault() {
        return true;
    }

    public boolean isUnsaved() {
        return false;
    }

    private JFrame makeWindow(final String organism) {
        System.err.println("Am I on EDT '" + EventQueue.isDispatchThread() + "'  x");
        final String title = "Organism: " + organism;
        JFrame lookup = Jogra.findNamedWindow(title);
        if (lookup == null) {
            lookup = getMainPanel(title);
        }
        return lookup;
    }

    public void setOrganismManager(final ExtendedOrganismManager manager) {
        this.organismManager = manager;
    }

}
