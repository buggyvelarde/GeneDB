package org.genedb.db.loading;

import java.util.List;

public class Names {

    private String systematicId; // Permanent, or temporary unique id
    private boolean idTemporary; // Is the systematic id permanent or temporary
    private String primary; // ie the gene name
    private String protein; // the name of the protein
    private List<String> synonyms; // a list of synonyms
    private List<String> obsolete; // a list of names that are no longer recommended
    private List<String> reserved; // A future primary name for this feature
    private List<String> previousSystematicIds; // a list of old systematic ids

    /**
     * Get the protein name, which is often different from the gene name eg in case
     *
     * @return the protein name, or null if not set
     */
    public String getProtein() {
        return this.protein;
    }

    /**
     * Set the protein name, which is often different from the gene name eg in case
     *
     * @param protein the protein name
     */
    public void setProtein(String protein) {
        this.protein = protein;
    }


    /**
     * Is the systematic id for this feature temporary
     *
     * @return true if it is temporary ie likely to change
     */
    public boolean isIdTemporary() {
        return this.idTemporary;
    }


    /**
     * Get a list of obsolete names for this feature
     *
     * @return the list of obsolete names, possibly empty
     */
    public List<String> getObsolete() {
        return this.obsolete;
    }


    /**
     *
     *
     * @param obsolete
     */
    public void setObsolete(List<String> obsolete) {
        this.obsolete = obsolete;
    }

    /**
     * The human readable name, e.g. the gene name
     *
     * @return the primary name
     */
    public String getPrimary() {
        return this.primary;
    }
    public void setPrimary(String primary) {
        this.primary = primary;
    }
    public List<String> getReserved() {
        return this.reserved;
    }
    public void setReserved(List<String> reserved) {
        this.reserved = reserved;
    }
    public List<String> getSynonyms() {
        return this.synonyms;
    }
    /**
     * Set the list of synonyms, which are other names which have no special meaning
     * now or previously.
     *
     * @param synonyms
     */
    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }
    public String getSystematicId() {
        return this.systematicId;
    }
    /**
     * Store the feature's systematic id, and whether it's temporary
     *
     * @param systematicId the systematic id
     * @param idTemporary true if the id is likely to change in the future
     */
    public void setSystematicIdAndTemp(String systematicId, boolean idTemporary) {
        this.systematicId = systematicId;
        this.idTemporary = idTemporary;
    }
    public List<String> getPreviousSystematicIds() {
        return this.previousSystematicIds;
    }
    /**
     * Set a List of ids which have previously been used as systematic ids for
     * this feature.
     *
     * @param previousSystematicIds
     */
    public void setPreviousSystematicIds(List<String> previousSystematicIds) {
        this.previousSystematicIds = previousSystematicIds;
    }

}
