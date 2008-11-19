/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.genedb.jogra.plugins;

import org.genedb.jogra.domain.Gene;



/**
 *
 * @author art
 */
public class GeneViewModel {

    private boolean autoAddOldNames = true;
    private Gene gene;

    public boolean isAutoAddOldNames() {
        return autoAddOldNames;
    }

    public void setAutoAddOldNames(boolean autoAddOldNames) {
        this.autoAddOldNames = autoAddOldNames;
    }
    
    public Gene getGene() {
        return gene;
    }

    public void setGene(Gene gene) {
        this.gene = gene;
    }
    
}
