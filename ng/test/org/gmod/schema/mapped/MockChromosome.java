package org.gmod.schema.mapped;

import java.sql.Timestamp;

import org.gmod.schema.feature.Chromosome;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureCvTerm;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.mapped.Pub;

public class MockChromosome extends Chromosome {
    private String uniqueName;
    public MockChromosome(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
        this.uniqueName = uniqueName;
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
    
    @Override
    public int getFeatureId(){
        return Integer.parseInt(uniqueName);
    }
}
