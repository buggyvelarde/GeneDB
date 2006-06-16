package org.genedb.db.jpa;

import org.genedb.db.hibernate3gen.BaseFeature;
import org.genedb.db.hibernate3gen.FeatureProp;

import java.util.Set;

public class Feature extends BaseFeature {

    @Override
    public void setResidues(String residues) {
	super.setResidues(residues);
	super.setSeqlen(residues.length());
	// TODO - should be fixed once packages sorted
//	super.setMd5checksum(FeatureUtils.calcMD5(residues));
    }
    
    public void addFeatureProp(FeatureProp fp) {
	fp.setFeature(this);
	super.getFeatureProps().add(fp);
    }

}
