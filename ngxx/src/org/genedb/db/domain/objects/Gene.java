package org.genedb.db.domain.objects;

import java.util.List;

/**
 * Extends BasicGene to add some additional information that can be found
 * in the database.
 *
 * @author art, rh11
 */
public class Gene extends BasicGene {

    public Gene () {
        // Deliberately empty
    }

    public Gene (BasicGene basis) {
        super(basis);
    }

    private List<String> orthologues;
    private List<String> paralogues;
    private List<String> clusters;

    private String reservedName;
    private String previousSystematicId;

    public List<String> getClusters() {
        return clusters;
    }

    public void setClusters(List<String> clusters) {
        this.clusters = clusters;
    }

    public List<String> getOrthologues() {
        return orthologues;
    }

    public void setOrthologues(List<String> orthologues) {
        this.orthologues = orthologues;
    }

    public List<String> getParalogues() {
        return paralogues;
    }

    public void setParalogues(List<String> paralogues) {
        this.paralogues = paralogues;
    }

    public void setReservedName(String reservedName) {
        this.reservedName = reservedName;
    }

    public String getReservedName() {
        return reservedName;
    }
    public void setPreviousSystematicId(String previousSystematicId) {
        this.previousSystematicId = previousSystematicId;
    }
    public String getPreviousSystematicId() {
        return previousSystematicId;
    }

}
