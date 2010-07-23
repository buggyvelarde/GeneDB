package org.gmod.schema.utils;

import java.util.Collections;
import java.util.List;

public class SingleLocation implements StrandedLocation {

    boolean interbase;
    int min;
    int max;
    boolean minPartial;
    boolean maxPartial;
    Strand strand = Strand.FORWARD; // FIXME

    public SingleLocation(org.biojava.bio.symbol.Location loc, org.biojava.bio.seq.StrandedFeature.Strand strand) {
        this.interbase = false;
        this.min = loc.getMin();
        this.max = loc.getMax();
        this.minPartial = false;
        this.maxPartial = true;
        switch (strand.getValue()) {
        case 1:
            this.strand = Strand.FORWARD;
            break;
        case 0:
            this.strand = Strand.UNKNOWN;
            break;
        case -1:
            this.strand = Strand.REVERSE;
            break;
        }
    }

    public SingleLocation(SingleLocation other, boolean convertToInterbase) {
        this.interbase = convertToInterbase;
        this.max = other.max;
        this.maxPartial = other.maxPartial;
        this.min = other.min;
        this.minPartial = other.minPartial;
        this.strand = other.strand;
        convertCoordinateSystem(convertToInterbase);
    }

    protected void convertCoordinateSystem(boolean convertToInterbase) {
        if (convertToInterbase) {
            this.min -= 1;
        } else {
            // Convert from interbase to embl type coordinates
            this.min += 1;
        }
    }


    /* (non-Javadoc)
     * @see org.gmod.schema.utils.Location#isInterbase()
     */
    public boolean isInterbase() {
        return interbase;
    }

    /* (non-Javadoc)
     * @see org.gmod.schema.utils.Location#getMin()
     */
    public int getMin() {
        return min;
    }

    /* (non-Javadoc)
     * @see org.gmod.schema.utils.Location#getMax()
     */
    public int getMax() {
        return max;
    }

    /* (non-Javadoc)
     * @see org.gmod.schema.utils.Location#isMinPartial()
     */
    public boolean isMinPartial() {
        return minPartial;
    }

    /* (non-Javadoc)
     * @see org.gmod.schema.utils.Location#isMaxPartial()
     */
    public boolean isMaxPartial() {
        return maxPartial;
    }

    /* (non-Javadoc)
     * @see org.gmod.schema.utils.Location#getStrand()
     */
    public Strand getStrand() {
        return strand;
    }

    public List<SingleLocation> getLocations() {
        return Collections.singletonList(this);
    }

    public SingleLocation getInterbaseVersion() {
        if (interbase) {
            return this;
        }
        SingleLocation ret = new SingleLocation(this, true);
        return ret;
    }
}
