package org.genedb.db.domain.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class PolypeptideRegionGroup extends CompoundLocatedFeature {

    private int fmin = Integer.MAX_VALUE;
    private int fmax = Integer.MIN_VALUE;
    private SortedSet<PolypeptideRegion> domains = new TreeSet<PolypeptideRegion>();

    /**
     * Add a region to this group.
     *
     * @param region the region to add
     * @return this object, for method chaining
     */
    public PolypeptideRegionGroup addRegion(PolypeptideRegion region) {
        domains.add(region);
        if (region.getFmin() < fmin) {
            fmin = region.getFmin();
        }
        if (region.getFmax() > fmax) {
            fmax = region.getFmax();
        }
        return this;
    }

    @Override
    public int getStrand() {
        return 0;
    }

    @Override
    public List<PolypeptideRegion> getSubfeatures() {
        List<PolypeptideRegion> domainsList = new ArrayList<PolypeptideRegion>(domains);
        return Collections.unmodifiableList(domainsList);
    }

    public boolean isEmpty() {
        return domains.isEmpty();
    }

    @Override
    public int getFmax() {
        if (isEmpty()) {
            throw new IllegalStateException(String.format("Domain group '%s' has no domains!", getUniqueName()));
        }
        return fmax;
    }

    @Override
    public int getFmin() {
        if (isEmpty()) {
            throw new IllegalStateException(String.format("Domain group '%s' has no domains!", getUniqueName()));
        }
        return fmin;
    }
}