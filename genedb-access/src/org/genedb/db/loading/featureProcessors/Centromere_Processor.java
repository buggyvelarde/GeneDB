package org.genedb.db.loading.featureProcessors;

import org.genedb.db.loading.EmblQualifiers;
import org.genedb.db.loading.FeatureProcessor;
import org.genedb.db.loading.ProcessingPhase;

import org.gmod.schema.feature.Centromere;
import org.gmod.schema.mapped.Cv;
import org.gmod.schema.mapped.Feature;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;

import java.sql.Timestamp;
import java.util.Date;

//FT   centromere      459080..461461
//FT                   /controlled_curation="term=centromere; attribution=Baker,
//FT                   David; dbxref=PMID:16617116; date=2000929"
//FT                   /db_xref="PMID:16617116"
//FT                   /so_type="centromere"
//FT                   /systematic_id="PF3D7CEN01"

public class Centromere_Processor extends BaseFeatureProcessor implements FeatureProcessor {

    @Override
    public void processStrandedFeature(Feature parent, StrandedFeature f, int offset) {
        // TODO Auto-generated method stub
        logger.debug("Entering processing for centromere");
        Location loc = f.getLocation();
        Annotation an = f.getAnnotation();
        short strand = (short) f.getStrand().getValue();
        String systematicId = (String) an.getProperty("systematic_id");

        Timestamp now = new Timestamp(new Date().getTime());
        Centromere centromere = Centromere.make(parent, loc, systematicId, organism, now);
        createFeaturePropsFromNotes(centromere, an, EmblQualifiers.QUAL_NOTE, MISC_NOTE, 0);
        createDbXRefs(centromere, an);
        Cv CV_CONTROLLEDCURATION = cvDao.getCvByName("CC_genedb_controlledcuration");
        createControlledCuration(centromere, an, CV_CONTROLLEDCURATION);
    }

    @Override
    public ProcessingPhase getProcessingPhase() {
        return ProcessingPhase.SIXTH;
    }

}
