

package org.genedb.jogra.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Product implements Serializable, Comparable<Product> {

    private int id;
    private String description;
  

    public Product(String description, int id) {
        this.description = description;
        this.id = id;
    }

    @Override
    public String toString() {
        return description;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((description == null) ? 0 : description.hashCode());
        result = PRIME * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        final Product other = (Product) obj;
        if (id != other.id) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else {
            if (!description.equals(other.description)) {
                return false;
            }
        }
        return true;
    }
    
    /* NDS: Added so that products can be sorted (e.g. Collections.sort) */
    @Override
    public int compareTo(Product other){
        return this.description.compareTo(other.description);
        
    }

    public int getId() {
        return id;
    }
    
    

}
