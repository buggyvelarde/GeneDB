package org.genedb.web.mvc.controller;

public enum SequenceDestination {

    BLAST("Blast"), OMNIBLAST("OmniBlast"), NCBI_BLAST("NCBI");

    String destination;

    SequenceDestination(String destination) {
        this.destination = destination;
    }

}
