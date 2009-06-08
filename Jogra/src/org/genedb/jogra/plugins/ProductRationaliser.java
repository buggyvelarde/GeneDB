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

import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeManager;
import org.genedb.jogra.domain.GeneDBMessage;
import org.genedb.jogra.domain.Product;
import org.genedb.jogra.drawing.Jogra;
import org.genedb.jogra.drawing.JograPlugin;
import org.genedb.jogra.drawing.OpenWindowEvent;
import org.genedb.jogra.services.MethodResult;
import org.genedb.jogra.services.ProductService;

import org.apache.log4j.Logger;
import org.bushe.swing.event.EventBus;
import org.springframework.util.StringUtils;

import skt.swing.SwingUtil;
import skt.swing.search.IncrementalSearchKeyListener;
import skt.swing.search.ListFindAction;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.BorderFactory;


/**
 * A plug-in which curators can use to display a list of products, in order to merge them. eg
 * To identify 3 products which mean the same thing, but are expressed or spelt differently
 * and then change all uses to a single product.
 *
 */
public class ProductRationaliser implements JograPlugin {

    private static final String WINDOW_TITLE = "Product Rationaliser";
    private static final String A_LONG_STRING = "This is the maximum product width we show";
    private static final Logger logger = Logger.getLogger(ProductRationaliser.class);

    private ProductService productService;
    private TaxonNodeManager taxonNodeManager; //Added in order to get corresponding taxonNodes
    private List<TaxonNode> taxonList = new ArrayList<TaxonNode>();
    private List<String> userSelection; //Organisms or class of organisms selected by user
    private Jogra jogra; //Jogra object in this application context
    private boolean showEVC; //Show Evidence codes?
    private boolean showSysID; //Show systematic IDs?
    private HashMap<Integer, List<String>> idMap = new HashMap<Integer, List<String>>(); // Key = cvtermid, Value = List of systematic IDs
    private HashMap<Integer, List<String>> evcMap = new HashMap<Integer, List<String>>(); // Key = cvtermid, Value = List of evidence codes
    private List<Product> products = new ArrayList<Product>(); //All products
    private Product[] productArray;
    
    /*Variables related to the user interface */
    private JList fromList;
    private JList toList;
    private JTextField textField;
    private JTextField idField;
    private JLabel productCountLabel;
    private JLabel scopeLabel = new JLabel("Scope: All organisms"); //Label showing user's selection. Default: View all
    private JTextArea information = new JTextArea(10,10);
    
    
   /* Essential setter method */
    public void setTaxonNodeManager(TaxonNodeManager taxonNodeManager) {
        this.taxonNodeManager = taxonNodeManager;
    }
    
 

    /**
     * Fetch the product list from the database and set that as the model for both the toList and fromList
     */
    private void initModels() {
       
        userSelection = jogra.getChosenOrganism();  
        taxonList.clear();
        logger.info("Product Rationaliser: Inside initModels() method. Getting selection from Jogra " + userSelection);
        if(userSelection!=null && userSelection.size()!=0 && !userSelection.contains("root")){ // 'root' with a simple r causes problems
           scopeLabel.setText("Scope: " + StringUtils.collectionToCommaDelimitedString(userSelection)); //Else, label will continue to have 'Scope: All organisms'
           for(String s: userSelection){
               taxonList.add(taxonNodeManager.getTaxonNodeForLabel(s));
           }
        }else{ //If there are no selections, get all products
            taxonList.add(taxonNodeManager.getTaxonNodeForLabel("Root"));
        }
      
        products = productService.getProductList(taxonList); 
        productArray = new Product[products.size()];
      
        int i=0;
        for (Product product : products) {
            
            if(isShowSysID()){ /* Get systematic IDs for this product if the relevant checkbox has been ticked */
               idMap.put(product.getId(), (List<String>)productService.getSystematicIDs(product));
            }
            if(isShowEVC()){ /* Get evidence codes for this product if the relevant checkbox has been ticked */
               evcMap.put(product.getId(),productService.getEvidenceCodes(product));  
            }
            productArray[i] = product;
            i++;
        }
        fromList.setListData(productArray);
        toList.setListData(productArray);
        productCountLabel.setText(products.size()+" Products");
        //Re-set other textboxes
        if(isShowSysID()){
            idField.setText("");
        }else{
            idField.setText("(not enabled)");
            idField.setEnabled(false);
        }
        textField.setText("");
          
    }


