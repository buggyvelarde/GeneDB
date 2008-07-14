package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;

import javax.persistence.Entity;

/**
 * A SECIS element.
 *
 * @author rh11
 */
@Entity
@FeatureType(cv="sequence", term="SECIS_element")
public class SECISElement extends TranscriptRegion {}
