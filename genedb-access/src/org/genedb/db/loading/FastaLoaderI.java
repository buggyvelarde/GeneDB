package org.genedb.db.loading;

public interface FastaLoaderI {

    public void setOrganismName(String organismName);

    public void setDataDirectory(String dataDirectory);

    public void process();

}
