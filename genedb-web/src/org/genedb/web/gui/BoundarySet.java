/**
 *
 */
package org.genedb.web.gui;

import org.genedb.db.domain.objects.LocatedFeature;

import java.util.Collection;
import java.util.TreeSet;

/**
 * Represents the boundary (either the START of the END) of a gene.
 */
class Boundary<T extends LocatedFeature>
    implements Comparable<Boundary<? extends LocatedFeature>>
{
    /**
     * Compares Boundaries, ordering them in the appropriate way for
     * track-allocation. If two boundaries have different locations, the
     * leftmost one goes first; if their locations coincide then we put
     * START boundaries before ENDs, and we put the START of a longer feature
     * before the START of a shorter.
     */
    public int compareTo(Boundary<? extends LocatedFeature> other) {
        int thisLoc = this.getLocation();
        int otherLoc = other.getLocation();

        if (thisLoc != otherLoc)
            return (thisLoc - otherLoc);
        else if (this.type == Boundary.Type.START && other.type == Boundary.Type.END)
            return -1;
        else if (this.type == Boundary.Type.END && other.type == Boundary.Type.START)
            return +1;
        else if (this.type == Boundary.Type.START && other.type == Boundary.Type.START) {
            int ret = other.feature.getFmax() - this.feature.getFmax();
            if (ret != 0)
                return ret;
        }
        return this.feature.getUniqueName().compareTo(other.feature.getUniqueName());
    }

    /**
     * Two Boundary objects are equal if they have the same type and
     * they refer to the same feature.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Boundary) {
            @SuppressWarnings("unchecked")
            Boundary<T> other = (Boundary<T>) obj;
            return (this.type == other.type && this.feature.equals(other.feature));
        }
        return false;
    }

    public enum Type {
        START, END
    }

    public Type type;
    public T feature;

    Boundary(Type type, T feature) {
        this.type = type;
        this.feature = feature;
    }

    /**
     * Returns the location of the boundary, which is to say the
     * <code>fmin</code> of the feature for a START boundary, or the
     * <code>fmax</code> for an END.
     *
     * @return the location
     */
    public int getLocation() {
        switch (type) {
        case START:
            return feature.getFmin();
        case END:
            return feature.getFmax();
        default:
            throw new IllegalStateException(
                    "GeneBoundary type is invalid. This should never happen!");
        }
    }

    @Override
    public String toString() {
        return String.format("%s of %s (%d..%d)", type, feature.getUniqueName(), feature.getFmin(),
            feature.getFmax());
    }
}

/**
 * A sorted set of feature boundaries. Useful for first fit track allocation.
 * @author rh11
 *
 * @param <T> the type of feature
 */
public class BoundarySet<T extends LocatedFeature> extends TreeSet<Boundary<T>> {
    public BoundarySet (Collection<? extends T> features) {
        super();
        addFeatures(features);
    }
    public void addFeatures(Collection<? extends T> features) {
        for (T feature: features) {
            this.addFeature(feature);
        }
    }
    public void addFeature(T feature) {
        this.add(new Boundary<T>(Boundary.Type.START, feature));
        this.add(new Boundary<T>(Boundary.Type.END,   feature));
    }
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        boolean first = true;
        for (Boundary<? extends LocatedFeature> boundary: this) {
            if (first)
                first = false;
            else
                s.append(", ");
            s.append(boundary.toString());
        }
        return s.toString();
    }
}