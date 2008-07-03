package org.gmod.schema.sequence.feature;

import javax.persistence.Entity;

import org.gmod.schema.cfg.FeatureType;
import org.hibernate.search.annotations.Indexed;

@Entity
@FeatureType(cv="sequence", term="pseudogenic_transcript")
@Indexed
public class PseudogenicTranscript extends ProductiveTranscript {}
