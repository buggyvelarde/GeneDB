package org.genedb.web.mvc.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GeneDTO extends TranscriptDTO implements Serializable  {
	
	List<TranscriptDTO> transcripts = new ArrayList<TranscriptDTO>();
	
	public List<TranscriptDTO> getTranscripts() {
		return transcripts;
	}
	
	public void setTranscripts(List<TranscriptDTO> transcripts) {
		this.transcripts=transcripts;
	}
	
}
