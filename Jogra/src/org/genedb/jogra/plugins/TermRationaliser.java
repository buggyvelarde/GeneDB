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
import org.genedb.jogra.services.RationaliserJList;
import org.genedb.jogra.services.RationaliserResult;
import org.genedb.jogra.services.TermService;

import org.apache.log4j.Logger;
import org.bushe.swing.event.EventBus;
import org.springframework.util.StringUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
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


/********************************************************************************************************
 * The TermRationalier is a tool that allows curators to correct (rationalise) terms from a chosen 
 * controlled vocabulary. It is most useful for curators wanting to work on a specific organism or a set 
 * of organisms as these can be selected via the Organism tree in Jogra and they will be passed on to the  
 * rationaliser which will then only display the relevant terms. 
 ********************************************************************************************************/
public class TermRationaliser implements JograPlugin {
    
    private static final Logger logger = Logger.getLogger(TermRationaliser.class);

    /* Constants */
    private static final String WINDOW_TITLE = "Term Rationaliser";
  
    /* Variables for rationaliser functionality */   
    private TermService termService;                                            //Interface to the SQLTermService
    private TaxonNodeManager taxonNodeManager;                                  //TaxonNodeManager to get the organism phylotree
    private List<TaxonNode> selectedTaxons = new ArrayList<TaxonNode>();        //Taxons corresponding to the selected organism names
    private Jogra jogra;                                                        //Instance of Jogra
    private boolean showEVC;                                                    //Show Evidence codes?
    private String termType;                                                    //One of the CVs set in the Spring application context
    private List<Term> terms = new ArrayList<Term>();                           //Terms specific to selected taxons (for JList)
    private List<Term> allTerms = new ArrayList<Term>();                        //All the terms in the CV
    private HashMap<String, String> instances = new HashMap<String, String>();  //To hold the types of cvterms
    private String[] cvnames;                                                   //Holds the cv names passed in through Spring

