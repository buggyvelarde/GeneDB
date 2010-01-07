package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import javax.persistence.Entity;

@Entity
@FeatureType(cv = "sequence", term = "EST_match")
public class ESTMatch extends NucleotideMatch {

    ESTMatch() {
        super();
    }

    public ESTMatch(Organism organism, String uniqueName, boolean analysis, boolean obsolete) {
        super(organism, uniqueName, analysis, obsolete);
    }

    public ESTMatch(Organism organism, String uniqueName) {
        super(organism, uniqueName);
    }

    public static ESTMatch create(EST est, TopLevelFeature target,
            int sourceFmin, int sourceFmax, int sourceStrand,
            int targetFmin, int targetFmax, int targetStrand) {

        if (sourceFmin > sourceFmax) {
            throw new IllegalArgumentException(String.format("source fmin (%d) > source fmax (%d)",
                sourceFmin, sourceFmax));
        }
        if (targetFmin > targetFmax) {
            throw new IllegalArgumentException(String.format("target fmin (%d) > target fmax (%d)",
                targetFmin, targetFmax));
        }

        String matchUniqueName;
        if (targetStrand >= 0) {
            matchUniqueName = String.format("%s:estMatch%d-%d", target.getUniqueName(), targetFmin+1, targetFmax);
        } else {
            matchUniqueName = String.format("%s:estMatch(%d-%d)", target.getUniqueName(), targetFmin+1, targetFmax);
        }
        ESTMatch estMatch = new ESTMatch(est.getOrganism(), matchUniqueName, true, false);

        //Feature#addLocatedChild(Feature child, int fmin, int fmax, int strand, Integer phase, int locgroup, int rank)
        est.addLocatedChild(estMatch, sourceFmin, sourceFmax, sourceStrand, null, 0, 0);
        target.addLocatedChild(estMatch, targetFmin, targetFmax, targetStrand, null, 0, 1);

        return estMatch;
    }
}
