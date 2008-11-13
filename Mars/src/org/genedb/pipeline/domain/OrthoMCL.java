package org.genedb.pipeline.domain;

public class OrthoMCL {

	private String clusterId;
	private String[] orthologueIds;



    public OrthoMCL(String clusterId, String[] orthologueIds) {
        this.clusterId = clusterId;
        this.orthologueIds = orthologueIds;
    }

    public String getClusterId() {
        return clusterId;
    }
    public String[] getOrthologueIds() {
        return orthologueIds;
    }



}
