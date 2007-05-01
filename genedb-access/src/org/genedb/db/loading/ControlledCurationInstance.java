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

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((attribution == null) ? 0 : attribution.hashCode());
		result = PRIME * result + ((cv == null) ? 0 : cv.hashCode());
		result = PRIME * result + ((dbXRef == null) ? 0 : dbXRef.hashCode());
		result = PRIME * result + ((evidence == null) ? 0 : evidence.hashCode());
		result = PRIME * result + ((qualifier == null) ? 0 : qualifier.hashCode());
		result = PRIME * result + ((residue == null) ? 0 : residue.hashCode());
		result = PRIME * result + ((term == null) ? 0 : term.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ControlledCurationInstance other = (ControlledCurationInstance) obj;
		if (attribution == null) {
			if (other.attribution != null)
				return false;
		} else if (!attribution.equals(other.attribution))
			return false;
		if (cv == null) {
			if (other.cv != null)
				return false;
		} else if (!cv.equals(other.cv))
			return false;
		if (dbXRef == null) {
			if (other.dbXRef != null)
				return false;
		} else if (!dbXRef.equals(other.dbXRef))
			return false;
		if (evidence == null) {
			if (other.evidence != null)
				return false;
		} else if (!evidence.equals(other.evidence))
			return false;
		if (qualifier == null) {
			if (other.qualifier != null)
				return false;
		} else if (!qualifier.equals(other.qualifier))
			return false;
		if (residue == null) {
			if (other.residue != null)
				return false;
		} else if (!residue.equals(other.residue))
			return false;
		if (term == null) {
			if (other.term != null)
				return false;
		} else if (!term.equals(other.term))
			return false;
		return true;
	}
}
