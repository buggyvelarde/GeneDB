package org.genedb.web.applications.motifsearch;

public class FastaEntry {

    public FastaEntry(String header) {
        this.header = header;
    }

    private String header;

    private String sequence;

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    

}
