package org.gmod.schema.mapped;

import java.sql.Timestamp;

import org.gmod.schema.feature.Chromosome;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureCvTerm;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.mapped.Pub;

public class TestChromosome extends Chromosome {
    public TestChromosome(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }
    @Override
    protected Pub nullPub() {
        return null;
    }
    
    @Override
    public FeatureCvTerm addCvTerm(CvTerm cvTerm) {
        ((Feature)this).setType(cvTerm);
        return null;
    }
}
