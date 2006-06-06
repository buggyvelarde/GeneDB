package org.genedb.db.loading;

import java.util.Map;

public class FeaturePart extends BasePart implements Part {
    
    private String name;
    private String soType;
    private Map<String,String> properties;
    private short strand;
    
    public short getStrand() {
        return this.strand;
    }

    public void setStrand(short strand) {
        this.strand = strand;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getSoType() {
        return this.soType;
    }

    public void setSoType(String soType) {
        this.soType = soType;
    }
}
