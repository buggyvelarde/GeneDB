package org.genedb.arttb.sqlt_hibernate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.genedb.arttb.sqlt_hibernate.rdbms.Database;
import org.xml.sax.SAXException;

public class Munger {
	private static final String BASE_PKG="org.genedb.arttb.sqlt_hibernate.gen";
	
	private Map<String, String> tablePackage;
	
	public Munger() {
		InputStream is;
		Properties props = new Properties();
		try {
			is = new FileInputStream(new File("resources/munger.properties"));
			props.load(is);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		tablePackage = new HashMap<String, String>();
		for (Iterator it = props.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
			if (entry.getKey().startsWith("package")) {
				String pkg = entry.getKey().substring("package.".length());
				String[] tables = entry.getValue().split(",");
				for (int i = 0; i < tables.length; i++) {
					tablePackage.put(tables[i].trim(), pkg);
				}
			}
			it.remove();
		}
	}

	public String munge(String in, String tableName) {
		if (in.equals(tableName+"_id")) {
			return "id";
		}
		if (in.length()>3 && in.endsWith("_id")) {
			in = in.substring(0, in.length()-3);
		}
		while (in.contains("_")) {
			StringBuffer ret = new StringBuffer();
			int index = in.indexOf("_");
			ret.append(in.substring(0, index));
			ret.append(in.substring(index+1, index+2).toUpperCase());
			ret.append(in.substring(index+2));
			in = ret.toString();
		}
		in = in.replace("Cvterm", "CvTerm");
		in = in.replace("Dbxref", "DbXRef");
		in = in.replace("Studydesign", "StudyDesign");
		in = in.replace("Arraydesign", "ArrayDesign");
		in = in.replace("Elementresult", "ElementResult");
		in = in.replace("Mageml", "MageML");
		in = in.replace("Gcontext", "GContext");
		if (in.length()>4 && in.endsWith("prop")) {
			in = in.substring(0, in.length()-4)+"Prop";
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
	
	public String tableMunge(String in) {
		String munged = in.substring(0,1).toUpperCase()+in.substring(1);
		return munge(munged, in);
	}
	
	public String getQualifiedNameForTable(String table) {
		return getPackage(table)+"."+tableMunge(table);
	}
	
	public String getPackage(String table) {
		if (tablePackage.containsKey(table)) {
			return BASE_PKG+"."+tablePackage.get(table);
		}
		System.err.println("No special package for '"+table+"'");
		return BASE_PKG+".unclassified";
	}
	
	public String getForeignTable(String foreignTable, String thisTable) {
		String foreignPackage = getPackage(foreignTable);
		String thisPackage = getPackage(thisTable);
		String calcPackage = "";
		if (!thisPackage.equals(foreignPackage)) {
			calcPackage = foreignPackage + ".";
		}
		String table = tableMunge(foreignTable);
		//System.err.println(thisTable+'\t'+foreignTable+'\t'+calcPackage+table);
		return calcPackage+table;
	}
}
