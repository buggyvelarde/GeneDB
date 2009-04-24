package org.genedb.web.mvc.controller;

public enum SequenceType {
    UNSPLICED("Unspliced"), SPLICED("Spliced"), PROTEIN("Protein");

    String type;

    private SequenceType(String type) {
        this.type = type;
    }

}
