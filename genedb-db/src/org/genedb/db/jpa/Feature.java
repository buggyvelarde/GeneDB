package org.genedb.db.jpa;

import org.genedb.db.hibernate3gen.BaseFeature;
import org.genedb.db.hibernate3gen.FeatureProp;

public class Feature extends BaseFeature {

    @Override
    public void setResidues(String residues) {
        if (residues != null) {
            super.setResidues(residues);
            super.setSeqlen(residues.length());
            // TODO - should be fixed once packages sorted
            //	super.setMd5checksum(FeatureUtils.calcMD5(residues));
        }
    }

    public void addFeatureProp(FeatureProp fp) {
        fp.setFeature(this);
        super.getFeatureProps().add(fp);
    }

    public String getDisplayName() {
        System.err.println("getName is returning '"+getName()+"'");
        return (getName() != null) ? getName() : getUniquename(); 
    }

}
