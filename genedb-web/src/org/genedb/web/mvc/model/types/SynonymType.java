package org.genedb.web.mvc.model.types;

import org.apache.commons.lang.StringUtils;

public class SynonymType {

    private String synonymName;
    private String cvtName;
    private boolean isCurrent;
    
    public String getSynonymName() {
        return synonymName;
    }

    public void setSynonymName(String synonymName) {
        this.synonymName = synonymName;
    }

    public String getCvtName() {
        return cvtName;
    }

    public void setCvtName(String cvtName) {
        this.cvtName = cvtName;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    /**
     * Re-format the synonym type name
     * @param rawName
     * @return
     */
    private String formatSynonymTypeName(String rawName){

        char formattedName[] = rawName.toCharArray();
        for(int i=0; i<formattedName.length; ++i){

            //Replace underscores with spaces
            if (formattedName[i]=='_'){
                formattedName[i] = ' ';

            //Replace first char lowercase to a uppercase char
            }else if(i==0 && Character.isLowerCase(formattedName[i])){
                formattedName[i] = Character.toUpperCase(formattedName[i]);

            //Replace any occurrence of a lowercase char preceeded a space with a upper case char
            }else if(i>0 && formattedName[i-1]==' ' && Character.isLowerCase(formattedName[i])){
                formattedName[i] = Character.toUpperCase(formattedName[i]);
            }
        }
        return String.valueOf(formattedName).trim();
    }
    
    
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        
        sb.append("'");
        sb.append(formatSynonymTypeName(formatStr(cvtName)));
        sb.append("',");
        
        sb.append("'");
        sb.append(formatStr(synonymName));
        sb.append("',");
        
        sb.append(isCurrent);
        
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
