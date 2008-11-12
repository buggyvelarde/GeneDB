package org.genedb.pipeline.domain;

public enum Phase {
	ZERO, ONE, TWO;
//	For features of type "CDS", the phase indicates where the feature
//	begins with reference to the reading frame.  The phase is one of the
//	integers 0, 1, or 2, indicating the number of bases that should be
//	removed from the beginning of this feature to reach the first base of
//	the next codon. In other words, a phase of "0" indicates that the next
//	codon begins at the first base of the region described by the current
//	line, a phase of "1" indicates that the next codon begins at the
//	second base of this region, and a phase of "2" indicates that the
//	codon begins at the third base of this region. This is NOT to be
//	confused with the frame, which is simply start modulo 3.
}
