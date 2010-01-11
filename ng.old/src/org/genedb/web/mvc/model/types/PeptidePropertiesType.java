package org.genedb.web.mvc.model.types;

import org.gmod.schema.utils.PeptideProperties;
import org.postgresql.util.PGobject;

public class PeptidePropertiesType extends PGobject{

    private String massInDaltons;
    private String aminoAcids;
    private String isoelectricPoint;
    private String charge;
    
    public PeptidePropertiesType(PeptideProperties peptideProperties){
        setType("peptidepropertiestype");
        
        massInDaltons = peptideProperties.getMass();
        aminoAcids = String.valueOf(peptideProperties.getAminoAcids());
        isoelectricPoint = peptideProperties.getIsoelectricPoint();
        charge = peptideProperties.getCharge();
    }
    
    @Override
    public String getValue(){
        return toString();
    }
    
    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        
        sb.append("'");
        sb.append(massInDaltons!=null?massInDaltons:"");
        sb.append("',");
        
        sb.append("'");
        sb.append(aminoAcids!=null?aminoAcids:"");
        sb.append("',");
        
        sb.append("'");
        sb.append(isoelectricPoint!=null?isoelectricPoint:"");
        sb.append("',");
        
        sb.append("'");
        sb.append(charge!=null?charge:"");
        sb.append("'");
        
        sb.append(")");
        return sb.toString();
    }

}
