/*
 * Copyright (c) 2009 Genome Research Limited.
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

import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeManager;
import org.genedb.jogra.domain.GeneDBMessage;
import org.genedb.jogra.domain.Term;
import org.genedb.jogra.drawing.Jogra;
import org.genedb.jogra.drawing.JograPlugin;
import org.genedb.jogra.drawing.JograProgressBar;
import org.genedb.jogra.drawing.OpenWindowEvent;
import org.genedb.jogra.services.FilteringJList;
import org.genedb.jogra.services.RationaliserResult;
import org.genedb.jogra.services.TermService;

import org.apache.log4j.Logger;
import org.bushe.swing.event.EventBus;
import org.springframework.util.StringUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.google.common.collect.Maps;

/********************************************************************************************************
 * The TermRationalier is a tool that enables curators to correct product names and controlled curation
 * terms. It is most useful for curators wanting to work on a specific organism or a set of organisms as
 * these can be selected via the Organism tree in Jogra and they will be passed on to the rationaliser 
 * which will then only display the relevant terms. This implements the JograPlugin interface and, as 
 * expected, is one of the Jogra plugins.
 ********************************************************************************************************/
public class TermRationaliser implements JograPlugin {
    
    private static final Logger logger = Logger.getLogger(TermRationaliser.class);

    /* Constants */
    private static final String WINDOW_TITLE = "Term Rationaliser";
  
    /* Variables for rationaliser functionality */   
    private TermService termService;                                            //Is the interface to the SQLTermService
    private TaxonNodeManager taxonNodeManager;                                  //TaxonNodeManager to get the organism phylotree
    private List<TaxonNode> selectedTaxons = new ArrayList<TaxonNode>();        //Taxons corresponding to the selected organism names
    private Jogra jogra;                                                        //Instance of Jogra
    private boolean showEVC;                                                    //Show Evidence codes?
    private boolean showSysID;                                                  //Show systematic IDs?
    private String termType;                                                    //Products or Controlled Curation terms
    private List<Term> terms = new ArrayList<Term>();                           //All terms (for JList)
    private LinkedHashMap<String, String> instances = Maps.newLinkedHashMap();  //To hold the types of cvterms

