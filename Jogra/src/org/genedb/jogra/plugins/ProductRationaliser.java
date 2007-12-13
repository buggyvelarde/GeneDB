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

import org.genedb.db.domain.misc.MethodResult;
import org.genedb.db.domain.objects.Product;
import org.genedb.db.domain.services.ProductService;
import org.genedb.jogra.drawing.Jogra;
import org.genedb.jogra.drawing.JograPlugin;
import org.genedb.jogra.drawing.OpenWindowEvent;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventServiceEvent;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;


public class ProductRationaliser implements JograPlugin {
	
	private static final String WINDOW_TITLE = "Product Rationalisation";
	private ProductService productService;
	JList fromList;
	JList toList; 
	
	private void initModels() {
		List<Product> products = productService.getProductList();
		Product[] productArray = new Product[products.size()];
		int i=0;
		for (Product product : products) {
			productArray[i] = product;
			i++;
		}
		fromList.setListData(productArray);
		toList.setListData(productArray);
	}
	
    public JFrame getMainPanel() {

        fromList = new JList();
        fromList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        toList = new JList();
        toList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	initModels();
    	
    	
        final JFrame ret = new JFrame();
        ret.setTitle(WINDOW_TITLE);
        ret.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        ret.setLayout(new BorderLayout());
        

        
        Box center = Box.createHorizontalBox();
        center.add(Box.createHorizontalStrut(5));
        center.add(new JScrollPane(fromList));
        center.add(Box.createHorizontalStrut(3));
        center.add(new JScrollPane(toList));
        center.add(Box.createHorizontalStrut(5));
        
        ret.add(center, BorderLayout.CENTER);
        
        Box buttons = Box.createHorizontalBox();
        buttons.add(Box.createHorizontalGlue());
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent evt) {
        		initModels(); // FIXME
        	}
        });
        buttons.add(refresh);
        buttons.add(Box.createHorizontalStrut(10));
        JButton go = new JButton("Go");
        go.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		Product to = (Product) toList.getSelectedValue();
        		Object[] from = fromList.getSelectedValues();
        		List<Product> old = new ArrayList<Product>();
        		for (Object o : from) {
					old.add((Product)o);
				}
        		MethodResult result = productService.rationaliseProduct(to, old);
        		// TODO Check results
        		initModels();
        	}
        });
        buttons.add(go);
        buttons.add(Box.createHorizontalGlue());
        ret.add(buttons, BorderLayout.SOUTH);
        
        ret.pack();
        
        return ret;
    }

    public JPanel getMainWindowPlugin() {
        final JPanel ret = new JPanel();
        final JButton temp = new JButton("Load Product Rationaliser");
        temp.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                System.err.println("Am I on EDT '" + EventQueue.isDispatchThread() + "' 1");
                new SwingWorker<JFrame, Void>() {

                    @Override
                    protected JFrame doInBackground() throws Exception {
                        return makeWindow();
                    }

                    @Override
                    public void done() {
                        try {
                            final EventServiceEvent e = new OpenWindowEvent(ProductRationaliser.this, get());
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
        return "Product Rationaliser";
    }

    public int getOrder() {
        return 7;
    }

    public boolean isSingletonByDefault() {
        return true;
    }

    public boolean isUnsaved() {
        // TODO Auto-generated method stub
        return false;
    }

    private JFrame makeWindow() {
        System.err.println("Am I on EDT '" + EventQueue.isDispatchThread() + "'  x");
        JFrame lookup = Jogra.findNamedWindow("Product Rationaliser");
        if (lookup == null) {
            lookup = getMainPanel();
        }
        return lookup;
    }

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

}
