package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;

import javax.persistence.Entity;

@SuppressWarnings("serial")
@Entity
@FeatureType(cv="sequence", term="repeat_region")
public class RepeatRegion extends Region {

    RepeatRegion() {
        // empty
    }

    public RepeatRegion(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    public RepeatRegion(Organism organism, String uniqueName, String name) {
        this(organism, uniqueName, false, false, new Timestamp(System.currentTimeMillis()));
        this.setName(name);
    }

    public static <T extends RepeatRegion> T make(Class<T> repeatRegionClass, Organism organism, String uniqueName, String name) {
        try {
            return repeatRegionClass.getConstructor(Organism.class, String.class, String.class).newInstance(organism, uniqueName, name);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to construct repeat region", e);
        } catch (SecurityException e) {
            throw new RuntimeException("Failed to construct repeat region", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Failed to construct repeat region", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to construct repeat region", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Failed to construct repeat region", e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to construct repeat region", e);
        }
    }
}
