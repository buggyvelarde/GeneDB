package org.genedb.jogra.domain;

public class ExpressionZone implements java.io.Serializable {

    // Fields    
     private String label;
     private String description;

     // Constructors
     /** default constructor */
     public ExpressionZone() {}

     /** full constructor */
     public ExpressionZone(String label, String description) {
         this.label = label;
         this.description = description;
     }

    public String getDescription() {
        return this.description;
    }

    public String getLabel() {
        return this.label;
    }

    @Override
    public String toString() {
        return description;
    }

    
    
}


