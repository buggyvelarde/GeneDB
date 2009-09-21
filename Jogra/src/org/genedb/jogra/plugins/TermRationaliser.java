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
import java.net.URL;
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
    private static final String A_LONG_STRING = "This is the maximum product width we show";
    private String explicitURL = "http://developer.genedb.org/Jogra/resources/";
  
    /* Variables for rationaliser functionality */   
    private TermService termService;                                            //Is the interface to the SQLTermService
    private TaxonNodeManager taxonNodeManager;                                  //TaxonNodeManager to get the organism phylotree
    private List<TaxonNode> selectedTaxons = new ArrayList<TaxonNode>();          //Taxons corresponding to the selected organism names
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
    private JLabel scopeLabel = new JLabel("Organism(s): All organisms");             //Label showing user's selection. Default: All organisms
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
       
        
        if(selectedOrganismNames!=null && selectedOrganismNames.size()!=0 && !selectedOrganismNames.contains("root")){ // 'root' with a simple r causes problems
           scopeLabel.setText("Organism(s): " + StringUtils.collectionToCommaDelimitedString(selectedOrganismNames)); //Else, label will continue to have 'Scope: All organisms'
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
        fromList.setPrototypeCellValue(A_LONG_STRING);
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
        toList.setPrototypeCellValue(A_LONG_STRING);
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
       ClassLoader classLoader = this.getClass().getClassLoader();
       
       Box center = Box.createHorizontalBox(); //A box that displays contents from left to right
       center.add(Box.createHorizontalStrut(5)); //Invisible fixed-width component
  
       /*FROM LIST - Left hand side */
       Box leftPane = Box.createVerticalBox();
       leftPane.add(new JLabel("From"));

       JTextField fromSearchField = new JTextField(20);
       fromList.installJTextField(fromSearchField);
   
       leftPane.add(fromSearchField); 
       JScrollPane fromScrollPane = new JScrollPane(fromList);

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
       JScrollPane toScrollPane = new JScrollPane(toList);
       toScrollPane.setPreferredSize(new Dimension(500,400));
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
            ImageIcon greenTickIcon = new ImageIcon(classLoader.getResource("green_tick.jpg")); 
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
                            "Do you want to delete the exiting term and make this change across *ALL* the organisms? "), 
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
                writeMessage("There was an error while trying to rationalise. Try again or contact the informatics team.");
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
          


