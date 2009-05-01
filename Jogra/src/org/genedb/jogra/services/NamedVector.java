
/**
 * Slightly modified helper class from Swing book to store the 'name' of the 'root' along with the children values (elements in vector)
 * 
 */

package org.genedb.jogra.services;

import java.util.Vector;

public class NamedVector extends Vector {
  String name;

  public NamedVector(String name) {
    this.name = name;
  }

  public NamedVector(String name, Vector elements) {
      this.name = name;
      addAll(elements);
      
    }

  public String toString() {
    return name;
  }
}
