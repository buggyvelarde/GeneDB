package org.gmod.schema.feature;

import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

/**
 * Extent of the region not traversing the lipid bilayer. (<code>SO:0001072</code>)
 *
 * @author rh11
 *
 */
public abstract class ExtramembraneRegion extends MembraneStructureComponent {

    ExtramembraneRegion() {
        // empty
    }

    public ExtramembraneRegion(Organism organism, CvTerm cvTerm, String uniqueName,
            boolean analysis, boolean obsolete, Timestamp timeAccessioned,
            Timestamp timeLastModified) {
        super(organism, cvTerm, uniqueName, analysis, obsolete, timeAccessioned, timeLastModified);
    }

    public ExtramembraneRegion(Organism organism, CvTerm cvTerm, String uniqueName,
            boolean analysis, boolean obsolete) {
        super(organism, cvTerm, uniqueName, analysis, obsolete);
    }

}
