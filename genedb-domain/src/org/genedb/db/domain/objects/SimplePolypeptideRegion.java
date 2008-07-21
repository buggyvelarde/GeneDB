package org.genedb.db.domain.objects;

import org.gmod.schema.mapped.FeatureLoc;

public class SimplePolypeptideRegion extends PolypeptideRegion {

    private String name;

    public SimplePolypeptideRegion(int fmin, int fmax, String name, String description, String score) {
        super(fmin, fmax, description, score);
        this.name = name;
    }

    public static SimplePolypeptideRegion build(org.gmod.schema.feature.PolypeptideRegion region) {
        FeatureLoc regionLoc = region.getRankZeroFeatureLoc();
        return new SimplePolypeptideRegion(regionLoc.getFmin(), regionLoc.getFmax(), region.getUniqueName(), null, null);
    }

    @Override
    public String getUniqueName() {
        return name;
    }

}