    /*Variables related to the user interface */
    private FilteringJList fromList = new FilteringJList();
    private FilteringJList toList = new FilteringJList();
    private JTextField textField;
    private JTextField idField1;
    private JTextField idField2;
    private JLabel productCountLabel;
    private JLabel scopeLabel = new JLabel("Organism(s): All organisms");       //Label showing user's selection. Default: All organisms
    private JTextArea information = new JTextArea(10,10);   
    
    
    /**
     * This method supplies the JPanel which is displayed in the main Jogra window.
     * It has options for the user to select: type of term and whether or not 
     * the rationaliser should display evidence codes and systematic IDs
     */
    public JPanel getMainWindowPlugin() {
        
        this.populateTermTypes();
        
        final JPanel ret = new JPanel();
        final JButton loadButton = new JButton("Load Term Rationaliser");
        final JLabel chooseType = new JLabel("Select term: ");
        final JComboBox termTypeBox = new JComboBox(instances.keySet().toArray());
        final JCheckBox showEVCFilter = new JCheckBox("Highlight terms with evidence codes", false);
        final JCheckBox showSysIDFilter = new JCheckBox("Retrieve systematic IDs for terms", false);
        
        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                System.err.println("Am I on EDT '" + EventQueue.isDispatchThread() + "' 1");
                new SwingWorker<JFrame, Void>() {
                    @Override
                    protected JFrame doInBackground() throws Exception {
                        ret.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        setTermType(instances.get((String)termTypeBox.getSelectedItem()));
                        setShowEVC(showEVCFilter.isSelected());
                        setShowSysID(showSysIDFilter.isSelected());
                        return makeWindow();
                    }

                    @Override
                    public void done() {
                        try {
                            final GeneDBMessage e = new OpenWindowEvent(TermRationaliser.this, get());
                            EventBus.publish(e);
                        } catch (final InterruptedException exp) {
                            exp.printStackTrace();
                        } catch (final ExecutionException exp) {
                            exp.printStackTrace();
                        }
                        ret.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));  
                    }
                }.execute();
            }
        });
        Box verticalBox = Box.createVerticalBox();
        Box horizontalBox = Box.createHorizontalBox();
        horizontalBox.add(chooseType);
        horizontalBox.add(termTypeBox);
        verticalBox.add(horizontalBox);
        verticalBox.add(loadButton);
        verticalBox.add(showEVCFilter);
        verticalBox.add(showSysIDFilter);   
        ret.add(verticalBox);
        return ret;
    }
    
    /**
     * Fetches the terms from the database and sets that as the model for both the Jlists: toList and fromList
     * The getTerms() method can be quite a time-consuming task depending on the number of terms. So, we push 
     * this into a Worker thread.
     */
    private void initModels() {
        JograProgressBar jpb = new JograProgressBar("Loading terms..."); //Progress bar added for better user information
        List<String> selectedOrganismNames = jogra.getSelectedOrganismNames();  //Names of organisms selected by the user passed here by Jogra
        logger.info("TR has asked JOgra for selection and got: " + StringUtils.collectionToCommaDelimitedString(selectedOrganismNames));
        if(selectedOrganismNames!=null && selectedOrganismNames.size()!=0 && !selectedOrganismNames.contains("root")){ // 'root' with a simple r causes problems
           scopeLabel.setText("Organism(s): " + StringUtils.collectionToCommaDelimitedString(selectedOrganismNames)); //Else, label will continue to have 'Scope: All organisms'
           if(selectedTaxons!=null && selectedTaxons.size()>0){
               selectedTaxons.clear();
           }
           for(String s: selectedOrganismNames){
               selectedTaxons.add(taxonNodeManager.getTaxonNodeForLabel(s));
           }
        }else{ //If there are no selections, get all terms
            selectedTaxons.add(taxonNodeManager.getTaxonNodeForLabel("Root"));
        }
        
        /*Loading the terms can sometimes take a while (e.g., to load ALL the terms). 
         * Hence we do it inside a worker thread
         */
        SwingWorker worker = new SwingWorker<List<Term>, Void>() {
            @Override
            public List<Term> doInBackground() { 
                terms = termService.getTerms(selectedTaxons, getTermType());
                Collections.sort(terms);
                return terms;
            }
            @Override
            public void done() { }
        };
        worker.run();
 
        fromList.addAll(terms);
        toList.addAll(terms);

        /* Fetch systematic IDs and evidence codes if the user has selected these options. At the moment, we fetch all of them but this may
         * be optimised by doing this 'on-the-fly' so that this information is fetched for the terms that the user is interested in 
         * (i.e. clicks on) */
        for (Term term : terms) {
            if(isShowSysID()){ 
               term.setSystematicIds(termService.getSystematicIDs(term, selectedTaxons));    
            }
            if(isShowEVC()){ 
                term.setEvidenceCodes(termService.getEvidenceCodes(term));    
            }
        }

        //'Re-set' the other textboxes in the interface
        productCountLabel.setText(String.format("Number of terms: %d terms found (%s)", terms.size(), this.getTermType()));

        if(isShowSysID()){
            idField1.setText("");
            idField2.setText("");
        }else{
            idField1.setText("(not enabled)");
            idField1.setEnabled(false);
            
            idField2.setText("(not enabled)");
            idField2.setEnabled(false);
        }
        textField.setText("");      
        
        jpb.stop();
    }
 

    /**
     * Return a new, initialised JFrame which is the main interface to the rationaliser.
     */
    public JFrame getMainPanel() {
        
        fromList.clearSelection(); //Clear any previous selections (if any)
        toList.clearSelection(); 

        if(isShowEVC()){ //If user has requested to view evidence codes, then use different renderer so that items with evidence codes are displayed in different colour
            ColorRenderer cr = new ColorRenderer();
            fromList.setCellRenderer(cr);
            toList.setCellRenderer(cr);
        }
    
        /* Label to display number of products */
        productCountLabel = new JLabel("Number of terms");
        
        /* Textfield displaying the name of the term to be edited*/
        textField = new JTextField(20);
        textField.setForeground(Color.BLUE);
        
        /* Systematic ID fields */
        idField1 = new JTextField(20);
        idField1.setEditable(false);
        idField1.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        idField1.setForeground(Color.DARK_GRAY);
        
        idField2 = new JTextField(20);
        idField2.setEditable(false);
        idField2.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        idField2.setForeground(Color.DARK_GRAY);
              
        /* FROM list */
        fromList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); //Allow multiple products to be selected 
        fromList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Term highlightedTerm = (Term)fromList.getSelectedValue();  
                if(highlightedTerm!=null){
                    if(isShowSysID()){ //Show systematic ID of term in the from list
                        idField1.setText(StringUtils.collectionToCommaDelimitedString(highlightedTerm.getSystematicIds()));
                    }
                }
            }
        });

        fromList.addKeyListener(new KeyListener(){    
            @Override
            public void keyPressed(KeyEvent arg0) {
                if(arg0.getKeyCode()==KeyEvent.VK_RIGHT){
                     synchroniseLists(fromList, toList);
                }
            }
            
            @Override
            public void keyReleased(KeyEvent arg0) {}
 
            @Override
            public void keyTyped(KeyEvent arg0) {}
        });

       
        /* TO list */
        toList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); //Single product selection in TO list
        toList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Term highlightedTerm = (Term)toList.getSelectedValue();  
                if(highlightedTerm!=null){
                    textField.setText(highlightedTerm.getName()); //Allow the user to edit the spelling of the term in the to list
                    if(isShowSysID()){ //Show systematic ID of the term in the to list
                        idField2.setText(StringUtils.collectionToCommaDelimitedString(highlightedTerm.getSystematicIds()));
                    }
                }
            }
        });
        
        toList.addKeyListener(new KeyListener(){    
            @Override
            public void keyPressed(KeyEvent arg0) {
                if(arg0.getKeyCode()==KeyEvent.VK_LEFT){
                     synchroniseLists(toList, fromList);
                }
            }
            @Override
            public void keyReleased(KeyEvent arg0) {}
 
            @Override
            public void keyTyped(KeyEvent arg0) {}
        });
     
       initModels();
        
       final JFrame ret = new JFrame();   
       ret.setTitle(WINDOW_TITLE);
       ret.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
       ret.setLayout(new BorderLayout());

       /* MENU */
       JMenuBar menuBar = new JMenuBar();
       JMenu menu = new JMenu("Help");
       menu.setMnemonic(KeyEvent.VK_H);
       JMenuItem menuItem1 = new JMenuItem("About");
       menuItem1.setMnemonic(KeyEvent.VK_A);
       menu.add(menuItem1);
       menuBar.add(menu);
       ret.setJMenuBar(menuBar);

       /* TO and FROM lists */
       ClassLoader classLoader = this.getClass().getClassLoader(); //Needed to access the images later on
       
       Box center = Box.createHorizontalBox(); //A box that displays contents from left to right
       center.add(Box.createHorizontalStrut(5)); //Invisible fixed-width component
  
       /*FROM LIST - Left hand side */
       Box leftPane = Box.createVerticalBox();
       leftPane.add(new JLabel("From"));

       JTextField fromSearchField = new JTextField(20);
       fromList.installJTextField(fromSearchField);
   
       leftPane.add(fromSearchField); 
       JScrollPane fromScrollPane = new JScrollPane();
       fromScrollPane.setViewportView(fromList);
       fromScrollPane.setPreferredSize(new Dimension(500,400));
       leftPane.add(fromScrollPane);

       
       //Systematic ID box for from list
       TitledBorder sysIDBorder = BorderFactory.createTitledBorder("Systematic IDs");
       sysIDBorder.setTitleColor(Color.DARK_GRAY);
       Box fromSysIDBox = Box.createVerticalBox();
       fromSysIDBox.add(idField1);
       fromSysIDBox.setBorder(sysIDBorder);
     
       leftPane.add(fromSysIDBox);
       leftPane.add(Box.createVerticalStrut(55));
       
       center.add(leftPane);
       center.add(Box.createHorizontalStrut(3));
       
       /* Middle pane with synchronise buttons */
       Box middlePane = Box.createVerticalBox();
    
       
       ImageIcon leftButtonIcon = new ImageIcon(classLoader.getResource("left_arrow.gif"));
       ImageIcon rightButtonIcon = new ImageIcon(classLoader.getResource("right_arrow.gif"));

       
       leftButtonIcon = new ImageIcon(leftButtonIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)); //TODO: Investigate simpler way to resize an icon!
       rightButtonIcon = new ImageIcon(rightButtonIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)); //TODO: Investigate simpler way to resize an icon!

       JButton rightSynch = new JButton(rightButtonIcon);
       rightSynch.setToolTipText("Synchronise TO list. \n Shortcut: Right-arrow key");
       
       rightSynch.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent actionEvent){
               synchroniseLists(fromList, toList);
           }
       });
      
       
       JButton leftSynch = new JButton(leftButtonIcon);
       leftSynch.setToolTipText("Synchronise FROM list. \n Shortcut: Left-arrow key");
       
       leftSynch.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent actionEvent){
               synchroniseLists(toList, fromList);
           }
       });

       middlePane.add(rightSynch);
       middlePane.add(leftSynch);
       
       center.add(middlePane);
       center.add(Box.createHorizontalStrut(3));
       
       /* TO LIST - Right hand side */
       Box rightPane = Box.createVerticalBox();
       rightPane.add(new JLabel("To"));
  
       JTextField toSearchField = new JTextField(20);
       toList.installJTextField(toSearchField);
 
       rightPane.add(toSearchField);
       JScrollPane toScrollPane = new JScrollPane(/*toList*/);
       toScrollPane.setViewportView(toList);
       toScrollPane.setPreferredSize(new Dimension(500,400));
       //toScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
       rightPane.add(toScrollPane);
       
       //Systematic ID box for to list
       Box toSysIDBox = Box.createVerticalBox();
       toSysIDBox.add(idField2);
       toSysIDBox.setBorder(sysIDBorder);
       rightPane.add(toSysIDBox);
       
       /* Add a box to edit the name of a product */
       Box newTerm = Box.createVerticalBox();
       newTerm.add(textField);
       TitledBorder editBorder = BorderFactory.createTitledBorder("Edit term name");
       editBorder.setTitleColor(Color.DARK_GRAY);
       newTerm.setBorder(editBorder);
       rightPane.add(newTerm);
      
       center.add(rightPane);
       center.add(Box.createHorizontalStrut(5));

       ret.add(center);

       /*Buttons and information boxes */
       Box main = Box.createVerticalBox();
       TitledBorder border = BorderFactory.createTitledBorder("Information");
       border.setTitleColor(Color.DARK_GRAY);

       /* Information box */
       Box info = Box.createVerticalBox();
       
       Box scope = Box.createHorizontalBox();
       scope.add(Box.createHorizontalStrut(5));
       scope.add(scopeLabel);
       scope.add(Box.createHorizontalGlue());

       Box productCount = Box.createHorizontalBox();
       productCount.add(Box.createHorizontalStrut(5));
       productCount.add(productCountLabel);
       productCount.add(Box.createHorizontalGlue());

       info.add(scope);
       info.add(productCount);
       info.setBorder(border);

       /* Action buttons */
       Box actionButtons = Box.createHorizontalBox();
       actionButtons.add(Box.createHorizontalGlue());
       actionButtons.add(Box.createHorizontalStrut(10));
   
       JButton findFix = new JButton(new FindClosestMatchAction());
       actionButtons.add(findFix);
       actionButtons.add(Box.createHorizontalStrut(10));

       RationaliserAction ra = new RationaliserAction();
       JButton go = new JButton(ra);
       actionButtons.add(go);
       actionButtons.add(Box.createHorizontalGlue());

       /* Show more information toggle */
       Box buttonBox = Box.createHorizontalBox();
       final JButton toggle = new JButton("Hide information <<");

       buttonBox.add(Box.createHorizontalStrut(5));
       buttonBox.add(toggle);
       buttonBox.add(Box.createHorizontalGlue());

       Box textBox = Box.createHorizontalBox();

       final JScrollPane scrollPane = new JScrollPane(information);
       scrollPane.setPreferredSize(new Dimension(ret.getWidth(),100));
       scrollPane.setVisible(true);
       textBox.add(Box.createHorizontalStrut(5));
       textBox.add(scrollPane); 

       ActionListener actionListener = new ActionListener(){
           public void actionPerformed(ActionEvent actionEvent){
               if(toggle.getText().equals("Show information >>")){
                   scrollPane.setVisible(true);
                   toggle.setText("Hide information <<");
                   ret.setPreferredSize(new Dimension(ret.getWidth(),ret.getHeight()+100));
                   ret.pack();
               }else if(toggle.getText().equals("Hide information <<")){
                   scrollPane.setVisible(false);
                   toggle.setText("Show information >>");
                   ret.setPreferredSize(new Dimension(ret.getWidth(),ret.getHeight()-100));
                   ret.pack();
               }
           }
       };
       toggle.addActionListener(actionListener);

       main.add(Box.createVerticalStrut(5));
       main.add(info);
       main.add(Box.createVerticalStrut(5));
      // main.add(newTerm);
       main.add(Box.createVerticalStrut(5));
       main.add(actionButtons);
       main.add(Box.createVerticalStrut(10));
       main.add(buttonBox);
       main.add(textBox);

       ret.add(main, BorderLayout.SOUTH);
      // ret.setPreferredSize(new Dimension(800,800));
       ret.pack();

       return ret;
    }
    

    /* */
    
    public JFrame makeWindow() {
        System.err.println("Am I on EDT '" + EventQueue.isDispatchThread() + "'  x");
       /* JFrame lookup = Jogra.findNamedWindow(WINDOW_TITLE);
        if (lookup == null) {
            lookup = getMainPanel(); 
        } */
        JFrame lookup = getMainPanel(); //Always getting a new frame since it has to pick up variable organism (improve efficiency later: NDS)
        return lookup;
    }
    
    
    /* Create a Box to hold the left and right panels which are nearly identical 
     * except that the right panel has a textbox to edit the term name */
