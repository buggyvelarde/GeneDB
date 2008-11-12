package org.genedb.pipeline.io;

import java.util.List;

import org.genedb.pipeline.domain.Embl;
import org.springframework.batch.item.file.FlatFileItemWriter;

public class EmblItemWriter extends FlatFileItemWriter<Embl> {

	@Override
	public void write(List<? extends Embl> entries) throws Exception {
		for (Embl entry : entries) {
			write(entry);
		}
	}

	private void write(Embl entry) {
		// TODO Auto-generated method stub
	}
}
