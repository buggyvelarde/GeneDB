/**
 * This class extends FilteringJList to create a JList that is perfect for the Rationaliser. 
 */

package org.genedb.jogra.services;

import org.genedb.jogra.domain.Term;
import org.genedb.jogra.services.FilteringJList.FilteringModel;

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
     * Clears the model and adds all the items in the list to it
     */
    public void addAll(List<Term> terms){
        clear();
        for(Term t: terms){
            addElement(t);
        }     
    }
    
    
    /**
     * A special type of 'contains'
     */
    public int contains(Term t){ 
        List<Object> list = ((FilteringModel)getModel()).getList();
        for(Object o: list){
            Term term = (Term)o;
            if(term.getName().equals(t.getName())){
                return 0; //exists
            }else if (term.getName().equalsIgnoreCase(t.getName())){
                return 1; //exists but contains letters in a different case  
            }
        }
        return -1; //does not contain
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
         * Blue if the term has an evidence code. Change later to show different colours 
         * */
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
        
        
        /** 
         * Set Tooltip to show evidence codes
         **/
        public String getToolTipText(MouseEvent event){
            if(current!=null && (current.toString()).equals(super.getText())){ //Checking if we have a term and that it is the right one
                List<String> tempevc = current.getEvidenceCodes();
                if(tempevc!=null && !tempevc.isEmpty()){
                    String results = StringUtils.collectionToDelimitedString(tempevc, "\n");
                    return results;
                }
            }
            return new String();
        }
    }
    
    
    
} 