//    private Box createPane(String label){
//
//        Box pane = Box.createVerticalBox();
//        pane.add(new JLabel(label));
//   
//        JTextField searchField = new JTextField(20);
//        toList.installJTextField(searchField);
//  
//        pane.add(searchField);
//        JScrollPane scrollPane = new JScrollPane();
//        scrollPane.setViewportView(toList);
//        toScrollPane.setPreferredSize(new Dimension(500,400));
//        //toScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//        rightPane.add(toScrollPane);
//        
//        //Systematic ID box for to list
//        Box toSysIDBox = Box.createVerticalBox();
//        toSysIDBox.add(idField2);
//        toSysIDBox.setBorder(sysIDBorder);
//        rightPane.add(toSysIDBox);
//        
//        /* Add a box to edit the name of a product */
//        Box newTerm = Box.createVerticalBox();
//        newTerm.add(textField);
//        TitledBorder editBorder = BorderFactory.createTitledBorder("Edit term name");
//        editBorder.setTitleColor(Color.DARK_GRAY);
//        newTerm.setBorder(editBorder);
//        rightPane.add(newTerm);
//        
//        
//        
//        
//        
//    }
    


    
    /**
     * PRIVATE HELPER METHODS
     */
    private void synchroniseLists(JList sourceList, JList targetList){
        Term term = (Term)sourceList.getSelectedValue();
        targetList.setSelectedValue(term, true);
        targetList.ensureIndexIsVisible(targetList.getSelectedIndex());
    }
    
    
    private void populateTermTypes(){
        instances.put("Products", "genedb_products");
        instances.put("Controlled Curation Terms", "CC_genedb_controlledcuration");
        //Add here for more
    }
    
    private void writeMessage(String m){
        this.information.setText(information.getText().concat(m).concat("\n"));
    }
    
    /**
     *  SETTER/GETTER METHODS
     */  
    public void setSelectedTaxons(List<TaxonNode> selectedTaxons){
        this.selectedTaxons = selectedTaxons;
    }
    
    public List<TaxonNode> getSelectedTaxons(){
        return this.selectedTaxons;
    } 
    
    
    public void setTaxonNodeManager(TaxonNodeManager taxonNodeManager) {
        this.taxonNodeManager = taxonNodeManager;
    }
    
    public void setTermType(String type){
        this.termType = type;
    }
    
    public String getTermType(){
        return termType;
    }

    public String getName() {
        return WINDOW_TITLE;
    }
    
    public void setShowEVC(boolean value){
        showEVC = value;
    }
    
    public boolean isShowEVC(){
        return showEVC;
    }
    
    public void setShowSysID(boolean value){
        showSysID = value;
    }
    
    public boolean isShowSysID(){
        return showSysID;
    }

    public boolean isSingletonByDefault() {
        return true;
    }

    public boolean isUnsaved() {
        // TODO
        return false;
    }

    
    public void setTermService(TermService termService) {
        this.termService = termService;
    }


    /*************************************************************************************
     * An action wrapping code which identifies the closest match in the right hand column
     * to the selected value in the left hand column. Closest is defined by the smallest
     * Levenshtein value.
     *************************************************************************************/
    class FindClosestMatchAction extends AbstractAction implements ListSelectionListener {

        public FindClosestMatchAction() {
            putValue(Action.NAME, "Find possible fix");
            
            ClassLoader classLoader = this.getClass().getClassLoader();
            ImageIcon hammerIcon = new ImageIcon(classLoader.getResource("hammer_and_spanner.png"));    
            hammerIcon = new ImageIcon(hammerIcon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH)); //TODO: Investigate simpler way to resize an icon!
            
            putValue(Action.SMALL_ICON, hammerIcon);
            fromList.addListSelectionListener(this);
            enableBasedOnSelection();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            
            Term from = (Term) fromList.getSelectedValue();

            int match = findClosestMatch(from.getName(), fromList.getSelectedIndex(), toList.getModel());
            if (match != -1) {
                toList.setSelectedIndex(match);
                toList.ensureIndexIsVisible(match);
            }
            
        }

        int findClosestMatch(String in, int fromIndex, ListModel list) {
            //System.err.println("Looking for match for '"+in+"'");
            int current = -1;
            int distance = Integer.MAX_VALUE;
            for (int i = 0; i < list.getSize(); i++) {
                if (i == fromIndex) {
                    continue;
                }
                String element = ((Term)list.getElementAt(i)).getName();
                if (in.equalsIgnoreCase(element)) {
                    System.err.println("Found identical except case at '"+i+"'");
                    return i;
                }
                int d = org.apache.commons.lang.StringUtils.getLevenshteinDistance(in, element);
                if (d==1) {
                    //System.err.println("Found 1 away at '"+i+"'");
                    return i;
                }
                if ( d < distance) {
                    //System.err.println("Found distance '"+d+"' at '"+i+"'");
                    distance = d;
                    current = i;
                }
            }
            return current;
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            enableBasedOnSelection();
        }

        private void enableBasedOnSelection() {
            boolean selection = (fromList.getMinSelectionIndex()!=-1);
            if (this.isEnabled() != selection) {
                this.setEnabled(selection);
            }
        }

    }


    /********************************************************************************************
     * Action which wraps the actual rationalise action in the TermService. It
     * passes the selected values in both columns and then refreshes the JLists.
     ********************************************************************************************/
    class RationaliserAction extends AbstractAction implements ListSelectionListener {

        public RationaliserAction() {
            putValue(Action.NAME, "Rationalise Terms");
            ClassLoader classLoader = this.getClass().getClassLoader();   
            ImageIcon greenTickIcon = new ImageIcon(classLoader.getResource("green_tick.png")); 
            greenTickIcon = new ImageIcon(greenTickIcon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH)); //TODO: Investigate simpler way to resize an icon!
            
            putValue(Action.SMALL_ICON, greenTickIcon);
            fromList.addListSelectionListener(this);
            toList.addListSelectionListener(this);
            enableBasedOnSelection();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            
            List<Term> from = new ArrayList<Term>(); //Terms to be changed (from list)
            
            Object[] temp = fromList.getSelectedValues();
            for (Object o: temp){
                from.add((Term)o);
            }
         
            Term to = (Term) toList.getSelectedValue(); //Term (to list) to be rationalised into
            String text = textField.getText(); //Corrected name (if provided)
     
            /* 
             * Doing a little bit of input validation before sending the values to be rationalised.
             * In particular, we check here if the new text entered by the user already exists (but perhaps with
             * letters in different cases). In this case, the user is prompted with a box to choose if she
             * wants to force these changes across all the organisms (and thereby delete the old term) or cancel
             * her request to rationalise into this new term name. In a situation where the product names differ only
             * in the capitalisation, it's an 'all or nothing' approach.
             */
            
            boolean changeAllOrganisms = false;
        
            Term tempTerm = new Term(-1, text); //Temporary term to hold the text that the user entered in the textfield
 
            if(!text.equals(to.getName()) && terms.contains(tempTerm)){
                    
                    writeMessage("Term with similar name as the one entered already exists. Awaiting user input.");

                    int userDecision = JOptionPane.showConfirmDialog
                            (null, 
                            new String("There is already a term with the name '" + text + "' but perhaps with different capitalisation.\n" +      
                            "Do you want to delete the old term and make this change across *ALL* the organisms? "), 
                            "Term with similar name already exists",
                            JOptionPane.OK_CANCEL_OPTION, 
                            JOptionPane.WARNING_MESSAGE);

                    if(userDecision==JOptionPane.OK_OPTION){
                        changeAllOrganisms = true;
                        writeMessage("Making requested change across all organisms....");
                    }else if (userDecision==JOptionPane.CANCEL_OPTION){
                        writeMessage("Request to rationalise cancelled.");
                        return;
                    }
             }else{
              
                    logger.info("Brand new cv term needs to be created");
             }
            
            
            /* 
             * Having validated the input, the rationaliseTerm method can be called. The result of this process is then used
             * to update the JLists. The changes in the terms are made in the jlists rather than fetching all the
             * terms again from the database as this was too slow.
             */
          
            try{    
              
                RationaliserResult result = termService.rationaliseTerm(from, text, changeAllOrganisms, selectedTaxons);

                Set<Term> add = new HashSet<Term>(result.getTermsAdded()); //Terms that have been added (if any)
                Set<Term> remove = new HashSet<Term>(result.getTermsDeleted()); //Terms that have been deleted (if any)
                terms.removeAll(remove); //Always do a remove first so that duplicates are dealt with
                terms.addAll(add);  
                Collections.sort(terms);
                toList.addAll(terms);
                fromList.addAll(terms);

                if(add.size()!=0){ //Make the lists point to the added term
                    toList.setSelectedValue(add.iterator().next(), true);
                    fromList.setSelectedValue(add.iterator().next(), true);
                }
                writeMessage(result.getMessage());
                toList.repaint();
                fromList.repaint();
                
            }catch (Exception se){ //Any other unexpected errors
                writeMessage("There was an error while trying to rationalise. Try again or contact the Pathogens informatics team with details of what you tried to do.");
                writeMessage(se.toString());
                logger.debug(se.toString());      
            }
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            enableBasedOnSelection();
        }

        private void enableBasedOnSelection() {
            boolean selection = (fromList.getMinSelectionIndex()!=-1) && (toList.getMinSelectionIndex()!=-1);
            if (this.isEnabled() != selection) {
                this.setEnabled(selection);
            }
        }

    }
    
    
    /**
     * Class to generate the term names in the JList in a different colour if they have evidence codes
     * It also sets the ToolTipText to have the evidence codes
     * Added by NDS on 22.5.2009
     */
    class ColorRenderer extends DefaultListCellRenderer {
        Term current;
        
        /* Creates a new instance of ColorRenderer */
        public ColorRenderer() { }
          
        /* Sets the colour of the text to blue if the term has an evidence code **/
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
          super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
          if(value instanceof Term){
              current = (Term)value;
              List<String> tempevc = current.getEvidenceCodes();
              if(tempevc!=null && !tempevc.isEmpty()){
                  setForeground(Color.BLUE);
              }
          }
          return this;
        }
        
        /* When mouse hovers over the list item, the user will be able to see the evidence codes related to that product (via a ToolTip)*/
        public String getToolTipText(MouseEvent event){
            if(current!=null && (current.toString()).equals(super.getText())){ //Checking if we have a term and that it is the right one
                List<String> tempevc = current.getEvidenceCodes();
                if(tempevc!=null && !tempevc.isEmpty()){
                    String results = StringUtils.collectionToDelimitedString(tempevc, "/n");
                    return results;
                }
            }
            return new String();
        }
    }
          



    public void process(List<String> newArgs) {
        // TODO Auto-generated method stub

    }
    
    @Override
    public void setJogra(Jogra jogra) {
        this.jogra = jogra;
    }


}