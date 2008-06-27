package org.genedb.db.domain.objects;

public class Gap extends LocatedFeature {
    private String uniqueName;
    private int fmin, fmax;

    public Gap(String uniqueName, int fmin, int fmax) {
        this.uniqueName = uniqueName;
        this.fmin = fmin;
        this.fmax = fmax;
    }

    @Override
    public int getFmax() {
        return fmax;
    }

    @Override
    public int getFmin() {
        return fmin;
    }

    @Override
    public String getUniqueName() {
        return uniqueName;
    }
}
