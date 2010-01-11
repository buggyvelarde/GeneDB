package org.gmod.schema.utils;

import java.util.List;

public interface StrandedLocation {

    public abstract boolean isInterbase();

    public abstract int getMin();

    public abstract int getMax();

    public abstract boolean isMinPartial();

    public abstract boolean isMaxPartial();

    public abstract Strand getStrand();

    public abstract List<SingleLocation> getLocations();

    public abstract StrandedLocation getInterbaseVersion();

}
