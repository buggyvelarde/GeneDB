package org.gmod.schema.sequence;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.gmod.schema.cv.CvTerm;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

@Indexed
public class Exon extends Feature {
	
	@ManyToOne(cascade={})
    @JoinColumn(name="type_id", unique=false, nullable=false, insertable=true, updatable=true)
    @IndexedEmbedded(depth=2)
    private CvTerm cvTerm;

	public CvTerm getCvTerm() {
		return cvTerm;
	}

	public void setCvTerm(CvTerm cvTerm) {
		this.cvTerm = cvTerm;
	}
}
