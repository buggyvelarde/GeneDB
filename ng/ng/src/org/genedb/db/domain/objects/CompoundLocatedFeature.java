package org.genedb.db.domain.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A compound located feature is a located feature that in some sense
 * comprises a collection of located features. For example, a BasicGene
 * comprises a collection of Transcripts, and a PolypeptideRegionGroup
 * comprises a collection of PolypeptideDomains.
 *
 * This is a useful abstraction in certain circumstances,
 * and is used for constructing tracked diagrams.
 *
 * @author rh11
 *
 */
public abstract class CompoundLocatedFeature extends LocatedFeature {
    public abstract int getStrand();
    public String getName() { return getUniqueName(); }
    public String getShortName() { return getName(); }
    public String getDescription() { return null; }
    public abstract List<? extends LocatedFeature> getSubfeatures();

    public Iterable<? extends LocatedFeature> getSubfeaturesOrderedByPosition() {
        Collection<LocatedFeature> ret = new TreeSet<LocatedFeature>(positionComparator());
        ret.addAll(getSubfeatures());
        return ret;
    }

    public Iterable<? extends Iterable<? extends LocatedFeature>> getStratifiedSubfeatures() {
        return getStratifiedSubfeatures(false);
    }

    public Iterable<? extends Iterable<? extends LocatedFeature>> getStratifiedSubfeaturesByFirst() {
        return getStratifiedSubfeatures(true);
    }

    private Iterable<? extends Iterable<? extends LocatedFeature>> getStratifiedSubfeatures(boolean byFirst) {
        List<String> orderedStratumIds = new ArrayList<String>();
        Map<String, Collection<LocatedFeature>> subfeaturesByStratumId
            = new TreeMap<String, Collection<LocatedFeature>>();
        Iterable<? extends LocatedFeature> subfeatures = byFirst ? getSubfeaturesOrderedByPosition() : getSubfeatures();

        for (LocatedFeature locatedFeature: subfeatures) {
            String stratumId = locatedFeature.getStratumId();
            if (!subfeaturesByStratumId.containsKey(stratumId)) {
                orderedStratumIds.add(stratumId);
                subfeaturesByStratumId.put(stratumId, new TreeSet<LocatedFeature>(positionComparator()));
            }
            subfeaturesByStratumId.get(stratumId).add(locatedFeature);
        }

        if (byFirst) {
            List<Iterable<? extends LocatedFeature>> orderedStrata = new ArrayList<Iterable<? extends LocatedFeature>>();
            for (Object stratumId: orderedStratumIds) {
                orderedStrata.add(subfeaturesByStratumId.get(stratumId));
            }
            return orderedStrata;
        } else {
            return subfeaturesByStratumId.values();
        }
    }
}