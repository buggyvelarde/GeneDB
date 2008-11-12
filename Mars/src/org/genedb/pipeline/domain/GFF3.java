package org.genedb.pipeline.domain;

import java.util.List;
import java.util.Map;

public class GFF3 {
//	Column 1: "seqid"
//
//		The ID of the landmark used to establish the coordinate system for the
//		current feature. IDs may contain any characters, but must escape any
//		characters not in the set [a-zA-Z0-9.:^*$@!+_?-|].  In particular, IDs
//		may not contain unescaped whitespace and must not begin with an
//		unescaped ">".
//
//		Column 2: "source"
//
//		The source is a free text qualifier intended to describe the algorithm
//		or operating procedure that generated this feature.  Typically this is
//		the name of a piece of software, such as "Genescan" or a database
//		name, such as "Genbank."  In effect, the source is used to extend the
//		feature ontology by adding a qualifier to the type creating a new
//		composite type that is a subclass of the type in the type column.
//
//		Column 3: "type"
//
//		The type of the feature (previously called the "method").  This is
//		constrained to be either: (a) a term from the "lite" sequence
//		ontology, SOFA; or (b) a SOFA accession number.  The latter
//		alternative is distinguished using the syntax SO:000000.
//
//		Columns 4 & 5: "start" and "end"
//
//		The start and end of the feature, in 1-based integer coordinates,
//		relative to the landmark given in column 1.  Start is always less than
//		or equal to end.
//
//		For zero-length features, such as insertion sites, start equals end
//		and the implied site is to the right of the indicated base in the
//		direction of the landmark.
//
//		Column 6: "score"
//
//		The score of the feature, a floating point number.  As in earlier
//		versions of the format, the semantics of the score are ill-defined.
//		It is strongly recommended that E-values be used for sequence
//		similarity features, and that P-values be used for ab initio gene
//		prediction features.
//
//		Column 7: "strand"
//
//		The strand of the feature.  + for positive strand (relative to the
//		landmark), - for minus strand, and . for features that are not
//		stranded.  In addition, ? can be used for features whose strandedness
//		is relevant, but unknown.
//
//		Column 8: "phase"
//
//		For features of type "CDS", the phase indicates where the feature
//		begins with reference to the reading frame.  The phase is one of the
//		integers 0, 1, or 2, indicating the number of bases that should be
//		removed from the beginning of this feature to reach the first base of
//		the next codon. In other words, a phase of "0" indicates that the next
//		codon begins at the first base of the region described by the current
//		line, a phase of "1" indicates that the next codon begins at the
//		second base of this region, and a phase of "2" indicates that the
//		codon begins at the third base of this region. This is NOT to be
//		confused with the frame, which is simply start modulo 3.
//
//		For forward strand features, phase is counted from the start
//		field. For reverse strand features, phase is counted from the end
//		field.
//
//		The phase is REQUIRED for all CDS features.
//
//		Column 9: "attributes"
//
//		A list of feature attributes in the format tag=value.  Multiple
//		tag=value pairs are separated by semicolons.  URL escaping rules are
//		used for tags or values containing the following characters: ",=;".
//		Spaces are allowed in this field, but tabs must be replaced with the
//		%09 URL escape.
//
//		These tags have predefined meanings:
//
//		    ID	   Indicates the name of the feature.  IDs must be unique
//			   within the scope of the GFF file.
//
//		    Name   Display name for the feature.  This is the name to be
//		           displayed to the user.  Unlike IDs, there is no requirement
//			   that the Name be unique within the file.
//
//		    Alias  A secondary name for the feature.  It is suggested that
//			   this tag be used whenever a secondary identifier for the
//			   feature is needed, such as locus names and
//			   accession numbers.  Unlike ID, there is no requirement
//			   that Alias be unique within the file.
//
//		    Parent Indicates the parent of the feature.  A parent ID can be
//			   used to group exons into transcripts, transcripts into
//			   genes, an so forth.  A feature may have multiple parents.
//			   Parent can *only* be used to indicate a partof 
//			   relationship.
//
//		    Target Indicates the target of a nucleotide-to-nucleotide or
//			   protein-to-nucleotide alignment.  The format of the
//			   value is "target_id start end [strand]", where strand
//			   is optional and may be "+" or "-".  If the target_id 
//			   contains spaces, they must be escaped as hex escape %20.
//
//		    Gap   The alignment of the feature to the target if the two are
//		          not collinear (e.g. contain gaps).  The alignment format is
//			  taken from the CIGAR format described in the 
//			  Exonerate documentation.
//			  (http://cvsweb.sanger.ac.uk/cgi-bin/cvsweb.cgi/exonerate
//		          ?cvsroot=Ensembl).  See "THE GAP ATTRIBUTE" for a description
//			  of this format.
//
//		    Derives_from  
//		          Used to disambiguate the relationship between one
//		          feature and another when the relationship is a temporal
//		          one rather than a purely structural "part of" one.  This
//		          is needed for polycistronic genes.  See "PATHOLOGICAL CASES"
//			  for further discussion.
//
//		    Note   A free text note.
//
//		    Dbxref A database cross reference.  See the section
//			   "Ontology Associations and Db Cross References" for
//			   details on the format.
//
//		    Ontology_term  A cross reference to an ontology term.  See
//		           the section "Ontology Associations and Db Cross References"
//			   for details.
//
//		Multiple attributes of the same type are indicated by separating the
//		values with the comma "," character, as in:
//
//		       Parent=AF2312,AB2812,abc-3
//
//		Note that attribute names are case sensitive.  "Parent" is not the
//		same as "parent".
//
//		All attributes that begin with an uppercase letter are reserved for
//		later use.  Attributes that begin with a lowercase letter can be used
//		freely by applications.
	
	String seqId; // "seqid"
	String source; // "source"
	String type; // "type"
	int start;
	int end; //  Columns 4 & 5: "start" and "end"
	double score; // "score"
	Strand strand; // "strand"
	Phase phase; // "phase"
	Map<String, List<String>> attributes; //  "attributes"
	
	public String getSeqId() {
		return seqId;
	}
	
	public void setSeqId(String seqId) {
		this.seqId = seqId;
	}
	
	public String getSource() {
		return source;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public int getStart() {
		return start;
	}
	
	public void setStart(int start) {
		this.start = start;
	}
	
	public int getEnd() {
		return end;
	}
	
	public void setEnd(int end) {
		this.end = end;
	}
	
	public double getScore() {
		return score;
	}
	
	public void setScore(double score) {
		this.score = score;
	}
	
	public Strand getStrand() {
		return strand;
	}
	
	public void setStrand(Strand strand) {
		this.strand = strand;
	}
	
	public Phase getPhase() {
		return phase;
	}
	
	public void setPhase(Phase phase) {
		this.phase = phase;
	}
	
	public Map<String, List<String>> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(Map<String, List<String>> attributes) {
		this.attributes = attributes;
	}
	
}