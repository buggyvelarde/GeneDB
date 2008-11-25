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
     * Get the query feature of this similarity.
     *
     * @return
     */
    public Region getQuery() {
        FeatureLoc featureLoc = this.getFeatureLoc(0, 0);
        if (featureLoc == null) {
            logger.error(String.format("Could not find query featureloc for ProteinMatch '%s' (ID=%d)",
                getUniqueName(), getFeatureId()));
            return null;
        }
        Feature queryFeature = featureLoc.getSourceFeature();
        if (queryFeature == null) {
            logger.error(String.format("Could not find query feature for ProteinMatch '%s' (ID=%d)",
                getUniqueName(), getFeatureId()));
            return null;
        }
        if (! (queryFeature instanceof Region) ) {
            logger.error(String.format("Query feature '%s' (ID=%d) for ProteinMatch '%s' (ID=%d) is %s, not Region",
                queryFeature.getUniqueName(), queryFeature.getFeatureId(), getUniqueName(), getFeatureId(), queryFeature.getClass()));
            return null;
        }
        return (Region) queryFeature;
    }

    /**
     * Get the target feature of this similarity.
     *
     * @return
     */
    public Polypeptide getTarget() {
        FeatureLoc featureLoc = this.getFeatureLoc(0, 1);
        if (featureLoc == null) {
            logger.error(String.format("Could not find target featureloc for ProteinMatch '%s' (ID=%d)",
                getUniqueName(), getFeatureId()));
            return null;
        }
        Feature targetFeature = featureLoc.getSourceFeature();
        if (targetFeature == null) {
            logger.error(String.format("Could not find target feature for ProteinMatch '%s' (ID=%d)",
                getUniqueName(), getFeatureId()));
            return null;
        }
        if (! (targetFeature instanceof Polypeptide)) {
            logger.error(String.format("Target feature '%s' (ID=%d) for ProteinMatch '%s' (ID=%d) is %s, not Polypeptide",
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
