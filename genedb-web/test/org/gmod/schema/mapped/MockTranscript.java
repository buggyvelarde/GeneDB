package org.gmod.schema.mapped;

import java.sql.Timestamp;

import org.gmod.schema.feature.Transcript;

public class MockTranscript extends Transcript {
    private String uniqueName;
    public MockTranscript(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
        this.uniqueName = uniqueName;
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
