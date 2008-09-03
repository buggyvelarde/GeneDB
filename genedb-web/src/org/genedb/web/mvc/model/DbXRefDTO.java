package org.genedb.web.mvc.model;

public class DbXRefDTO {
    String dbName;
    String accession;
    String urlPrefix;

    public DbXRefDTO(String dbName, String accession, String urlPrefix) {
        this.dbName = dbName;
        this.accession = accession;
        this.urlPrefix = urlPrefix;
    }

    public String getDbName() {
        return dbName;
    }

    public String getAccession() {
        return accession;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }

}
