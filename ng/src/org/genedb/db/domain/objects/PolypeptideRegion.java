package org.genedb.db.domain.objects;

public abstract class PolypeptideRegion extends LocatedFeature {

    private int fmin;
    private int fmax;
    private String description;
    private String score;
    private String significance;

    public PolypeptideRegion(int fmin, int fmax, String description, String score, String significance) {
        this.fmin = fmin;
        this.fmax = fmax;
        this.description = description;
        this.score = score;
        this.significance = significance;
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
    
    public String getSignificance() {
        return significance;
    }
}