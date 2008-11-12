package org.genedb.pipeline.domain;

public enum Strand {
	POSITIVE, NEGATIVE, NONE, UNKNOWN;
}
//
//The strand of the feature.  + for positive strand (relative to the
//landmark), - for minus strand, and . for features that are not
//stranded.  In addition, ? can be used for features whose strandedness
//is relevant, but unknown.
//