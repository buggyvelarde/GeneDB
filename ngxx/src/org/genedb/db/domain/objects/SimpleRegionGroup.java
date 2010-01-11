package org.genedb.db.domain.objects;

/**
 * A simple polypeptide region group. Simply consists of a title and a collection of
 * regions.
 *
 * @author rh11
 */
public class SimpleRegionGroup extends PolypeptideRegionGroup {
    private String title;
    private String abbreviation;

    public SimpleRegionGroup(String title, String abbreviation) {
        this.title = title;
        this.abbreviation = abbreviation;
    }

    @Override
    public String getUniqueName() {
        return title;
    }

    @Override
    public String getShortName() {
        return abbreviation;
    }
}
