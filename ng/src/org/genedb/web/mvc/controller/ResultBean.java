package org.genedb.web.mvc.controller;

import org.gmod.schema.mapped.Organism;

import java.util.List;

public class ResultBean {

    private List<Organism> results;
    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<Organism> getResults() {
        return results;
    }

    public void setResults(List<Organism> results) {
        this.results = results;
    }
}
