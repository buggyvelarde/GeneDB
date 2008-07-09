package org.gmod.schema.sequence.feature;

import javax.persistence.Entity;

import org.gmod.schema.cfg.FeatureType;

/**
 * A SECIS element.
 *
 * @author rh11
 */
@Entity
@FeatureType(cv="sequence", term="SECIS_element")
public class SECISElement extends TranscriptRegion {}
