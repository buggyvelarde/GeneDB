package org.gmod.schema.feature;

import org.gmod.schema.mapped.Organism;

import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;

import javax.persistence.Entity;

/**
 * A class representing a feature that may be a top level feature. (It could be an interface but
 * fits nicely into the hierarchy here so isn't)
 *
 *  It provides one method which indicates whether the given feature is actually a top-level feature.
 *  This helps distinguish cases eg where the project is (a) in contigs, or (b) in chromosomes but with
 *  contig features attached for tracking purposes.
 *
 * @author rh11
 */
@Entity
public abstract class TopLevelFeature extends Region {

    private static final Logger logger = Logger.getLogger(TopLevelFeature.class);

    TopLevelFeature() {
        // empty
    }

    public TopLevelFeature(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    public static <T extends TopLevelFeature> T make(Class<T> featureClass, String uniqueName, Organism organism) {
        try {
            return featureClass.getConstructor(Organism.class, String.class, Boolean.TYPE, Boolean.TYPE, Timestamp.class)
                .newInstance(organism, uniqueName, false, false, new Timestamp(System.currentTimeMillis()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Is this feature acting as a top-level feature in this case? Checks presence of
     * top_level_feature FeatureProp
     *
     * @return <code>true</code> if acting as a top-level feature ie a primary location reference
     */
    public boolean isTopLevelFeature() {
        return hasProperty("genedb_misc", "top_level_seq");
    }

    public void markAsTopLevelFeature() {
        addFeatureProp("true", "genedb_misc", "top_level_seq", 0);
    }

    public Gap addGap(int fmin, int fmax) {
        Gap gap = new Gap(getOrganism(), String.format("%s:gap:%d-%d", getUniqueName(), fmin, fmax));
        this.addLocatedChild(gap, fmin, fmax, (short)0, 0);
        return gap;
    }

    /**
     * Delete this feature. If it's a top-level feature
     * (i.e. if {@link #isTopLevelFeature()} returns <code>true</code>)
     * then also delete all features located on this feature.
     */
    @Override
    public void delete() {
        delete(isTopLevelFeature());
    }

    /**
     * Delete this feature. If the parameter <code>deleteLocatedFeatures</code>
     * is true, also delete all features located on this feature.
     *
     * @param deleteLocatedFeatures
     */
    public void delete(boolean deleteLocatedFeatures) {
        logger.trace(String.format("Deleting top-level feature '%s' (%s)",
            getUniqueName(),
            deleteLocatedFeatures ? "and sublocated features" : "but not sublocated features"));

        if (deleteLocatedFeatures) {
            sequenceDao.deleteFeaturesLocatedOn(this);
        }
        super.delete();
    }
}