    /*Variables related to the user interface */
    private JFrame frame = new JFrame(); 
    private RationaliserJList fromList = new RationaliserJList();
    private RationaliserJList toList = new RationaliserJList();
    private JTextArea textField;
    private JLabel productCountLabel = new JLabel();
    private JLabel scopeLabel = new JLabel();       //Label showing user's selection. Default: All organisms
    private JTextArea information = new JTextArea(10,10);   
    
    
    /**
     * Supplies the JPanel which is displayed in the main Jogra window.
     */
    public JPanel getMainWindowPlugin() {

        final JPanel ret = new JPanel();
        final JButton loadButton = new JButton("Load Term Rationaliser");
        final JLabel chooseType = new JLabel("Select term: ");
        final JComboBox termTypeBox = new JComboBox(instances.keySet().toArray());
        final JCheckBox showEVCFilter = new JCheckBox("Highlight terms with evidence codes", false);
        
        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {

                new SwingWorker<JFrame, Void>() {
                    @Override
                    protected JFrame doInBackground() throws Exception {
                        ret.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        setTermType(instances.get((String)termTypeBox.getSelectedItem()));
                        setShowEVC(showEVCFilter.isSelected());
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
        ret.add(verticalBox);
        return ret;
    }
    
    
    
    /**
     * Populates the JLists with data from the database (initialise models)
     */
    private void initModels() {
        
        JograProgressBar jpb = new JograProgressBar("Loading terms from database..."); //Progress bar added for better user information    
        this.setSelectedTaxonsAndScopeLabel(jogra.getSelectedOrganismNames());

        //trying to get these two threads to run simultaneously
        SwingWorker worker_1 = new SwingWorker<List<Term>, Void>() { //Loading can take a while; so in a worker thread
            @Override
            public List<Term> doInBackground() { 
                terms = termService.getTerms(getSelectedTaxons(), getTermType());
                Collections.sort(terms);
                return terms;
            }
            @Override
            public void done() { }
        };
        worker_1.run();
        
        SwingWorker worker_2 = new SwingWorker<List<Term>, Void>() {
            @Override
            public List<Term> doInBackground() { 
                allTerms = termService.getAllTerms(getTermType());
                Collections.sort(allTerms);
                return allTerms;
            }
            @Override
            public void done() { }
        };
        worker_2.run();
        
        //Is there a way to combine these two loops?
        for (Term term : terms) { 
            if(isShowEVC()){ //Fetch the evidence codes for the terms if the user wants them
                term.setEvidenceCodes(termService.getEvidenceCodes(term));    
            }
        }
        
        for (Term term : allTerms) { 
            if(isShowEVC()){ //Fetch the evidence codes for the terms if the user wants them
                term.setEvidenceCodes(termService.getEvidenceCodes(term));    
            }
        }

        fromList.addAll(terms);
        toList.addAll(allTerms);
        
        

        productCountLabel.setText(String.format("Number of terms for selected organisms: %d terms found (%s)", terms.size(), getTermType()));
        textField.setText(""); //Re-set the editable text box   
        fromList.clearSelection(); //Clear any previous selections
        toList.clearSelection(); 
        fromList.repaint();
        toList.repaint();
        
        jpb.stop();
    }
 
    
    

    /**
     * Return a new JFrame which is the main interface to the Rationaliser.
     */
    public JFrame getMainPanel() {
        
        /* JFRAME */  
        frame.setTitle(WINDOW_TITLE);
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        
        /* MENU */
        JMenuBar menuBar = new JMenuBar();
        
        JMenu actions_menu = new JMenu("Actions");
        JMenuItem actions_mitem_1 = new JMenuItem("Refresh lists");
        actions_mitem_1.addActionListener(
                new ActionListener(){
                    public void actionPerformed(ActionEvent actionEvent){
                       initModels();
                    }
                });
        actions_menu.add(actions_mitem_1);
        
        JMenu about_menu = new JMenu("About");
        JMenuItem about_mitem_1 = new JMenuItem("About");
        about_mitem_1.addActionListener(
                new ActionListener(){
                    public void actionPerformed(ActionEvent actionEvent){
                        JOptionPane.showMessageDialog( 
                          null, 
                          "Term Rationaliser \n" +
                          "Wellcome Trust Sanger Institute, UK \n" +
                          "2009",                          
                          "Term Rationaliser",
                          JOptionPane.PLAIN_MESSAGE
                        );
                    }
                });
        about_menu.add(about_mitem_1);

        menuBar.add(about_menu);
        menuBar.add(actions_menu);
        frame.add(menuBar, BorderLayout.NORTH);
        
        /* MAIN BOX */
        Box center = Box.createHorizontalBox(); //A box that displays contents from left to right
        center.add(Box.createHorizontalStrut(5)); //Invisible fixed-width component
        
        /* FROM LIST AND PANEL */
        fromList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); //Allow multiple products to be selected 
        fromList.addKeyListener(new KeyListener(){    
            @Override
            public void keyPressed(KeyEvent arg0) {
                if(arg0.getKeyCode()==KeyEvent.VK_RIGHT){
                     synchroniseLists(fromList, toList); //synchronise from right to left
                }
            }
            @Override
            public void keyReleased(KeyEvent arg0) {}
            @Override
            public void keyTyped(KeyEvent arg0) {}
        });
        
        Box fromPanel = this.createRationaliserPanel("From (selected terms)", fromList); //Box on left hand side
        fromPanel.add(Box.createVerticalStrut(55)); //Add some space
        center.add(fromPanel); //Add to main box
        center.add(Box.createHorizontalStrut(3)); //Add some space
        
        
        /* MIDDLE PANE */
        Box middlePane = Box.createVerticalBox();
   
        ClassLoader classLoader = this.getClass().getClassLoader(); //Needed to access the images later on
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
        
        center.add(middlePane); //Add middle pane to main box
        center.add(Box.createHorizontalStrut(3));
   
        /* TO LIST AND PANEL */
        toList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); //Single product selection in TO list
        toList.addKeyListener(new KeyListener(){    
            @Override
            public void keyPressed(KeyEvent arg0) {
                if(arg0.getKeyCode()==KeyEvent.VK_LEFT){
                     synchroniseLists(toList, fromList); //synchronise from right to left
                }
            }
            @Override
            public void keyReleased(KeyEvent arg0) {}
            @Override
            public void keyTyped(KeyEvent arg0) {}
        });
        
        Box toPanel = this.createRationaliserPanel("To (all terms)", toList);
        
        Box newTerm = Box.createVerticalBox();
        textField = new JTextArea(1,1); //textfield to let the user edit the name of an existing term
        textField.setForeground(Color.BLUE);
        JScrollPane jsp = new JScrollPane(textField); //scroll pane so that there is a horizontal scrollbar
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        newTerm.add(jsp);
        TitledBorder editBorder = BorderFactory.createTitledBorder("Edit term name");
        editBorder.setTitleColor(Color.DARK_GRAY);
        newTerm.setBorder(editBorder);
        toPanel.add(newTerm); //add textfield to panel
       
        center.add(toPanel); //add panel to main box
        center.add(Box.createHorizontalStrut(5));
        
        frame.add(center); //add the main panel to the frame
     
        initModels(); //load the lists with data

        
       /* BOTTOM HALF OF FRAME */
       Box main = Box.createVerticalBox();
       TitledBorder border = BorderFactory.createTitledBorder("Information");
       border.setTitleColor(Color.DARK_GRAY);

       /* INFORMATION BOX */
       Box info = Box.createVerticalBox();
       
       Box scope = Box.createHorizontalBox();
       scope.add(Box.createHorizontalStrut(5));
       scope.add(scopeLabel); //label showing the scope of the terms
       scope.add(Box.createHorizontalGlue());

       Box productCount = Box.createHorizontalBox(); 
       productCount.add(Box.createHorizontalStrut(5));  
       productCount.add(productCountLabel); //display the label showing the number of terms
       productCount.add(Box.createHorizontalGlue());

       info.add(scope);
       info.add(productCount);
       info.setBorder(border);

       /* ACTION BUTTONS */
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

       /* MORE INFORMATION TOGGLE */
       Box buttonBox = Box.createHorizontalBox();
       final JButton toggle = new JButton("Hide information <<");

       buttonBox.add(Box.createHorizontalStrut(5));
       buttonBox.add(toggle);
       buttonBox.add(Box.createHorizontalGlue());

       Box textBox = Box.createHorizontalBox();

       final JScrollPane scrollPane = new JScrollPane(information);
       scrollPane.setPreferredSize(new Dimension(frame.getWidth(),100));
       scrollPane.setVisible(true);
       textBox.add(Box.createHorizontalStrut(5));
       textBox.add(scrollPane); 

       ActionListener actionListener = new ActionListener(){
           public void actionPerformed(ActionEvent actionEvent){
               if(toggle.getText().equals("Show information >>")){
                   scrollPane.setVisible(true);
                   toggle.setText("Hide information <<");
                   frame.setPreferredSize(new Dimension(frame.getWidth(),frame.getHeight()+100));
                   frame.pack();
               }else if(toggle.getText().equals("Hide information <<")){
                   scrollPane.setVisible(false);
                   toggle.setText("Show information >>");
                   frame.setPreferredSize(new Dimension(frame.getWidth(),frame.getHeight()-100));
                   frame.pack();
               }
           }
       };
       toggle.addActionListener(actionListener);

       main.add(Box.createVerticalStrut(5));
       main.add(info);
       main.add(Box.createVerticalStrut(5));
       main.add(Box.createVerticalStrut(5));
       main.add(actionButtons);
       main.add(Box.createVerticalStrut(10));
       main.add(buttonBox);
       main.add(textBox);

       frame.add(main, BorderLayout.SOUTH);
       frame.pack();

       return frame;
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

   
   private void writeMessage(String m){
        this.information.setText(information.getText().concat(m).concat("\n"));
   }
   
   
   private void setSelectedTaxonsAndScopeLabel(List<String> organismNames){
       if(this.selectedTaxons!=null && this.selectedTaxons.size()>0){
           this.selectedTaxons.clear(); //clear anything that already is in the list
       }
      
       if(organismNames!=null && organismNames.size()!=0 && !organismNames.contains("root")){ // 'root' with a simple r causes problems 
           scopeLabel.setText("Organism(s): " + StringUtils.collectionToCommaDelimitedString(organismNames)); 
           for(String s: organismNames){
               this.selectedTaxons.add(taxonNodeManager.getTaxonNodeForLabel(s));
           }
        }else{ //If there are no selections, get all terms
            scopeLabel.setText("Organism(s): All organisms");
            this.selectedTaxons.add(taxonNodeManager.getTaxonNodeForLabel("Root"));
        }
   }
   


   private Box createRationaliserPanel(final String name, final RationaliserJList rjlist){
       /* Since both the TO and FROM panels are so similar, we put all the common
        * drawing tasks inside the following method.  */
       
       int preferredHeight = 500; //change accordingly
       int preferredWidth  = 400; 

       Box box = Box.createVerticalBox();
       box.add(new JLabel(name));

       JTextField searchField = new JTextField(20); //Search field on top
       rjlist.installJTextField(searchField);
       box.add(searchField);

       JScrollPane scrollPane = new JScrollPane(); //scroll pane
       scrollPane.setViewportView(rjlist);
       scrollPane.setPreferredSize(new Dimension(preferredHeight,preferredWidth));
       box.add(scrollPane);

       TitledBorder sysidBorder = BorderFactory.createTitledBorder("Systematic IDs"); //systematic ID box
       sysidBorder.setTitleColor(Color.DARK_GRAY);
       
//       final JTextField idField = new JTextField(20);
//       idField.setEditable(false);
//       idField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
//       idField.setForeground(Color.DARK_GRAY);
       
       final JTextArea idField = new JTextArea(1,1);
       idField.setEditable(false);
       idField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
       idField.setForeground(Color.DARK_GRAY);
       JScrollPane scroll = new JScrollPane(idField);
       scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
              
       Box sysidBox = Box.createVerticalBox();
       sysidBox.add(scroll /*idField*/);
       sysidBox.setBorder(sysidBorder);
       box.add(sysidBox);

       rjlist.addListSelectionListener(new ListSelectionListener() {
           @Override
           public void valueChanged(ListSelectionEvent e) {
               Term highlightedTerm = (Term) rjlist.getSelectedValue();  
               if(highlightedTerm!=null){
                   //Set the sys id box to show the sys id
                   idField.setText(StringUtils.collectionToCommaDelimitedString(termService.getSystematicIDs(highlightedTerm, selectedTaxons)));
                   if(name.equalsIgnoreCase("To (all terms)")){
                       textField.setText(highlightedTerm.getName()); //Allow the user to edit the spelling of the term in the to list
                   }
               }
           }
       });

       return box;

   }

   
   
    /**
     *  SETTER/GETTER METHODS
     */  
    
   public JFrame getFrame(){
       return frame;
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
            
            int current = -1;
            int distance = Integer.MAX_VALUE;
            for (int i = 0; i < list.getSize(); i++) {
                if (i == fromIndex) {
                    continue;
                }
                String element = ((Term)list.getElementAt(i)).getName();
                if (in.equalsIgnoreCase(element)) {
                    return i;
                }
                int d = org.apache.commons.lang.StringUtils.getLevenshteinDistance(in, element);
                
                if (d==1) {
                    return i;
                }
                if ( d < distance) {
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
     * Action which wraps the rationalise action in the TermService.
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
            
            List<Term> from = new ArrayList<Term>(); 
            Object[] temp = fromList.getSelectedValues(); //terms selected on the left
            for (Object o: temp){ //TODO:is there an easier way to convert an array to a list of **typed** objects?
                from.add((Term)o);
            }  
            
           
            Term to = (Term) toList.getSelectedValue(); //Term selected on the right
            String text = textField.getText(); //Corrected name (if provided) in the text box
     
            /* 
             * Doing a bit of input validation before sending the values to be rationalised.
             */
            
            boolean changeAllOrganisms = false;
            
            if(toList.contains(text)==1){ //User chose to rationalise to a term from the L.H.S. list

                int userDecision = this.show_OK_Cancel_Dialogue("You are about to rationalise the following terms:\n" + 
                                                                StringUtils.collectionToDelimitedString(from, "\n") + "\n\n" +
                                                                "to: \n" +
                                                                text + "\n\n" +
                                                                "Do you want to continue? ", 
                                                                "Confirm");     
                if (userDecision==JOptionPane.CANCEL_OPTION){
                    writeMessage("Request to rationalise cancelled.");
                    return;
                }

            
            }else if(toList.contains(text)==0) { //So, user has decided to rationalise to a new term that is currently not in the cv
                
                int userDecision = this.show_OK_Cancel_Dialogue("You are about to rationalise the following terms:\n" + 
                                                                StringUtils.collectionToDelimitedString(from, "\n") + "\n\n" +
                                                                "to a new term: \n" +
                                                                text + "\n\n" +
                                                                "Do you want to continue? ", 
                                                                "Confirm");     
                if (userDecision==JOptionPane.CANCEL_OPTION){
                    writeMessage("Request to rationalise cancelled.");
                    return;
                }
 
            }else if(toList.contains(text)==2){ //So, the user is just changing the case of the terms
                
                int userDecision = this.show_OK_Cancel_Dialogue("You are about to change the case of this term:\n" + 
                                                                 text + "\n\n" +
                                                                " and this will be done across ALL the organisms. " +                                                               
                                                                "Do you want to continue? ", 
                                                                "Confirm");     
                if (userDecision==JOptionPane.CANCEL_OPTION){
                    writeMessage("Request to rationalise cancelled.");
                    return;
                }
                
                changeAllOrganisms = true;
                             
            }
        
                
   
            
            /* 
             * Having validated the input, the rationaliseTerm method can be called. The result of this process is then used
             * to update the JLists. The changes in the terms are made in the jlists rather than fetching *all* the
             * terms again from the database as this was too slow.
             * The user can opt to fetch the terms from the database by selecting 'refresh models' which will call
             * the init method
             */
          
            try{    
                
                getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                
                writeMessage("Rationalising...");
                
                RationaliserResult result = termService.rationaliseTerm(from, text, changeAllOrganisms, selectedTaxons);

                //Left list (specific)
                terms.removeAll(result.getTermsDeletedSpecific()); //Always do a remove first so that duplicates are dealt with
                terms.addAll(result.getTermsAddedSpecific());  
                //Right list (general)
                allTerms.removeAll(result.getTermsDeletedGeneral());
                allTerms.removeAll(result.getTermsAddedGeneral());
                
                //Can we try to get rid of this as this sort can be slow?
                Collections.sort(terms);
                Collections.sort(allTerms);
 
                fromList.addAll(terms);
                toList.addAll(allTerms);

                if(result.getTermsAddedSpecific().size()!=0){ //Make the lists point to the added term
                    toList.setSelectedValue(result.getTermsAddedSpecific().iterator().next(), true);
                    fromList.setSelectedValue(result.getTermsAddedSpecific().iterator().next(), true);
                }
                writeMessage(result.getMessage());
                //Draw the lists again
                toList.repaint();
                fromList.repaint();
                
            }catch (Exception se){ //Any other unexpected errors
                writeMessage("There was an error while trying to rationalise. " +
                	     "Try again or contact the WTSI Pathogens Informatics team with details of what you tried to do.");
                writeMessage(se.toString());
                logger.debug(se.toString());      
            }
            
            getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
        
        private int show_OK_Cancel_Dialogue(String message, String title){
            return 
            JOptionPane.showConfirmDialog
            ( null, 
              message, 
              title,
              JOptionPane.OK_CANCEL_OPTION, 
              JOptionPane.WARNING_MESSAGE
            );
            
        }

    }
    


    public void process(List<String> newArgs) {
        // TODO Auto-generated method stub

    }
    
    @Override
    public void setJogra(Jogra jogra) {
        this.jogra = jogra;
    }

    public String[] getCvnames() {
        return cvnames;
    }

    public void setCvnames(String[] cvnames) {
        this.cvnames = cvnames;
        for(String c: cvnames){
            String[] splitparts = StringUtils.split(c, ",");
            instances.put(splitparts[1], splitparts[0]);
         }
    }
    
   
    
   
    
    }
    


