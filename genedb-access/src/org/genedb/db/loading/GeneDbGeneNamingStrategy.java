package org.genedb.db.loading;

public class GeneDbGeneNamingStrategy implements GeneNamingStrategy {

    private static final String SEP = ":";
    
    public String get3pUtr(String systematicId, int transcriptNum) {
	return joinNames(systematicId, "3UTR");
//	return joinNames(systematicId, "3UTR", transcriptNum);
    }

    public String get5pUtr(String systematicId, int transcriptNum) {
	return joinNames(systematicId, "5UTR");
//	return joinNames(systematicId, "5UTR", transcriptNum);
    }

    public String getExon(String systematicId, int transcriptNum, int exonNum) {
	// Ignore transcriptNum as the exons may be shared
	return joinNames(systematicId, "exon", exonNum);
    }

    public String getGene(String systematicId) {
	return systematicId;
    }

    public String getPolypeptide(String systematicId, int transcriptNum) {
	return joinNames(systematicId, "pep");
	//return joinNames(systematicId, "pep", transcriptNum);
    }

    public String getTranscript(String systematicId, int transcriptNum) {
	return joinNames(systematicId, "mRNA");	
//	return joinNames(systematicId, "mRNA", transcriptNum);
    }
    
    
   private String joinNames(String sysId, Object... extras) {
       StringBuilder sb = new StringBuilder(sysId);
       for (Object extra : extras) {
	   sb.append(SEP);
	   sb.append(extra);
       }
       return sb.toString();
   }

}
