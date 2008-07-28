package org.genedb.db.domain.objects;

import org.gmod.schema.mapped.FeatureLoc;

import java.awt.Color;

public class SimplePolypeptideRegion extends PolypeptideRegion {

    private String name;
    private Color color;

    public SimplePolypeptideRegion(int fmin, int fmax, String name, String description, String score, Color color) {
        super(fmin, fmax, description, score);
        this.name = name;
        this.color = color;
    }

    public static SimplePolypeptideRegion build(org.gmod.schema.feature.PolypeptideRegion region, String description, String score, Color color) {
        FeatureLoc regionLoc = region.getRankZeroFeatureLoc();
        return new SimplePolypeptideRegion(regionLoc.getFmin(), regionLoc.getFmax(), region.getUniqueName(), description, score, color);
    }

    @Override
    public String getUniqueName() {
        return name;
    }

    @Override
    public Color getColor() {
        return color;
    }
}
