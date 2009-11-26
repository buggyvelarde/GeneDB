/**
 * The Java below was obtained from http://java.sun.com/developer/JDCTechTips/2005/tt1214.html
 */

package org.genedb.jogra.services;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

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
 * Manages filtering of list model
 */

public class FilteringModel extends AbstractListModel
    implements DocumentListener {
  List<Object> list;
  List<Object> filteredList;
  String lastFilter = "";
  

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
  
  public List<Object> getList(){
      return list;
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