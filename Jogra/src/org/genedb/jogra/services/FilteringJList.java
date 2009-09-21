/**
 * The Java below was obtained from http://java.sun.com/developer/JDCTechTips/2005/tt1214.html
 * We have added a method called addAll to it to enable a list of objects to be added to the JList
 */

package org.genedb.jogra.services;

import org.genedb.jogra.domain.Term;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.util.*;

public class FilteringJList extends JList {
  private JTextField input;

public FilteringJList() {
  FilteringModel model = new FilteringModel();
  setModel(new FilteringModel());
}

/**
 * Associates filtering document listener to text
 * component.
 */

 public void installJTextField(JTextField input) {
   if (input != null) {
     this.input = input;
     FilteringModel model = (FilteringModel)getModel();
     input.getDocument().addDocumentListener(model);
   }
 }

/**
 * Disassociates filtering document listener from text
 * component.
 */

 public void uninstallJTextField(JTextField input) {
   if (input != null) {
     FilteringModel model = (FilteringModel)getModel();
     input.getDocument().removeDocumentListener(model);
     this.input = null;
   }
 }

/**
 * Doesn't let model change to non-filtering variety
 */

public void setModel(ListModel model) {
  if (!(model instanceof FilteringModel)) {
    throw new IllegalArgumentException();
  } else {
    super.setModel(model);
  }
}

/**
 * Adds item to model of list
 */
public void addElement(Object element) {
  ((FilteringModel)getModel()).addElement(element);
}

/**
 * Added by nds 16.09.2009: Clears the list and adds all the items in the list
 */
public void addAll(List<Term> terms){
    ((FilteringModel)getModel()).clear();
    for(Term t: terms){
        addElement(t);
    }
    
}

/**
 * Manages filtering of list model
 */

private class FilteringModel extends AbstractListModel
    implements DocumentListener {
  List<Object> list;
  List<Object> filteredList;
  String lastFilter = "";
  
  //Added by nds on 16.9.2009
  public void clear(){
      list.clear();
  }

  public FilteringModel() {
    list = new ArrayList<Object>();
    filteredList = new ArrayList<Object>();
  }

  public void addElement(Object element) {
    list.add(element);
    filter(lastFilter);
  }

  public int getSize() {
    return filteredList.size();
  }

  public Object getElementAt(int index) {
    Object returnValue;
    if (index < filteredList.size()) {
      returnValue = filteredList.get(index);
    } else {
      returnValue = null;
    }
    return returnValue;
  }

  void filter(String search) {
    filteredList.clear();
    for (Object element: list) {
      if (element.toString().indexOf(search, 0) != -1) {
        filteredList.add(element);
      }
    }
    fireContentsChanged(this, 0, getSize());
  }

  // DocumentListener Methods

  public void insertUpdate(DocumentEvent event) {
    Document doc = event.getDocument();
    try {
      lastFilter = doc.getText(0, doc.getLength());
      filter(lastFilter);
    } catch (BadLocationException ble) {
      System.err.println("Bad location: " + ble);
    }
  }

  public void removeUpdate(DocumentEvent event) {
    Document doc = event.getDocument();
    try {
      lastFilter = doc.getText(0, doc.getLength());
      filter(lastFilter);
    } catch (BadLocationException ble) {
      System.err.println("Bad location: " + ble);
    }
  }

  public void changedUpdate(DocumentEvent event) {
  }
 }
} 