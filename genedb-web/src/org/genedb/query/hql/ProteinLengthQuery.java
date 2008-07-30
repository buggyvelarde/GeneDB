package org.genedb.query.hql;

import org.genedb.query.core.HqlQuery;
import org.genedb.query.core.QueryClass;
import org.genedb.query.core.QueryParam;

@QueryClass(
		title="Coding and pseudogenes by protein length",
		shortDesc="Get a list of transcripts ",
		longDesc=""
	)
public class ProteinLengthQuery extends HqlQuery {

	@QueryParam(
			order=1,
			title="Minimum length of protein in bases"
	)
	private int min;

	@QueryParam(
			order=2,
			title="Maximum length of protein in bases"
	)
	private int max;

	@Override
	protected String getHql() {
		return "f.transcript.featureId from feature f where f.residues.length >= :min and f.residues.length <= :max";
	}

	public void setMinProteinLength(int min) {
		this.min = min;
	}
	
	public void setMaxProteinLength(int max) {
		this.max = max;
	}
}
