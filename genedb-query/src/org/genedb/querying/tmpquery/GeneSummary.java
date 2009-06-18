package org.genedb.querying.tmpquery;

import java.io.Serializable;

public class GeneSummary implements Comparable<GeneSummary>, Serializable {
    private String systematicId;

    private String displayId;

    public String getDisplayId() {
        return displayId;
    }


    private String taxonDisplayName;

    private String product;

    private String topLevelFeatureName;

    private int left;

    public GeneSummary() {
        // No-args constructor
    }


    public GeneSummary(String systematicId) {
        setSystematicId(systematicId);
    }

    public GeneSummary(String systematicId, String taxonDisplayName,
            String product, String topLevelFeatureName, int left) {
        setSystematicId(systematicId);
        this.taxonDisplayName = taxonDisplayName;
        setProduct(product);
        this.topLevelFeatureName = topLevelFeatureName;
        this.left = left;
    }

    public String getSystematicId() {
        return systematicId;
    }

    public void setSystematicId(String systematicId) {
        this.systematicId = systematicId;
        String munged = removeSuffix(systematicId, ":pep");
        munged = removeSuffix(munged, ":mRNA");
        munged = removeSuffix(munged, ":pseudogenic_transcript");
        this.displayId = munged;
    }

    private String removeSuffix(String original, String suffix) {
        if (original.endsWith(suffix)) {
            return original.substring(0, original.length()-suffix.length());
        }
        return original;
    }

    public String getTaxonDisplayName() {
        return taxonDisplayName;
    }

    public void setTaxonDisplayName(String taxonDisplayName) {
        this.taxonDisplayName = taxonDisplayName;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        if (product != null) {
            this.product = product.replaceAll("\\t", " ; ");
        }
    }

    public String getTopLevelFeatureName() {
        return topLevelFeatureName;
    }

    public void setTopLevelFeatureName(String topLevelFeatureName) {
        this.topLevelFeatureName = topLevelFeatureName;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public boolean isConfigured() {
        return taxonDisplayName != null;
    }

    @Override
    public int compareTo(GeneSummary other) {
        int compare = this.taxonDisplayName.compareTo(other.getTaxonDisplayName());
        if (compare != 0) {
            return compare;
        }

        compare = this.topLevelFeatureName.compareTo(other.getTopLevelFeatureName());
        if (compare != 0) {
            return compare;
        }

        if (this.left < other.getLeft()) {
            return -1;
        }
        if (this.left > other.getLeft()) {
            return 1;
        }
        return this.systematicId.compareTo(other.getSystematicId());
    }


    @Override
    public String toString() {
        return String.format("sysid='%s', displayId='%s', taxon='%s', product='%s'", systematicId, displayId, taxonDisplayName, product);
    }


    @Override
    public boolean equals(Object obj) {
        return systematicId.equals(((GeneSummary)obj).getSystematicId());
    }


    @Override
    public int hashCode() {
        return systematicId.hashCode();
    }



}
