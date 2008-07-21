package org.genedb.db.domain.objects;

/**
 * A simple polypeptide region group. Simply consists of a title and a collection of
 * regions.
 *
 * @author rh11
 */
public class SimpleRegionGroup extends PolypeptideRegionGroup {
    private String title;

    public SimpleRegionGroup(String title) {
        this.title = title;
    }

    @Override
    public String getUniqueName() {
        return title;
    }
}
