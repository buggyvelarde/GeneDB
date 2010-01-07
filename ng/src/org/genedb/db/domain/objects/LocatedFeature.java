package org.genedb.db.domain.objects;

import java.awt.Color;
import java.io.Serializable;
import java.util.Comparator;

/**
 * A located feature is a feature that has a specific location
 * on its source feature.
 *
 * @author rh11
 */
public abstract class LocatedFeature implements Comparable<LocatedFeature>, Serializable {
    public abstract String getUniqueName();
    public abstract int getFmin();
    public abstract int getFmax();
    public String getUrl() { return null; }
    public Color getColor() { return null; }
    public String getStratumId() { return ""; }

    /**
     * The default comparison is by position (see {@link #positionComparator()})
     * but this can be overridden by subclasses.
     */
    public int compareTo(LocatedFeature other) {
        return positionComparator.compare(this, other);
    }

    private static final Comparator<LocatedFeature> positionComparator
        = new Comparator<LocatedFeature>() {
            public int compare(LocatedFeature a, LocatedFeature b) {
                if (a.getFmin() != b.getFmin())
                    return a.getFmin() - b.getFmin();
                if (a.getFmax() != b.getFmax())
                    return b.getFmax() - a.getFmax();
                return a.getUniqueName().compareTo(b.getUniqueName());
            }
        };

    public static final Comparator<LocatedFeature> positionComparator() {
        return positionComparator;
    }
}
