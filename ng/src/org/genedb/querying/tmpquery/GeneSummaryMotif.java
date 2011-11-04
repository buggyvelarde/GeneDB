package org.genedb.querying.tmpquery;

public class GeneSummaryMotif extends GeneSummary {
	
	String match;
	String residues;
	int start;
	int end;
	
	public String getMatch() {
		return match;
	}
	public void setMatch(String match) {
		this.match = match;
	}
	public String getResidues() {
		return residues;
	}
	public void setResidues(String residues) {
		this.residues = residues;
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
	
	public GeneSummaryMotif(String displayId, String systematicId,
			String taxonDisplayName, String product,
			String topLevelFeatureName, int left, int start, int end, String match, String residues) {
		super(displayId, systematicId, taxonDisplayName, product,
				topLevelFeatureName, left);
		this.start = start;
		this.end = end;
		this.match = match;
		this.residues = residues;
	}

}
