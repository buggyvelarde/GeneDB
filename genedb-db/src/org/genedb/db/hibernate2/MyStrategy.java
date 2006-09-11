package org.genedb.db.hibernate2;

import org.hibernate.cfg.reveng.DelegatingReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.ReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.TableIdentifier;

public class MyStrategy extends DelegatingReverseEngineeringStrategy {

    private static final String DEF_PKG = "org.gmod.schema.";
    
    public MyStrategy(ReverseEngineeringStrategy delegate) {
	super(delegate);
    }
    
    @Override
    public String columnToPropertyName(TableIdentifier table, String column) {
	column = munge(column);
	return super.columnToPropertyName(table, column);
    }
    
    @Override
    public String tableToClassName(TableIdentifier ti) {
	return DEF_PKG+munge(ti.getName());
    }
    
    public String munge(String input) {
        String in = input;
	while (in.contains("_")) {
	    StringBuffer ret = new StringBuffer();
	    int index = in.indexOf("_");
	    ret.append(in.substring(0, index));
	    ret.append(in.substring(index+1, index+2).toUpperCase());
	    ret.append(in.substring(index+2));
	    in = ret.toString();
	}	
	in = in.substring(0,1).toUpperCase()+in.substring(1);
	in = in.replace("Cvterm", "CvTerm");
	in = in.replace("Dbxref", "DbXRef");
	in = in.replace("Studydesign", "StudyDesign");
	in = in.replace("Arraydesign", "ArrayDesign");
	in = in.replace("Elementresult", "ElementResult");
	in = in.replace("Mageml", "MageML");
	in = in.replace("Gcontext", "GContext");
	in = in.replace("feature", "Feature");
	in = in.replace("synonym", "Synonym");
	//in = in.replace("xref","XRef");
	in = in.replace("author", "Author");
	in = in.replace("propPub", "PropPub");
	if (in.length()>4 && in.endsWith("prop")) {
	    in = in.substring(0, in.length()-4)+"Prop";
	}
	if (in.length()>4 && in.endsWith("path")) {
	    in = in.substring(0, in.length()-4)+"Path";
	}
	if (in.startsWith("Feature") && in.length()>"Feature".length()) {
	    StringBuffer tmp = new StringBuffer("Feature");
	    int index = "Feature".length()-1;
	    tmp.append(in.substring(index+1, index+2).toUpperCase());
	    tmp.append(in.substring(index+2));
	    in = tmp.toString();
	}
	return in;
    }

}
