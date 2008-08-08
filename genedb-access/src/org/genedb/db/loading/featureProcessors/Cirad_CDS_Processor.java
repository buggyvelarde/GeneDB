package org.genedb.db.loading.featureProcessors;

import java.util.List;

import org.biojava.bio.Annotation;
import org.genedb.db.loading.MiningUtils;

import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureProp;

public class Cirad_CDS_Processor extends CDS_Processor {

    /**
     * This is called once the basic gene structure has been created. It calls
     * the base class copy to process common qualifiers like 'note'. The
     * following is an example of the code to process a /evidence qualifier. The
     * code is written so that you can have multiple values for a given
     * qualifier, although the if statement stops that here.
     *
     * @see org.genedb.db.loading.featureProcessors.CDS_Processor#processQualifiers(Feature,
     *      Annotation)
     */
    @Override
    protected void processQualifiers(Feature polypeptide, Annotation an) {
        super.processQualifiers(polypeptide, an);

        CvTerm evidenceTerm = cvDao.getCvTermByNameAndCvName("private", "genedb_misc");
        List<String> evidences = MiningUtils.getProperties("evidence", an);
        if (evidences.size() > 1) {
            throw new RuntimeException("Found more than 1 value for 'evidence'");
        }
        int rank = 0;
        for (String evidence : evidences) {
            FeatureProp fp = new FeatureProp(polypeptide, evidenceTerm, evidence, rank);
            this.sequenceDao.persist(fp);
            rank++;
        }
    }

}
