package org.genedb.db.loading;

public class ParsedString {
    private String main;
    private String extract;
    
    public ParsedString(String main, String extract) {
	this.main = main;
	this.extract = extract;
    }

    public String getExtract() {
        return this.extract;
    }

    public String getMain() {
        return this.main;
    }
    
    public boolean isSplit() {
	return extract != null;
    }
}