    /**
     * Return a new, initialised JFrame which is the main interface.
     *
     * @return the main interface
     */
    public JFrame getMainPanel() {
        fromList = new JList();
        toList = new JList();
        
        if(isShowEVC()){ //If user has requested to view evidence codes, then use different renderer so that items with evidence codes are displayed in different colour
            ColorRenderer cr = new ColorRenderer();
            fromList.setCellRenderer(cr);
            toList.setCellRenderer(cr);
        }
    
        productCountLabel = new JLabel("No. of products");
        textField = new JTextField(20);
        textField.setForeground(Color.BLUE);
        
        idField = new JTextField(20);
        idField.setEditable(false);
        idField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        idField.setForeground(Color.DARK_GRAY);
              
        fromList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); //Allow multiple products to be selected 
        fromList.setPrototypeCellValue(A_LONG_STRING);
        ListFindAction findAction = new ListFindAction(true);
        SyncAction fromSync = new SyncAction(fromList, toList, KeyStroke.getKeyStroke("RIGHT"));
        SwingUtil.installActions(fromList, new Action[]{
            fromSync,
            findAction,
            new ListFindAction(false)
        });
        fromList.addKeyListener(new IncrementalSearchKeyListener(findAction));

        toList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); //Single product selection in TO list
        toList.setPrototypeCellValue(A_LONG_STRING);
        SyncAction toSync = new SyncAction(toList, fromList, KeyStroke.getKeyStroke("LEFT"));
        SwingUtil.installActions(toList, new Action[]{
                toSync,
                findAction,
                new ListFindAction(false)
            });
        
        toList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(toList.getSelectedValue()!=null){
                    textField.setText(((Product)toList.getSelectedValue()).toString());
                }
                if(isShowSysID()){
                   idField.setText(StringUtils.collectionToCommaDelimitedString(idMap.get(((Product)toList.getSelectedValue()).getId())));
                }
             }
        });
       toList.addKeyListener(new IncrementalSearchKeyListener(findAction));
       
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
        Box center = Box.createHorizontalBox(); //A box that displays contents from left to right
        center.add(Box.createHorizontalStrut(5)); //Invisible fixed-width component
        
        Box leftPane = Box.createVerticalBox();
        leftPane.add(new JLabel("From"));
        leftPane.add(new JScrollPane(fromList));
        center.add(leftPane);
        center.add(Box.createHorizontalStrut(3));
        
        Box rightPane = Box.createVerticalBox();
        rightPane.add(new JLabel("To"));
        rightPane.add(new JScrollPane(toList));
        center.add(rightPane);
        center.add(Box.createHorizontalStrut(5));

        ret.add(center, BorderLayout.CENTER);
        
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
        
        Box sysIDBox = Box.createHorizontalBox();
        sysIDBox.add(Box.createHorizontalStrut(5));
        JLabel label2 = new JLabel("Systematic IDs");
        sysIDBox.add(label2);
        sysIDBox.add(Box.createHorizontalStrut(5));
        sysIDBox.add(idField);
        sysIDBox.add(Box.createHorizontalGlue());
                
        info.add(scope);
        info.add(productCount);
        info.add(sysIDBox);
        info.setBorder(border);
        
        /* Add a box to edit the name of a product */
        Box newTerm = Box.createHorizontalBox();
        newTerm.add(Box.createHorizontalStrut(5));
        JLabel label1 = new JLabel("Edit");
        newTerm.add(label1);
        newTerm.add(textField);
        newTerm.add(Box.createHorizontalGlue());
        border = BorderFactory.createTitledBorder("Rationalise to a new term");
        border.setTitleColor(Color.DARK_GRAY);
        newTerm.setBorder(border);
        
        /* Action buttons */
        Box actionButtons = Box.createHorizontalBox();
        actionButtons.add(Box.createHorizontalGlue());
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ret.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                initModels();//This refreshes lists from the database
                ret.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });
        actionButtons.add(refresh);
        actionButtons.add(Box.createHorizontalStrut(10));
              
        JButton speeling = new JButton(new FindClosestMatchAction());
        actionButtons.add(speeling);
        actionButtons.add(Box.createHorizontalStrut(10));

        RationaliserAction ra = new RationaliserAction();
        JButton go = new JButton(ra);
        actionButtons.add(go);
        actionButtons.add(Box.createHorizontalGlue());
        
        /* Show more information toggle */
        Box buttonBox = Box.createHorizontalBox();
        final JButton toggle = new JButton("Show information >>");
      
        buttonBox.add(Box.createHorizontalStrut(5));
        buttonBox.add(toggle);
        buttonBox.add(Box.createHorizontalGlue());
               
        Box textBox = Box.createHorizontalBox();
       
        final JScrollPane scrollPane = new JScrollPane(information);
        scrollPane.setPreferredSize(new Dimension(800,100));
        scrollPane.setVisible(false);
        textBox.add(Box.createHorizontalStrut(5));
        textBox.add(scrollPane); 
      
        ActionListener actionListener = new ActionListener(){
            public void actionPerformed(ActionEvent actionEvent){
                if(toggle.getText().equals("Show information >>")){
                    scrollPane.setVisible(true);
                    toggle.setText("Hide information <<");
                    ret.setPreferredSize(new Dimension(800,900));
                    ret.pack();
                }else if(toggle.getText().equals("Hide information <<")){
                    scrollPane.setVisible(false);
                    toggle.setText("Show information >>");
                    ret.setPreferredSize(new Dimension(800,800));
                    ret.pack();
                }
            }
        };
        toggle.addActionListener(actionListener);
        
        main.add(Box.createVerticalStrut(5));
        main.add(info);
        main.add(Box.createVerticalStrut(5));
        main.add(newTerm);
        main.add(Box.createVerticalStrut(5));
        main.add(actionButtons);
        main.add(Box.createVerticalStrut(10));
        main.add(buttonBox);
        main.add(textBox);
        
       /* JLabel hints = new JLabel("<html><i><ol>"+
                "<li>Use the left or right arrow, as appropriate, to sync the selection" +
                "<li>Use CTRL+i to start an incremental search, then the UP/DOWN arrows" +
                "</ol></i></html>");
        */
        ret.add(main, BorderLayout.SOUTH);
        ret.setPreferredSize(new Dimension(800,800));
        ret.pack();
     
        return ret;
    }
    
    
    
    /**
     * Supply a JPanel which will be displayed in the main Jogra window
     */

    public JPanel getMainWindowPlugin() {
        final JPanel ret = new JPanel();
        final JButton loadButton = new JButton("Load Product Rationaliser");
        final JCheckBox showEVCFilter = new JCheckBox("Highlight products with evidence codes", false);
        final JCheckBox showSysIDFilter = new JCheckBox("Retrieve systematic IDs for products", false);
        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                System.err.println("Am I on EDT '" + EventQueue.isDispatchThread() + "' 1");
                new SwingWorker<JFrame, Void>() {

                    @Override
                    protected JFrame doInBackground() throws Exception {
                        ret.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        setShowEVC(showEVCFilter.isSelected());
                        setShowSysID(showSysIDFilter.isSelected());
                        return makeWindow();
                    }

                    @Override
                    public void done() {
                        try {
                            final GeneDBMessage e = new OpenWindowEvent(ProductRationaliser.this, get());
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
        verticalBox.add(loadButton);
        verticalBox.add(showEVCFilter);
        verticalBox.add(showSysIDFilter);
           
        ret.add(verticalBox);
        return ret;
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

    JFrame makeWindow() {
        System.err.println("Am I on EDT '" + EventQueue.isDispatchThread() + "'  x");
       /* JFrame lookup = Jogra.findNamedWindow(WINDOW_TITLE);
        if (lookup == null) {
            lookup = getMainPanel(); 
        } */
        JFrame lookup = getMainPanel(); //Always getting a new frame since it has to pick up variable organism (improve efficiency later: NDS)
        return lookup;
    }

    public void setProductService(ProductService productService) {
        this.productService = productService;
    }


    /**
     * An action wrapping code which identifies the closest match in the right hand column
     * to the selected value in the left hand column. Closest is defined by the smallest
     * Levenshtein value.
     */
    class FindClosestMatchAction extends AbstractAction implements ListSelectionListener {

        public FindClosestMatchAction() {
            putValue(Action.NAME, "Find possible fix");
            fromList.addListSelectionListener(this);
            enableBasedOnSelection();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            
            Product from = (Product) fromList.getSelectedValue();

            int match = findClosestMatch(from.toString(), fromList.getSelectedIndex(), toList.getModel());
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
                String element = ((Product)list.getElementAt(i)).toString();
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


    /**
     * Action which wraps the actual synchronize action in the ProductService. It
     * passes the selected values in both columns and then refreshes the model.
     */
    class RationaliserAction extends AbstractAction implements ListSelectionListener {

        public RationaliserAction() {
            putValue(Action.NAME, "Rationalise Products");
            fromList.addListSelectionListener(this);
            toList.addListSelectionListener(this);
            enableBasedOnSelection();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
          
            Product to = (Product) toList.getSelectedValue(); //Product (from list) to be rationalised into
            String text = textField.getText(); //Corrected name (if provided)
            Object[] from = fromList.getSelectedValues(); // Products to be rationalised
            List<Product> old = new ArrayList<Product>();
            for (Object o : from) {
                old.add((Product)o); 
            }
            MethodResult result = productService.rationaliseProduct(to, old, text);
            if(result.isSuccessful()){
                System.out.println("Message: " + result.getSuccessMsg() );
                List<Product> add = productService.getProductsToAdd();
                List<Product> remove = productService.getProductsToRemove();
    
                if(add!=null){
                    products.addAll(add);
                }
                if(remove!=null){
                    products.removeAll(remove);
                }
            
                
                toList.setListData(products.toArray()); //TO DO: Try to sort this array first
                fromList.setListData(products.toArray());
                information.setText(information.getText().concat(result.getSuccessMsg()));
          
                toList.repaint();
                fromList.repaint();
             
             
            }else{
                
                information.setText("There was an error while trying to rationalise the selected products.");
                logger.debug("Cannot rationalise");
                
            }
           
           
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            enableBasedOnSelection();
        }

        private void enableBasedOnSelection() {
            boolean selection = (fromList.getMinSelectionIndex()!=-1)
                && (toList.getMinSelectionIndex()!=-1);
            if (this.isEnabled() != selection) {
                this.setEnabled(selection);
            }
        }

    }
    
    
    /**
     * Class to generate the product names in the JList in a different colour if they have evidence codes
     * It also sets the ToolTipText to have the evidence codes
     * Added by NDS on 22.5.2009
     */
    class ColorRenderer extends DefaultListCellRenderer {
        Product current;
        
        /** Creates a new instance of ColorRenderer */
        public ColorRenderer() { }
          
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

          super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
          /* The value here will be a product. If this product have evidence codes, set the text to green */
          if(value instanceof Product){
              current = (Product)value;
              List<String> tempevc = evcMap.get(current.getId());
              if(tempevc!=null && !tempevc.isEmpty()){
                  setForeground(Color.BLUE);

              }
              
          }
        
          return this;
        }
        
        /* When mouse hovers over the list item, the user will be able to see the evidence codes related to that product (via a ToolTip)*/
        public String getToolTipText(MouseEvent event){
            
            if(current!=null && (current.toString()).equals(super.getText())){ //Checking if we have a product and that it is the right one
                List<String> tempevc = evcMap.get(current.getId());
                if(tempevc!=null && !tempevc.isEmpty()){
                    String results = StringUtils.collectionToDelimitedString(tempevc, "/n");
                    System.out.println(results);
                    return results;

                }
            }
            return new String();
        }
    }
          


    /**
     * Action which will set the position in the target list to the same position
     *  as the selection in the source list.
     */
    public class SyncAction extends AbstractAction {

        private JList sourceList;
        private JList targetList;

        public SyncAction(JList sourceList, JList targetList, KeyStroke keyStroke) {
            this.targetList = targetList;
            this.sourceList = sourceList;
            putValue(NAME, "syncList");
            putValue(ACCELERATOR_KEY, keyStroke);
        }

        public void actionPerformed(ActionEvent evt) {
            System.err.println("key listener called");
            int index = sourceList.getSelectedIndex();
            targetList.setSelectedIndex(index);
            targetList.ensureIndexIsVisible(index);
        }

    }

    public static void list(InputMap map, KeyStroke[] keys) {
        if (keys == null) {
            return;
        }
        for (int i=0; i<keys.length; i++) {
            // This method is defined in e859 Converting a KeyStroke to a String
            String keystrokeStr =  keyStroke2String(keys[i]);

            // Get the action name bound to this keystroke
            while (map.get(keys[i]) == null) {
                map = map.getParent();
            }
            if (map.get(keys[i]) instanceof String) {
                String actionName = (String)map.get(keys[i]);
                System.err.println(keystrokeStr+"        "+actionName);
            } else {
                Action action = (Action)map.get(keys[i]);
                System.err.println(keystrokeStr+"        "+action);
            }
        }
    }

    public static String keyStroke2String(KeyStroke key) {
        StringBuffer s = new StringBuffer(50);
        int m = key.getModifiers();

        if ((m & (InputEvent.SHIFT_DOWN_MASK|InputEvent.SHIFT_MASK)) != 0) {
            s.append("shift ");
        }
        if ((m & (InputEvent.CTRL_DOWN_MASK|InputEvent.CTRL_MASK)) != 0) {
            s.append("ctrl ");
        }
        if ((m & (InputEvent.META_DOWN_MASK|InputEvent.META_MASK)) != 0) {
            s.append("meta ");
        }
        if ((m & (InputEvent.ALT_DOWN_MASK|InputEvent.ALT_MASK)) != 0) {
            s.append("alt ");
        }
        if ((m & (InputEvent.BUTTON1_DOWN_MASK|InputEvent.BUTTON1_MASK)) != 0) {
            s.append("button1 ");
        }
        if ((m & (InputEvent.BUTTON2_DOWN_MASK|InputEvent.BUTTON2_MASK)) != 0) {
            s.append("button2 ");
        }
        if ((m & (InputEvent.BUTTON3_DOWN_MASK|InputEvent.BUTTON3_MASK)) != 0) {
            s.append("button3 ");
        }

        switch (key.getKeyEventType()) {
        case KeyEvent.KEY_TYPED:
            s.append("typed ");
            s.append(key.getKeyChar() + " ");
            break;
        case KeyEvent.KEY_PRESSED:
            s.append("pressed ");
            s.append(getKeyText(key.getKeyCode()) + " ");
            break;
        case KeyEvent.KEY_RELEASED:
            s.append("released ");
            s.append(getKeyText(key.getKeyCode()) + " ");
            break;
        default:
            s.append("unknown-event-type ");
            break;
        }

        return s.toString();
    }

    public static String getKeyText(int keyCode) {
        if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9 ||
            keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
            return String.valueOf((char)keyCode);
        }

        switch(keyCode) {
          case KeyEvent.VK_COMMA: return "COMMA";
          case KeyEvent.VK_PERIOD: return "PERIOD";
          case KeyEvent.VK_SLASH: return "SLASH";
          case KeyEvent.VK_SEMICOLON: return "SEMICOLON";
          case KeyEvent.VK_EQUALS: return "EQUALS";
          case KeyEvent.VK_OPEN_BRACKET: return "OPEN_BRACKET";
          case KeyEvent.VK_BACK_SLASH: return "BACK_SLASH";
          case KeyEvent.VK_CLOSE_BRACKET: return "CLOSE_BRACKET";

          case KeyEvent.VK_ENTER: return "ENTER";
          case KeyEvent.VK_BACK_SPACE: return "BACK_SPACE";
          case KeyEvent.VK_TAB: return "TAB";
          case KeyEvent.VK_CANCEL: return "CANCEL";
          case KeyEvent.VK_CLEAR: return "CLEAR";
          case KeyEvent.VK_SHIFT: return "SHIFT";
          case KeyEvent.VK_CONTROL: return "CONTROL";
          case KeyEvent.VK_ALT: return "ALT";
          case KeyEvent.VK_PAUSE: return "PAUSE";
          case KeyEvent.VK_CAPS_LOCK: return "CAPS_LOCK";
          case KeyEvent.VK_ESCAPE: return "ESCAPE";
          case KeyEvent.VK_SPACE: return "SPACE";
          case KeyEvent.VK_PAGE_UP: return "PAGE_UP";
          case KeyEvent.VK_PAGE_DOWN: return "PAGE_DOWN";
          case KeyEvent.VK_END: return "END";
          case KeyEvent.VK_HOME: return "HOME";
          case KeyEvent.VK_LEFT: return "LEFT";
          case KeyEvent.VK_UP: return "UP";
          case KeyEvent.VK_RIGHT: return "RIGHT";
          case KeyEvent.VK_DOWN: return "DOWN";

          // numpad numeric keys handled below
          case KeyEvent.VK_MULTIPLY: return "MULTIPLY";
          case KeyEvent.VK_ADD: return "ADD";
          case KeyEvent.VK_SEPARATOR: return "SEPARATOR";
          case KeyEvent.VK_SUBTRACT: return "SUBTRACT";
          case KeyEvent.VK_DECIMAL: return "DECIMAL";
          case KeyEvent.VK_DIVIDE: return "DIVIDE";
          case KeyEvent.VK_DELETE: return "DELETE";
          case KeyEvent.VK_NUM_LOCK: return "NUM_LOCK";
          case KeyEvent.VK_SCROLL_LOCK: return "SCROLL_LOCK";

          case KeyEvent.VK_F1: return "F1";
          case KeyEvent.VK_F2: return "F2";
          case KeyEvent.VK_F3: return "F3";
          case KeyEvent.VK_F4: return "F4";
          case KeyEvent.VK_F5: return "F5";
          case KeyEvent.VK_F6: return "F6";
          case KeyEvent.VK_F7: return "F7";
          case KeyEvent.VK_F8: return "F8";
          case KeyEvent.VK_F9: return "F9";
          case KeyEvent.VK_F10: return "F10";
          case KeyEvent.VK_F11: return "F11";
          case KeyEvent.VK_F12: return "F12";
          case KeyEvent.VK_F13: return "F13";
          case KeyEvent.VK_F14: return "F14";
          case KeyEvent.VK_F15: return "F15";
          case KeyEvent.VK_F16: return "F16";
          case KeyEvent.VK_F17: return "F17";
          case KeyEvent.VK_F18: return "F18";
          case KeyEvent.VK_F19: return "F19";
          case KeyEvent.VK_F20: return "F20";
          case KeyEvent.VK_F21: return "F21";
          case KeyEvent.VK_F22: return "F22";
          case KeyEvent.VK_F23: return "F23";
          case KeyEvent.VK_F24: return "F24";

          case KeyEvent.VK_PRINTSCREEN: return "PRINTSCREEN";
          case KeyEvent.VK_INSERT: return "INSERT";
          case KeyEvent.VK_HELP: return "HELP";
          case KeyEvent.VK_META: return "META";
          case KeyEvent.VK_BACK_QUOTE: return "BACK_QUOTE";
          case KeyEvent.VK_QUOTE: return "QUOTE";

          case KeyEvent.VK_KP_UP: return "KP_UP";
          case KeyEvent.VK_KP_DOWN: return "KP_DOWN";
          case KeyEvent.VK_KP_LEFT: return "KP_LEFT";
          case KeyEvent.VK_KP_RIGHT: return "KP_RIGHT";

          case KeyEvent.VK_DEAD_GRAVE: return "DEAD_GRAVE";
          case KeyEvent.VK_DEAD_ACUTE: return "DEAD_ACUTE";
          case KeyEvent.VK_DEAD_CIRCUMFLEX: return "DEAD_CIRCUMFLEX";
          case KeyEvent.VK_DEAD_TILDE: return "DEAD_TILDE";
          case KeyEvent.VK_DEAD_MACRON: return "DEAD_MACRON";
          case KeyEvent.VK_DEAD_BREVE: return "DEAD_BREVE";
          case KeyEvent.VK_DEAD_ABOVEDOT: return "DEAD_ABOVEDOT";
          case KeyEvent.VK_DEAD_DIAERESIS: return "DEAD_DIAERESIS";
          case KeyEvent.VK_DEAD_ABOVERING: return "DEAD_ABOVERING";
          case KeyEvent.VK_DEAD_DOUBLEACUTE: return "DEAD_DOUBLEACUTE";
          case KeyEvent.VK_DEAD_CARON: return "DEAD_CARON";
          case KeyEvent.VK_DEAD_CEDILLA: return "DEAD_CEDILLA";
          case KeyEvent.VK_DEAD_OGONEK: return "DEAD_OGONEK";
          case KeyEvent.VK_DEAD_IOTA: return "DEAD_IOTA";
          case KeyEvent.VK_DEAD_VOICED_SOUND: return "DEAD_VOICED_SOUND";
          case KeyEvent.VK_DEAD_SEMIVOICED_SOUND: return "DEAD_SEMIVOICED_SOUND";

          case KeyEvent.VK_AMPERSAND: return "AMPERSAND";
          case KeyEvent.VK_ASTERISK: return "ASTERISK";
          case KeyEvent.VK_QUOTEDBL: return "QUOTEDBL";
          case KeyEvent.VK_LESS: return "LESS";
          case KeyEvent.VK_GREATER: return "GREATER";
          case KeyEvent.VK_BRACELEFT: return "BRACELEFT";
          case KeyEvent.VK_BRACERIGHT: return "BRACERIGHT";
          case KeyEvent.VK_AT: return "AT";
          case KeyEvent.VK_COLON: return "COLON";
          case KeyEvent.VK_CIRCUMFLEX: return "CIRCUMFLEX";
          case KeyEvent.VK_DOLLAR: return "DOLLAR";
          case KeyEvent.VK_EURO_SIGN: return "EURO_SIGN";
          case KeyEvent.VK_EXCLAMATION_MARK: return "EXCLAMATION_MARK";
          case KeyEvent.VK_INVERTED_EXCLAMATION_MARK:
                   return "INVERTED_EXCLAMATION_MARK";
          case KeyEvent.VK_LEFT_PARENTHESIS: return "LEFT_PARENTHESIS";
          case KeyEvent.VK_NUMBER_SIGN: return "NUMBER_SIGN";
          case KeyEvent.VK_MINUS: return "MINUS";
          case KeyEvent.VK_PLUS: return "PLUS";
          case KeyEvent.VK_RIGHT_PARENTHESIS: return "RIGHT_PARENTHESIS";
          case KeyEvent.VK_UNDERSCORE: return "UNDERSCORE";

          case KeyEvent.VK_FINAL: return "FINAL";
          case KeyEvent.VK_CONVERT: return "CONVERT";
          case KeyEvent.VK_NONCONVERT: return "NONCONVERT";
          case KeyEvent.VK_ACCEPT: return "ACCEPT";
          case KeyEvent.VK_MODECHANGE: return "MODECHANGE";
          case KeyEvent.VK_KANA: return "KANA";
          case KeyEvent.VK_KANJI: return "KANJI";
          case KeyEvent.VK_ALPHANUMERIC: return "ALPHANUMERIC";
          case KeyEvent.VK_KATAKANA: return "KATAKANA";
          case KeyEvent.VK_HIRAGANA: return "HIRAGANA";
          case KeyEvent.VK_FULL_WIDTH: return "FULL_WIDTH";
          case KeyEvent.VK_HALF_WIDTH: return "HALF_WIDTH";
          case KeyEvent.VK_ROMAN_CHARACTERS: return "ROMAN_CHARACTERS";
          case KeyEvent.VK_ALL_CANDIDATES: return "ALL_CANDIDATES";
          case KeyEvent.VK_PREVIOUS_CANDIDATE: return "PREVIOUS_CANDIDATE";
          case KeyEvent.VK_CODE_INPUT: return "CODE_INPUT";
          case KeyEvent.VK_JAPANESE_KATAKANA: return "JAPANESE_KATAKANA";
          case KeyEvent.VK_JAPANESE_HIRAGANA: return "JAPANESE_HIRAGANA";
          case KeyEvent.VK_JAPANESE_ROMAN: return "JAPANESE_ROMAN";
          case KeyEvent.VK_KANA_LOCK: return "KANA_LOCK";
          case KeyEvent.VK_INPUT_METHOD_ON_OFF: return "INPUT_METHOD_ON_OFF";

          case KeyEvent.VK_AGAIN: return "AGAIN";
          case KeyEvent.VK_UNDO: return "UNDO";
          case KeyEvent.VK_COPY: return "COPY";
          case KeyEvent.VK_PASTE: return "PASTE";
          case KeyEvent.VK_CUT: return "CUT";
          case KeyEvent.VK_FIND: return "FIND";
          case KeyEvent.VK_PROPS: return "PROPS";
          case KeyEvent.VK_STOP: return "STOP";

          case KeyEvent.VK_COMPOSE: return "COMPOSE";
          case KeyEvent.VK_ALT_GRAPH: return "ALT_GRAPH";
        }

        if (keyCode >= KeyEvent.VK_NUMPAD0 && keyCode <= KeyEvent.VK_NUMPAD9) {
            char c = (char)(keyCode - KeyEvent.VK_NUMPAD0 + '0');
            return "NUMPAD"+c;
        }

        return "unknown(0x" + Integer.toString(keyCode, 16) + ")";
    }


    public void process(List<String> newArgs) {
        // TODO Auto-generated method stub

    }


}