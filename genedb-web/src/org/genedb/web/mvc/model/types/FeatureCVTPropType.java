package org.genedb.web.mvc.model.types;

public class FeatureCVTPropType {
    private String fctTypeName;
    private String fctValue;
    
    public FeatureCVTPropType(String fctTypeName, String fctValue) {
        super();
        this.fctTypeName = fctTypeName;
        this.fctValue = fctValue;
    }
    
    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        
        sb.append("'");
        sb.append(fctTypeName);
        sb.append("',");

        sb.append("'");
        sb.append(fctValue);
        sb.append("'");
        
        sb.append(")");
        return sb.toString();
    }

}
