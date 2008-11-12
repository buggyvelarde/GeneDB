package org.genedb.pipeline.io;

import org.genedb.pipeline.domain.Fasta;
import org.springframework.batch.item.file.transform.LineAggregator;

public class FastaLineAggregator implements LineAggregator<Fasta> {

	@Override
	public String aggregate(Fasta fasta) {
		StringBuilder sb = new StringBuilder();
		sb.append('>');
		sb.append(fasta.getHeader());
		sb.append(System.getProperty("line.separator"));
		// FIXME 60 chars
		sb.append(fasta.getSequence());
		return sb.toString();
	}

}
