package org.gmod.schema.sequence.feature;

import org.gmod.schema.cfg.FeatureType;

import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", term="pseudogenic_transcript")
@Indexed
public class PseudogenicTranscript extends ProductiveTranscript {}
