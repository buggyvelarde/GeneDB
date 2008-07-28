package org.genedb.db.domain.objects;

import java.awt.Color;

/**
 * A located feature is a feature that has a specific location
 * on its source feature.
 *
 * @author rh11
 */
public abstract class LocatedFeature implements Comparable<LocatedFeature> {
    public abstract String getUniqueName();
    public abstract int getFmin();
    public abstract int getFmax();
    public String getUrl() { return null; }
    public Color getColor() { return null; }

    public int compareTo(LocatedFeature other) {
        if (this.getFmin() != other.getFmin())
            return this.getFmin() - other.getFmin();
        if (this.getFmax() != other.getFmax())
            return other.getFmax() - this.getFmax();
        return this.getUniqueName().compareTo(other.getUniqueName());
    }
}
