package org.gmod.schema.mapped;

import java.sql.Timestamp;

import org.gmod.schema.feature.Transcript;

public class TestTranscript extends Transcript {
    public TestTranscript(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }
    
    @Override
    public FeatureCvTerm addCvTerm(CvTerm cvTerm) {
        ((Feature)this).setType(cvTerm);
        return null;
    }
}
