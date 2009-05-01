/**
 * ORGANISM TREE PLUG-IN FOR JOGRA
 * This plug-in displays a Jtree with the hierarchy of organisms and allows the user to select an organism or a class of organisms.
 * This information is then used to restrict the products in the rationaliser (i.e. only products pertaining to selected organism(s) will be displayed.
 * This selection can potentially be transferred to other Jogra plugins in the future.
 * 
 * @author nds
 */

package org.genedb.jogra.plugins;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.*;

import org.bushe.swing.event.EventBus;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.genedb.jogra.domain.GeneDBMessage;
import org.genedb.jogra.drawing.Jogra;
import org.genedb.jogra.drawing.JograPlugin;
import org.genedb.jogra.drawing.OpenWindowEvent;
import org.genedb.jogra.services.NamedVector;
import org.genedb.jogra.services.CheckBoxNode;
import org.genedb.jogra.services.CheckBoxNodeEditor;
import org.genedb.jogra.services.CheckBoxNodeRenderer;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeManager;
import org.genedb.db.taxon.TaxonNodeArrayPropertyEditor;

public class OrganismTree implements JograPlugin {
    private TaxonNodeManager taxonNodeManager; 
    private String userSelection = new String(); //Stores the user's selected organism name
    private Jogra jogra;
    
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
                    System.err.println(e);
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
        button.setEnabled(false);
        ActionListener actionListener = new ActionListener(){
            public void actionPerformed(ActionEvent actionEvent){
                try{
                    frame.setVisible(false); //Make frame disappear
                    //frame.dispose(); //Or should frame be disposed??
                    jogra.setChosenOrganism(userSelection); //Tell Jogra about the user's selection
                }catch(Exception e){ //handle exceptions better
                    System.err.println(e);
                }
             }
        };
        button.addActionListener(actionListener);
        //TaxonNodeManager created and initialised at start up. Create JTree
        TaxonNode taxonNode = taxonNodeManager.getTaxonNodeForLabel("Root");
        Vector orgTree = getOrganismTree(taxonNode,"");
        JTree tree = new JTree(orgTree);
        CheckboxTree checkboxTree = new CheckboxTree(tree.getModel()); //checkboxnode constructor takes a tree model
        checkboxTree.addTreeCheckingListener(new TreeCheckingListener() {
            public void valueChanged(TreeCheckingEvent e) {
                if(e.isCheckedPath()){
                    System.out.println("User selected:  " + (e.getPath().getLastPathComponent()));
                    button.setText("Continue with " + (e.getPath().getLastPathComponent()));
                    userSelection = (e.getPath().getLastPathComponent()).toString();
                    button.setEnabled(true);
                }
            }
        });
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
    private Vector getOrganismTree(TaxonNode taxonNode, String indent){
        System.out.println(indent + taxonNode.getLabel());
        List<TaxonNode> childrenList = taxonNode.getChildren();
        Vector childrenVector = new Vector();
        for(TaxonNode child : childrenList){
            childrenVector.add(getOrganismTree(child, indent.concat("  ")));
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
