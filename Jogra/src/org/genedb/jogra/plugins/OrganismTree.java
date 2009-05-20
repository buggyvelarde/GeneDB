/*
 * ORGANISM TREE PLUG-IN FOR JOGRA (April 2009)
 * This plug-in displays a Jtree with the hierarchy of organisms and allows the user to select an organism or a class of organisms.
 * This information is then used to restrict the products in the rationaliser (i.e. only products pertaining to selected organism(s) will be displayed.
 * This selection can potentially be transferred to other Jogra plugins in the future.
 * 
 * 19.5.2009: Added capability to receive multiple organism selections from user
 * 
 * @author nds
 */

package org.genedb.jogra.plugins;

import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeManager;
import org.genedb.jogra.drawing.Jogra;
import org.genedb.jogra.drawing.JograPlugin;
import org.genedb.jogra.services.NamedVector;

import org.apache.log4j.Logger;
import org.bushe.swing.event.EventBus;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTree;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingEvent;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingListener;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.tree.TreePath;

public class OrganismTree implements JograPlugin {
    private TaxonNodeManager taxonNodeManager; 
    private List<String> userSelection = new ArrayList<String>(); //Stores the user's selected organism name
    private Jogra jogra;
    
    private static final Logger logger = Logger.getLogger(OrganismTree.class);
    
    public void setTaxonNodeManager(TaxonNodeManager taxonNodeManager) {
        this.taxonNodeManager = taxonNodeManager;
    }
  
    /**
     * Supply a JPanel which will be displayed in the main Jogra application panel,
     * used for launching a plug-in, or displaying status
     *
     * @return a JPanel, ready for displaying
     */
    public JPanel getMainWindowPlugin(){ 
        final JPanel panel = new JPanel();
        final JLabel label = new JLabel("Restrict products by organism");
        final JButton button = new JButton("Load organism tree");
        //When user clicks button, load a new frame with Jtree
        ActionListener actionListener = new ActionListener(){
            public void actionPerformed(ActionEvent actionEvent){
                try{
                    new SwingWorker<JFrame, Void>() {
                        @Override
                        protected JFrame doInBackground() throws Exception {
                          return getMainPanel();
                        }
                    }.execute();     
                }catch(Exception e){ //handle exceptions better later
                    logger.debug(e);
                }
             }
        };
        button.addActionListener(actionListener);
        panel.add(label);
        panel.add(button);
        return panel;
    }
    
    /**
     * Method that creates the JFrame object containing the tree of organisms
     * @return
     */
    public JFrame getMainPanel() {
        final JFrame frame = new JFrame("Hierarchy of organisms currently on database");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JButton button = new JButton("Select an organism");   //Create button to confirm user selection. Disabled at first
       
        //TaxonNodeManager created and initialised at start up. Create JTree
        TaxonNode taxonNode = taxonNodeManager.getTaxonNodeForLabel("Root");
        Vector orgTree = getOrganismTree(taxonNode);
        JTree tree = new JTree(orgTree);
        final CheckboxTree checkboxTree = new CheckboxTree(tree.getModel()); //checkboxnode constructor takes a tree model
        checkboxTree.getCheckingModel().setCheckingMode(TreeCheckingModel.CheckingMode.PROPAGATE_PRESERVING_CHECK); //Check if this can be both SINGLE & PROPOGATE
        checkboxTree.addTreeCheckingListener(new TreeCheckingListener() {
            public void valueChanged(TreeCheckingEvent e) {
                if(e.isCheckedPath()){
                    button.setText("Continue"); 
                    button.setEnabled(true);
                }
            }
        });
        
        button.setEnabled(false);
        ActionListener actionListener = new ActionListener(){
            public void actionPerformed(ActionEvent actionEvent){
              
                try{
                    userSelection.clear();
                    TreePath tp[] = checkboxTree.getCheckingRoots();
                   
                    for(TreePath p: tp){
                        userSelection.add(p.getLastPathComponent().toString());
                    }
                    frame.setVisible(false); //Make frame disappear
                    //frame.dispose(); //Or should frame be disposed??
                    jogra.setChosenOrganism(userSelection); //Tell Jogra about the user's selection
                    EventBus.publish("selection", userSelection); //EventBus publish mechanism here needs to be tested later
                    logger.info("ORGANISM TREE: User selected " + userSelection.toString());
                }catch(Exception e){ //handle exceptions better
                    logger.debug(e);
                    e.printStackTrace();
                }
             }
        };
        button.addActionListener(actionListener);
        //Place Jtree in scrollable pane to enable scrolling
        JScrollPane scrollPane = new JScrollPane(checkboxTree);
        frame.add(scrollPane, BorderLayout.CENTER); 
        frame.add(button, BorderLayout.PAGE_END);
        frame.setSize(500,500);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        return frame;
    }
    
    /**
     * A recursive helper method to print indented organism hierarchy (for testing purposes) and return a vector
     *
     * @return Vector containing organism hierarchy
     */

    private Vector getOrganismTree(TaxonNode taxonNode){
        List<TaxonNode> childrenList = taxonNode.getChildren();
        Vector childrenVector = new Vector();
        for(TaxonNode child : childrenList){
           if(child.isLeaf()){
               childrenVector.add(child.getLabel());
           }else{
               childrenVector.add(getOrganismTree(child));
           }
        }
        return new NamedVector(taxonNode.getLabel(), childrenVector);
    }

    /**
     * The name of the plug-in, maybe this should be set in the config
     *
     * @return the name
     */
    public String getName(){
        return "Organism Tree";
    }

    /**
     * Is there only one instance of the plug-in, by default
     *
     * @return true if there should only be one copy of the plug-in
     */
    public boolean isSingletonByDefault(){
        return false; //Shouldn't this be true by default?
    }

    /**
     * Allow the plug-in to indicate whether it has unsaved changes
     *
     * @return true if there are changes to be saved
     */
    public boolean isUnsaved(){
        return false;
    }

    /**
     * @param newArgs
     */
    public void process(List<String> newArgs){
        //What is this meant to do?
    }

    @Override
    public void setJogra(Jogra jogra) {
        this.jogra = jogra;
    }
    
   
    

}
