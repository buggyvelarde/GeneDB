package org.genedb.db.loading;

public interface GeneNamingStrategy {
    
    String getGene(String systematicId);
    
    String getTranscript(String systematicId, int transcriptNum);
    
    String getExon(String systematicId, int transcriptNum, int exonNum);
    
    String getPolypeptide(String systematicId, int transcriptNum);

    String get3pUtr(String systematicId, int transcriptNum);
    
    String get5pUtr(String systematicId, int transcriptNum);
    
}
