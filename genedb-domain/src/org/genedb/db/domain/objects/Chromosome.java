package org.genedb.db.domain.objects;

/**
 * Represents a chromosome.
 *
 * @author rh11
 */
public class Chromosome {
    private int featureId;
    private String name;
    private int length;

    public Chromosome(String name, int chromsomeFeatureId, int length) {
        this.length = length;
        this.name = name;
        this.featureId = chromsomeFeatureId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getLength() {
        return length;
    }
    public void setLength(int length) {
        this.length = length;
    }
    public int getFeatureId() {
        return featureId;
    }
}
