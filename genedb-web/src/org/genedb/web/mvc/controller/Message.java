package org.genedb.web.mvc.controller;

public class Message {

    private String chr;

    private String feature;
    
    private String start;
    
    private String stop;
    
    private String strand;
    
    private String attributes;

    public String getAttribute() {
        return attributes;
    }

    public void setAttribute(String attributes) {
        this.attributes = attributes;
    }

    public String getChr() {
        return chr;
    }

    public void setChr(String chr) {
        this.chr = chr;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getStop() {
        return stop;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }
    
}
