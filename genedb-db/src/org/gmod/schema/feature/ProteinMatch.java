package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.Organism;

import org.apache.log4j.Logger;

import javax.persistence.Entity;

/**
 * ProteinMatch features are currently used in two rather different ways.
 * <ul>
 * <li> They are used to represent /similarity data. In this case, the ProteinMatch
 *      has two FeatureLocs representing the source (rank 0) and target (rank 1)
 *      of the match.
 * </ul> They are used to represent orthologue clusters. In this case, the ProteinMatch
 *      belongs to the "dummy" organism and has no FeatureLocs. It is the object of a
 *      number of <code>orthologous_to</code> FeatureRelationships from Polypeptide
 *      features.
 *
 * @author rh11
 *
 */
@Entity
@FeatureType(cv="sequence", term="protein_match")
public class ProteinMatch extends Match {
    private static final Logger logger = Logger.getLogger(ProteinMatch.class);

    ProteinMatch() {
        // empty
    }

    public ProteinMatch(Organism organism, String uniqueName, boolean analysis, boolean obsolete) {
        super(organism, uniqueName, analysis, obsolete);
    }

    public ProteinMatch(Organism organism, String uniqueName) {
        super(organism, uniqueName);
    }

    /**
     * Get the subject feature of this similarity, which is a Region belonging to the dummy
     * organism, and has various bits of similarity metadata as FeatureProps
     * (with types <code>feature_property:organism</code>, <code>genedb_misc:product</code>
     * and <code>sequence:gene</code>). The primary reference is also stored as the primary
     * DbXRef of the subject feature, and any secondary references are related to it via
     * FeatureDbXRef.
     *
     * @return
     */
    public Region getSubject() {
        FeatureLoc featureLoc = this.getFeatureLoc(0, 1);
        if (featureLoc == null) {
            logger.error(String.format("Could not find subject featureloc for ProteinMatch '%s' (ID=%d)",
                getUniqueName(), getFeatureId()));
            return null;
        }
        Feature subjectFeature = featureLoc.getSourceFeature();
        if (subjectFeature == null) {
            logger.error(String.format("Could not find subject feature for ProteinMatch '%s' (ID=%d)",
                getUniqueName(), getFeatureId()));
            return null;
        }
        if (! (subjectFeature instanceof Region) ) {
            logger.error(String.format("Subject feature '%s' (ID=%d) for ProteinMatch '%s' (ID=%d) is %s, not Region",
                subjectFeature.getUniqueName(), subjectFeature.getFeatureId(), getUniqueName(), getFeatureId(), subjectFeature.getClass()));
            return null;
        }
        return (Region) subjectFeature;
    }

    /**
     * Get the query feature of this similarity, which is the polypeptide it's associated with.
     *
     * @return
     */
    public Polypeptide getPolypeptide() {
        FeatureLoc featureLoc = this.getFeatureLoc(0, 0);
        if (featureLoc == null) {
            logger.error(String.format("Could not find query featureloc for ProteinMatch '%s' (ID=%d)",
                getUniqueName(), getFeatureId()));
            return null;
        }
        Feature targetFeature = featureLoc.getSourceFeature();
        if (targetFeature == null) {
            logger.error(String.format("Could not find query feature for ProteinMatch '%s' (ID=%d)",
                getUniqueName(), getFeatureId()));
            return null;
        }
        if (! (targetFeature instanceof Polypeptide)) {
            logger.error(String.format("Query feature '%s' (ID=%d) for ProteinMatch '%s' (ID=%d) is %s, not Polypeptide",
                targetFeature.getUniqueName(), targetFeature.getFeatureId(), getUniqueName(), getFeatureId(), targetFeature.getClass()));
            return null;
        }
        return (Polypeptide) targetFeature;
    }

    /**
     * Add an orthologue link from the specified polypeptide to this cluster.
     * @param source the source polypeptide
     */
    public void addOrthologue(Polypeptide source) {
        if (! getFeatureLocs().isEmpty()) {
            throw new IllegalStateException("This ProteinMatch feature has FeatureLocs. " +
                    "Are you sure it represents an orthologue cluster?");
        }
        if (! getOrganism().getCommonName().equals("dummy")) {
            throw new IllegalStateException("This ProteinMatch feature does not belong to the 'dummy' organism." +
                    "Are you sure it represents an orthologue cluster?");
        }

        this.addFeatureRelationship(source, "sequence", "orthologous_to");
    }
}
