package org.genedb.web.mvc.controller;

public class FastaFile {
    
    private String fileName;
    private String description;
    private boolean indexedForBlast;
    private SequenceType sequenceType;
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public boolean isIndexedForBlast() {
        return indexedForBlast;
    }
    public void setIndexedForBlast(boolean indexedForBlast) {
        this.indexedForBlast = indexedForBlast;
    }
    public SequenceType getSequenceType() {
        return sequenceType;
    }
    public void setSequenceType(SequenceType sequenceType) {
        this.sequenceType = sequenceType;
    }
    

}
