package org.genedb.db.loading;

public class ControlledCurationInstance {

	private String term;
	
	private String dbXRef;
	
	private String date;
	
	private String cv;
	
	private String qualifier;
	
	private String evidence;
	
	private String residue;
	
	private String attribution;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDbXRef() {
		return dbXRef;
	}

	public void setDbXRef(String dbXRef) {
		this.dbXRef = dbXRef;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getAttribution() {
		return attribution;
	}

	public void setAttribution(String attribution) {
		this.attribution = attribution;
	}

	public String getCv() {
		return cv;
	}

	public void setCv(String cv) {
		this.cv = cv;
	}

	public String getEvidence() {
		return evidence;
	}

	public void setEvidence(String evidence) {
		this.evidence = evidence;
	}

	public String getQualifier() {
		return qualifier;
	}

	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

	public String getResidue() {
		return residue;
	}

	public void setResidue(String residue) {
		this.residue = residue;
	}
}
