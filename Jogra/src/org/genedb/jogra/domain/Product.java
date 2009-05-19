/*
 * 5.5.2009: Added a method to retrieve all the organisms that are connected to a product
 */

package org.genedb.jogra.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Product implements Serializable {

    private int id;
    private String description;
    private List<String> organisms = new ArrayList<String>();

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

    public int getId() {
        return id;
    }
    
    public void setOrganisms(List<String> orgs) {
        organisms = orgs;
    }

    public List<String> getOrganisms() {
        return organisms;
    }

}
