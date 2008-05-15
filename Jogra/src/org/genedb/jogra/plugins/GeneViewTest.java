/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.genedb.jogra.plugins;

import javax.swing.JFrame;

/**
 *
 * @author art
 */
public class GeneViewTest {
    
    
    
    public static void main(String[] args) {
        JFrame f = new JFrame();
        GeneViewModel model = new GeneViewModel();
        GeneView gv = new GeneView(model);
        f.add(gv);
        f.pack();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

}
