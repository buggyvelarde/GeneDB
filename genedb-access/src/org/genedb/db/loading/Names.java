package org.genedb.db.loading;

import java.util.List;

public class Names {
    
    private String systematicId; // Permanent, or temporary unique id
    private boolean idTemporary; // Is the systematic id permanent or temporary
    private String primary; // ie the gene name
    private String protein; // the name of the protein
    private List<String> synonyms; // a list of synonyms
    private List<String> obsolete; // a list of names that are no longer recommended
    private String reserved; // A future primary name for this feature
    private List<String> previousSystematicIds; // a list of old systematic ids
    
    public String getProtein() {
        return this.protein;
    }
    public void setProtein(String protein) {
        this.protein = protein;
    }
    public boolean isIdTemporary() {
        return this.idTemporary;
    }
    public List<String> getObsolete() {
        return this.obsolete;
    }
    public void setObsolete(List<String> obsolete) {
        this.obsolete = obsolete;
    }
    public String getPrimary() {
        return this.primary;
    }
    public void setPrimary(String primary) {
        this.primary = primary;
    }
    public String getReserved() {
        return this.reserved;
    }
    public void setReserved(String reserved) {
        this.reserved = reserved;
    }
    public List<String> getSynonyms() {
        return this.synonyms;
    }
    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }
    public String getSystematicId() {
        return this.systematicId;
    }
    public void setSystematicIdAndTemp(String systematicId, boolean idTemporary) {
        this.systematicId = systematicId;
        this.idTemporary = idTemporary;
    }
    public List<String> getPreviousSystematicIds() {
        return this.previousSystematicIds;
    }
    public void setPreviousSystematicIds(List<String> previousSystematicIds) {
        this.previousSystematicIds = previousSystematicIds;
    }
    
}
