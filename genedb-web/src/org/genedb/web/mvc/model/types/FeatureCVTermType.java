package org.genedb.web.mvc.model.types;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.genedb.web.mvc.model.DbXRefDTO;
import org.genedb.web.mvc.model.FeatureCvTermDTO;


/**
 * Hand Made mapping class for Postgres composite-type featurecvtermtype
 * @author lo2@sangerinstitute
 *
 */
public class FeatureCVTermType{
    private String typeName;
    private String typeAccession;
    private String withFrom;
    private long count;
    private String[] pubs;
    private DBXRefType dbXRef[];
    private FeatureCVTPropType[] props;
                                       
                                       
    public FeatureCVTermType(FeatureCvTermDTO dto){
        typeName = dto.getTypeName();
        typeAccession = dto.getTypeAccession();
        withFrom = dto.getWithFrom();
        count = dto.getCount();
        pubs = dto.getPubs().toArray(new String[0]);
        
        initDBXRefType(dto.getDbXRefDtoList());
        initProps(dto.getProps());
    }
    
    private void initDBXRefType(List<DbXRefDTO> dtos){
        int i=0;
        dbXRef = new DBXRefType[dtos.size()];
        for(DbXRefDTO dto: dtos){
            dbXRef[i++] = new DBXRefType(dto);
        }
    }
    
    private void initProps(Map<String, Collection<String>> dtoProps){
        int i=0;
        props = new FeatureCVTPropType[dtoProps.size()];
        for(String propTypeName: dtoProps.keySet()){
            Collection<String> fctValues = dtoProps.get(propTypeName);
            props[i++] = new FeatureCVTPropType(propTypeName, fctValues.toArray(new String[0]));
        }
    }
    
    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        
//        sb.append("'");
//        sb.append(formatStr(typeName));
//        sb.append("',");
//        
//        sb.append("'");
//        sb.append(formatStr(typeAccession));
//        sb.append("',");
//        
//        sb.append("'");
//        sb.append(formatStr(withFrom));
//        sb.append("',");
//        
//        sb.append(count);
//        sb.append(",");
        
//        //pubs
//        sb.append("{");
//        for(int i=0; i<pubs.length; ++i){   
//            sb.append("\"");
//            sb.append(pubs[i]);            
//            sb.append("\"");                        
//            if (i+1<pubs.length){
//                sb.append(",");
//            }
//        }
//        if(pubs.length==0){
//            //sb.append("\"\"");
//        }
//        sb.append("},");
        
        //dbXref
        sb.append("{");
        for(int i=0; i<dbXRef.length; ++i){ 
            sb.append("\"");
            sb.append(dbXRef[i]);     
            sb.append("\"");            
            if (i+1<dbXRef.length){
                sb.append(",");
            }
        }
        if(dbXRef.length==0){
            //sb.append("\"\"");
        }
        sb.append("}");
//        
//        //props
//        sb.append("{");
//        for(int i=0; i<props.length; ++i){  
//            sb.append("\"");
//            sb.append(props[i]);  
//            sb.append("\"");                 
//            if (i+1<props.length){
//                sb.append(",");
//            }
//        }
//        if(props.length==0){
//            sb.append("\"\"");
//        }
//        sb.append("}");
//        
        sb.append(")");
        return sb.toString();
    }
    
    private String formatStr(String value){
        if (StringUtils.isEmpty(value) || "null".equalsIgnoreCase(value)){
            return "";  
        }
        value = value.replaceAll(",", "\\\\\\\\,");        
        value = value.replaceAll("\\(", "\\\\\\\\(");
        value = value.replaceAll("\\)", "\\\\\\\\)");
        return value;
    }
}