//    /*********************************************************************************
//     * Action which will set the position in the target list to the same position
//     *  as the selection in the source list. 14.9.2009: Previously this method 
//     *  synchronised on the index. However, as the lists may now only contain a subset
//     *  of the terms (after a search term has been entered), this has been modified
//     *  to synchronise on the actual term
//     **********************************************************************************/
//    public class SyncAction extends AbstractAction {
//
//        private JList sourceList;
//        private JList targetList;
//
//        public SyncAction(JList sourceList, JList targetList, KeyStroke keyStroke) {
//            this.targetList = targetList;
//            this.sourceList = sourceList;
//            putValue(NAME, "syncList");
//            putValue(ACCELERATOR_KEY, keyStroke);
//        }
//
//        public void actionPerformed(ActionEvent evt) {
//            Term term = (Term)sourceList.getSelectedValue();
//            targetList.setSelectedValue(term, true);
//            targetList.ensureIndexIsVisible(targetList.getSelectedIndex());
//        }
//
//    }
//
//    public static void list(InputMap map, KeyStroke[] keys) {
//        if (keys == null) {
//            return;
//        }
//        for (int i=0; i<keys.length; i++) {
//            // This method is defined in e859 Converting a KeyStroke to a String
//            String keystrokeStr =  keyStroke2String(keys[i]);
//
//            // Get the action name bound to this keystroke
//            while (map.get(keys[i]) == null) {
//                map = map.getParent();
//            }
//            if (map.get(keys[i]) instanceof String) {
//                String actionName = (String)map.get(keys[i]);
//                System.err.println(keystrokeStr+"        "+actionName);
//            } else {
//                Action action = (Action)map.get(keys[i]);
//                System.err.println(keystrokeStr+"        "+action);
//            }
//        }
//    }
//
//    public static String keyStroke2String(KeyStroke key) {
//        StringBuffer s = new StringBuffer(50);
//        int m = key.getModifiers();
//
//        if ((m & (InputEvent.SHIFT_DOWN_MASK|InputEvent.SHIFT_MASK)) != 0) {
//            s.append("shift ");
//        }
//        if ((m & (InputEvent.CTRL_DOWN_MASK|InputEvent.CTRL_MASK)) != 0) {
//            s.append("ctrl ");
//        }
//        if ((m & (InputEvent.META_DOWN_MASK|InputEvent.META_MASK)) != 0) {
//            s.append("meta ");
//        }
//        if ((m & (InputEvent.ALT_DOWN_MASK|InputEvent.ALT_MASK)) != 0) {
//            s.append("alt ");
//        }
//        if ((m & (InputEvent.BUTTON1_DOWN_MASK|InputEvent.BUTTON1_MASK)) != 0) {
//            s.append("button1 ");
//        }
//        if ((m & (InputEvent.BUTTON2_DOWN_MASK|InputEvent.BUTTON2_MASK)) != 0) {
//            s.append("button2 ");
//        }
//        if ((m & (InputEvent.BUTTON3_DOWN_MASK|InputEvent.BUTTON3_MASK)) != 0) {
//            s.append("button3 ");
//        }
//
//        switch (key.getKeyEventType()) {
//        case KeyEvent.KEY_TYPED:
//            s.append("typed ");
//            s.append(key.getKeyChar() + " ");
//            break;
//        case KeyEvent.KEY_PRESSED:
//            s.append("pressed ");
//            s.append(getKeyText(key.getKeyCode()) + " ");
//            break;
//        case KeyEvent.KEY_RELEASED:
//            s.append("released ");
//            s.append(getKeyText(key.getKeyCode()) + " ");
//            break;
//        default:
//            s.append("unknown-event-type ");
//            break;
//        }
//
//        return s.toString();
//    }
//
//    public static String getKeyText(int keyCode) {
//        if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9 ||
//            keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
//            return String.valueOf((char)keyCode);
//        }
//
//        switch(keyCode) {
//          case KeyEvent.VK_COMMA: return "COMMA";
//          case KeyEvent.VK_PERIOD: return "PERIOD";
//          case KeyEvent.VK_SLASH: return "SLASH";
//          case KeyEvent.VK_SEMICOLON: return "SEMICOLON";
//          case KeyEvent.VK_EQUALS: return "EQUALS";
//          case KeyEvent.VK_OPEN_BRACKET: return "OPEN_BRACKET";
//          case KeyEvent.VK_BACK_SLASH: return "BACK_SLASH";
//          case KeyEvent.VK_CLOSE_BRACKET: return "CLOSE_BRACKET";
//
//          case KeyEvent.VK_ENTER: return "ENTER";
//          case KeyEvent.VK_BACK_SPACE: return "BACK_SPACE";
//          case KeyEvent.VK_TAB: return "TAB";
//          case KeyEvent.VK_CANCEL: return "CANCEL";
//          case KeyEvent.VK_CLEAR: return "CLEAR";
//          case KeyEvent.VK_SHIFT: return "SHIFT";
//          case KeyEvent.VK_CONTROL: return "CONTROL";
//          case KeyEvent.VK_ALT: return "ALT";
//          case KeyEvent.VK_PAUSE: return "PAUSE";
//          case KeyEvent.VK_CAPS_LOCK: return "CAPS_LOCK";
//          case KeyEvent.VK_ESCAPE: return "ESCAPE";
//          case KeyEvent.VK_SPACE: return "SPACE";
//          case KeyEvent.VK_PAGE_UP: return "PAGE_UP";
//          case KeyEvent.VK_PAGE_DOWN: return "PAGE_DOWN";
//          case KeyEvent.VK_END: return "END";
//          case KeyEvent.VK_HOME: return "HOME";
//          case KeyEvent.VK_LEFT: return "LEFT";
//          case KeyEvent.VK_UP: return "UP";
//          case KeyEvent.VK_RIGHT: return "RIGHT";
//          case KeyEvent.VK_DOWN: return "DOWN";
//
//          // numpad numeric keys handled below
//          case KeyEvent.VK_MULTIPLY: return "MULTIPLY";
//          case KeyEvent.VK_ADD: return "ADD";
//          case KeyEvent.VK_SEPARATOR: return "SEPARATOR";
//          case KeyEvent.VK_SUBTRACT: return "SUBTRACT";
//          case KeyEvent.VK_DECIMAL: return "DECIMAL";
//          case KeyEvent.VK_DIVIDE: return "DIVIDE";
//          case KeyEvent.VK_DELETE: return "DELETE";
//          case KeyEvent.VK_NUM_LOCK: return "NUM_LOCK";
//          case KeyEvent.VK_SCROLL_LOCK: return "SCROLL_LOCK";
//
//          case KeyEvent.VK_F1: return "F1";
//          case KeyEvent.VK_F2: return "F2";
//          case KeyEvent.VK_F3: return "F3";
//          case KeyEvent.VK_F4: return "F4";
//          case KeyEvent.VK_F5: return "F5";
//          case KeyEvent.VK_F6: return "F6";
//          case KeyEvent.VK_F7: return "F7";
//          case KeyEvent.VK_F8: return "F8";
//          case KeyEvent.VK_F9: return "F9";
//          case KeyEvent.VK_F10: return "F10";
//          case KeyEvent.VK_F11: return "F11";
//          case KeyEvent.VK_F12: return "F12";
//          case KeyEvent.VK_F13: return "F13";
//          case KeyEvent.VK_F14: return "F14";
//          case KeyEvent.VK_F15: return "F15";
//          case KeyEvent.VK_F16: return "F16";
//          case KeyEvent.VK_F17: return "F17";
//          case KeyEvent.VK_F18: return "F18";
//          case KeyEvent.VK_F19: return "F19";
//          case KeyEvent.VK_F20: return "F20";
//          case KeyEvent.VK_F21: return "F21";
//          case KeyEvent.VK_F22: return "F22";
//          case KeyEvent.VK_F23: return "F23";
//          case KeyEvent.VK_F24: return "F24";
//
//          case KeyEvent.VK_PRINTSCREEN: return "PRINTSCREEN";
//          case KeyEvent.VK_INSERT: return "INSERT";
//          case KeyEvent.VK_HELP: return "HELP";
//          case KeyEvent.VK_META: return "META";
//          case KeyEvent.VK_BACK_QUOTE: return "BACK_QUOTE";
//          case KeyEvent.VK_QUOTE: return "QUOTE";
//
//          case KeyEvent.VK_KP_UP: return "KP_UP";
//          case KeyEvent.VK_KP_DOWN: return "KP_DOWN";
//          case KeyEvent.VK_KP_LEFT: return "KP_LEFT";
//          case KeyEvent.VK_KP_RIGHT: return "KP_RIGHT";
//
//          case KeyEvent.VK_DEAD_GRAVE: return "DEAD_GRAVE";
//          case KeyEvent.VK_DEAD_ACUTE: return "DEAD_ACUTE";
//          case KeyEvent.VK_DEAD_CIRCUMFLEX: return "DEAD_CIRCUMFLEX";
//          case KeyEvent.VK_DEAD_TILDE: return "DEAD_TILDE";
//          case KeyEvent.VK_DEAD_MACRON: return "DEAD_MACRON";
//          case KeyEvent.VK_DEAD_BREVE: return "DEAD_BREVE";
//          case KeyEvent.VK_DEAD_ABOVEDOT: return "DEAD_ABOVEDOT";
//          case KeyEvent.VK_DEAD_DIAERESIS: return "DEAD_DIAERESIS";
//          case KeyEvent.VK_DEAD_ABOVERING: return "DEAD_ABOVERING";
//          case KeyEvent.VK_DEAD_DOUBLEACUTE: return "DEAD_DOUBLEACUTE";
//          case KeyEvent.VK_DEAD_CARON: return "DEAD_CARON";
//          case KeyEvent.VK_DEAD_CEDILLA: return "DEAD_CEDILLA";
//          case KeyEvent.VK_DEAD_OGONEK: return "DEAD_OGONEK";
//          case KeyEvent.VK_DEAD_IOTA: return "DEAD_IOTA";
//          case KeyEvent.VK_DEAD_VOICED_SOUND: return "DEAD_VOICED_SOUND";
//          case KeyEvent.VK_DEAD_SEMIVOICED_SOUND: return "DEAD_SEMIVOICED_SOUND";
//
//          case KeyEvent.VK_AMPERSAND: return "AMPERSAND";
//          case KeyEvent.VK_ASTERISK: return "ASTERISK";
//          case KeyEvent.VK_QUOTEDBL: return "QUOTEDBL";
//          case KeyEvent.VK_LESS: return "LESS";
//          case KeyEvent.VK_GREATER: return "GREATER";
//          case KeyEvent.VK_BRACELEFT: return "BRACELEFT";
//          case KeyEvent.VK_BRACERIGHT: return "BRACERIGHT";
//          case KeyEvent.VK_AT: return "AT";
//          case KeyEvent.VK_COLON: return "COLON";
//          case KeyEvent.VK_CIRCUMFLEX: return "CIRCUMFLEX";
//          case KeyEvent.VK_DOLLAR: return "DOLLAR";
//          case KeyEvent.VK_EURO_SIGN: return "EURO_SIGN";
//          case KeyEvent.VK_EXCLAMATION_MARK: return "EXCLAMATION_MARK";
//          case KeyEvent.VK_INVERTED_EXCLAMATION_MARK:
//                   return "INVERTED_EXCLAMATION_MARK";
//          case KeyEvent.VK_LEFT_PARENTHESIS: return "LEFT_PARENTHESIS";
//          case KeyEvent.VK_NUMBER_SIGN: return "NUMBER_SIGN";
//          case KeyEvent.VK_MINUS: return "MINUS";
//          case KeyEvent.VK_PLUS: return "PLUS";
//          case KeyEvent.VK_RIGHT_PARENTHESIS: return "RIGHT_PARENTHESIS";
//          case KeyEvent.VK_UNDERSCORE: return "UNDERSCORE";
//
//          case KeyEvent.VK_FINAL: return "FINAL";
//          case KeyEvent.VK_CONVERT: return "CONVERT";
//          case KeyEvent.VK_NONCONVERT: return "NONCONVERT";
//          case KeyEvent.VK_ACCEPT: return "ACCEPT";
//          case KeyEvent.VK_MODECHANGE: return "MODECHANGE";
//          case KeyEvent.VK_KANA: return "KANA";
//          case KeyEvent.VK_KANJI: return "KANJI";
//          case KeyEvent.VK_ALPHANUMERIC: return "ALPHANUMERIC";
//          case KeyEvent.VK_KATAKANA: return "KATAKANA";
//          case KeyEvent.VK_HIRAGANA: return "HIRAGANA";
//          case KeyEvent.VK_FULL_WIDTH: return "FULL_WIDTH";
//          case KeyEvent.VK_HALF_WIDTH: return "HALF_WIDTH";
//          case KeyEvent.VK_ROMAN_CHARACTERS: return "ROMAN_CHARACTERS";
//          case KeyEvent.VK_ALL_CANDIDATES: return "ALL_CANDIDATES";
//          case KeyEvent.VK_PREVIOUS_CANDIDATE: return "PREVIOUS_CANDIDATE";
//          case KeyEvent.VK_CODE_INPUT: return "CODE_INPUT";
//          case KeyEvent.VK_JAPANESE_KATAKANA: return "JAPANESE_KATAKANA";
//          case KeyEvent.VK_JAPANESE_HIRAGANA: return "JAPANESE_HIRAGANA";
//          case KeyEvent.VK_JAPANESE_ROMAN: return "JAPANESE_ROMAN";
//          case KeyEvent.VK_KANA_LOCK: return "KANA_LOCK";
//          case KeyEvent.VK_INPUT_METHOD_ON_OFF: return "INPUT_METHOD_ON_OFF";
//
//          case KeyEvent.VK_AGAIN: return "AGAIN";
//          case KeyEvent.VK_UNDO: return "UNDO";
//          case KeyEvent.VK_COPY: return "COPY";
//          case KeyEvent.VK_PASTE: return "PASTE";
//          case KeyEvent.VK_CUT: return "CUT";
//          case KeyEvent.VK_FIND: return "FIND";
//          case KeyEvent.VK_PROPS: return "PROPS";
//          case KeyEvent.VK_STOP: return "STOP";
//
//          case KeyEvent.VK_COMPOSE: return "COMPOSE";
//          case KeyEvent.VK_ALT_GRAPH: return "ALT_GRAPH";
//        }
//
//        if (keyCode >= KeyEvent.VK_NUMPAD0 && keyCode <= KeyEvent.VK_NUMPAD9) {
//            char c = (char)(keyCode - KeyEvent.VK_NUMPAD0 + '0');
//            return "NUMPAD"+c;
//        }
//
//        return "unknown(0x" + Integer.toString(keyCode, 16) + ")";
//    }


    public void process(List<String> newArgs) {
        // TODO Auto-generated method stub

    }
    
    @Override
    public void setJogra(Jogra jogra) {
        this.jogra = jogra;
    }


}