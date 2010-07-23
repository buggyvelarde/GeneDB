package org.gmod.schema.feature;


import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Organism;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;

import javax.persistence.Entity;

/**
 * SO:0000833 (but we don't use it directly, which is why it's an
 * abstract class).
 *
 * @author rh11
 */
@Entity
public abstract class TranscriptRegion extends Region {

    TranscriptRegion() {
        // empty
    }

    public TranscriptRegion(Organism organism, CvTerm cvTerm, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp timeAccessioned, Timestamp timeLastModified) {
        super(organism, cvTerm, uniqueName, analysis, obsolete, timeAccessioned, timeLastModified);
    }

    public TranscriptRegion(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    static <T extends TranscriptRegion> T construct(Class<T> regionClass, Organism organism, String uniqueName) {
        try {
            return regionClass.getConstructor(Organism.class, String.class).newInstance(organism, uniqueName);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Internal error: failed to instantiate region", e);
        } catch (SecurityException e) {
            throw new RuntimeException("Internal error: failed to instantiate region", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Internal error: failed to instantiate region", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Internal error: failed to instantiate region", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Internal error: failed to instantiate region", e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Internal error: failed to instantiate region", e);
        }
    }


}
