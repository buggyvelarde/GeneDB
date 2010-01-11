package org.genedb.web.mvc.model.types;

import org.genedb.db.domain.objects.InterProHit;
import org.genedb.db.domain.objects.SimpleRegionGroup;

public class PepRegionGroupType {
    private int fmin;
    private int fmax;
    private String description;
    private String score;    
    
    public PepRegionGroupType(InterProHit regionGroup){
        fmin = regionGroup.getFmin();
        fmax = regionGroup.getFmax();
        description = regionGroup.getDescription();
    } 
    
    public PepRegionGroupType(SimpleRegionGroup regionGroup){
        fmin = regionGroup.getFmin();
        fmax = regionGroup.getFmax();
        description = regionGroup.getDescription();
    }
    
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        
        sb.append("'");
        sb.append(fmin);
        sb.append("',");
        
        sb.append("'");
        sb.append(fmax);
        sb.append("',");
        
        sb.append("'");
        sb.append(description!=null?description:"");
        sb.append("'");
        
//        sb.append("'");
//        sb.append(score!=null?score:"");
//        sb.append("'");
        
        sb.append(")");
        return sb.toString();
    }
}
