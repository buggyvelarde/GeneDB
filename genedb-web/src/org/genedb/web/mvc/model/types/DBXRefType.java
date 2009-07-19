package org.genedb.web.mvc.model.types;

import org.genedb.web.mvc.model.DbXRefDTO;

public class DBXRefType {
    private String accession;
    private String dbName;
    private String urlPrefix;
    
    public DBXRefType(DbXRefDTO dto) {
        accession = dto.getAccession();
        dbName = dto.getDbName();
        urlPrefix = dto.getUrlPrefix();
    }
    
    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        
        sb.append("'");
        sb.append(accession!=null?accession:"");
        sb.append("',");
        
        sb.append("'");
        sb.append(dbName!=null?dbName:"");
        sb.append("',");
        
        sb.append("'");
        sb.append(urlPrefix!=null?urlPrefix:"");
        sb.append("'");
        
        sb.append(")");
        return sb.toString();
    }
}
