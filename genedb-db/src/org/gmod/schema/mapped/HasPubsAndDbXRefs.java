package org.gmod.schema.mapped;

/**
 * Something that has Pub and DbXRef objects associated with it,
 * i.e. a Feature or a FeatureCvTerm. (Are there others?)
 *
 * @author rh11
 */
public interface HasPubsAndDbXRefs {
    public Object addPub(Pub pub);
    public Object addDbXRef(DbXRef dbXRef);
}
