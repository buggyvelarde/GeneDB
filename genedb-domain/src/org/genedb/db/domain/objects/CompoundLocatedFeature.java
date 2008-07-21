package org.genedb.db.domain.objects;

import java.util.List;

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
    public abstract List<? extends LocatedFeature> getSubfeatures();
}