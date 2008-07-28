package org.genedb.db.domain.objects;

public abstract class PolypeptideRegion extends LocatedFeature {

    private int fmin;
    private int fmax;
    private String description;
    private String score;

    public PolypeptideRegion(int fmin, int fmax, String description, String score) {
        this.fmin = fmin;
        this.fmax = fmax;
        this.description = description;
        this.score = score;
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
    public String getUrl() {
        return null;
    }

    public String getDescription() {
        return description;
    }

    public String getScore() {
        return score;
    }
}