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
        if (getSubfeatures().size() == 1) {
            return getSubfeatures().get(0).getUniqueName();
        }
        return title;
    }

    @Override
    public String getShortName() {
        if (getSubfeatures().size() == 1) {
            return getSubfeatures().get(0).getUniqueName();
        }
        return abbreviation;
    }

    @Override
    public String getDescription() {
        if (getSubfeatures().size() == 1) {
            return getSubfeatures().get(0).getDescription();
        }
        return super.getDescription();
    }
}
