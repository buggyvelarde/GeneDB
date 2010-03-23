/**
 * This class extends FilteringJList to create a JList that is perfect for the Rationaliser. 
 * FilteringJList was obtained from: http://java.sun.com/developer/JDCTechTips/2005/tt1214.html
 */


package org.genedb.jogra.services;

import org.genedb.jogra.domain.Term;
import org.springframework.util.StringUtils;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class RationaliserJList extends FilteringJList {
    
    public RationaliserJList(){
        super();
        this.setCellRenderer(new ColorRenderer()); //to display terms with evidence codes in colour
    }
    
    /**
     * Clear the list
     */
    public void clear(){
        ((FilteringModel)getModel()).getList().clear();
    }
    
    
    /**
     * Clears the model and adds all the items 
     * in the list to it
     */
    public void addAll(List<Term> terms){
        clear();
        for(Term t: terms){
            addElement(t);
        }     
    }
    
    
    /**
     * A special type of 'contains'
     * 1: contains
     * 2: contains but in a different case
     * 0: does not contain
     */
    public int contains(Term t){ 
        List<Object> list = ((FilteringModel)getModel()).getList();
        for(Object o: list){
            Term term = (Term)o;
            if(term.getName().equals(t.getName())){
                return 1; //exists
            }else if (term.getName().equalsIgnoreCase(t.getName())){
                return 2; //exists but contains letters in a different case  
            }
        }
        return 0; //does not contain
    }
    
    /**
     * A way to call contains with a string
     * @param termName
     * @return
     */
    public int contains(String termName){
        return contains(new Term(0,termName));
    }
    

    /**
     * Class to paint the term in a different colour if they have evidence codes
     * It also sets the ToolTipText to have the evidence codes
     */
    class ColorRenderer extends DefaultListCellRenderer {
        Term current;
        
        /**
         * Creates a new instance of ColorRenderer 
         **/
        public ColorRenderer() { }
        
          
        /** 
         * Displays the terms in different colours depending on the evidence codes attached to them 
         * Red: If it only has an electronic evidence code
         * Amber: If it has a mixture
         * Green: If it has all manual evidence codes
         * 
         * Remains black if there are no evidence codes
         * */
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
          super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
          if(value instanceof Term){
              current = (Term)value;
              List<String> evidenceCodes = current.getEvidenceCodes();
              if(evidenceCodes!=null && !evidenceCodes.isEmpty()){
                  //Is it just one evidence code and is it electronic?
                  if(evidenceCodes.size()==1){
                      if(evidenceCodes.contains("Inferred from Electronic Annotation") ||
                         evidenceCodes.contains("inferred from electronic annotation")){
                          //Red
                          setForeground(Color.RED);
                      }
                  }else{
                      if(evidenceCodes.contains("Inferred from Electronic Annotation") ||
                         evidenceCodes.contains("inferred from electronic annotation")){
                          //Amber
                          setForeground(Color.ORANGE);
                      }else{
                          //Green
                          setForeground(new Color(51, 153,51));
                      }
                      
                  }
                  
              }
          }
          return this;
        }
        
        
        /** 
         * Set Tooltip to show evidence codes
         **/
        public String getToolTipText(MouseEvent event){
            if(current!=null && (current.toString()).equals(super.getText())){ //Checking if we have a term and that it is the right one
                List<String> tempevc = current.getEvidenceCodes();
                if(tempevc!=null && !tempevc.isEmpty()){
                    String results = StringUtils.collectionToDelimitedString(tempevc, "; ");
                    return results;
                }
            }
            return new String();
        }
    }
    
    
    
} 