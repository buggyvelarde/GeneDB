package org.genedb.web.mvc.model.types;

public class FeatureCVTPropType {
    private String fctTypeName;
    private String[] fctValues;
    
    public FeatureCVTPropType(String fctTypeName, String[] fctValues) {
        super();
        this.fctTypeName = fctTypeName;
        this.fctValues = fctValues;
    }
    
    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        
        sb.append("'");
        sb.append(fctTypeName);
        sb.append("',");
        
        sb.append("{");
        for(int i=0; i<fctValues.length; ++i){   
            sb.append("\"");
            sb.append(fctValues[i]);            
            sb.append("\"");                        
            if (i+1<fctValues.length){
                sb.append(",");
            }
        }
        sb.append("}");
        
        sb.append(")");
        return sb.toString();
    }

}
