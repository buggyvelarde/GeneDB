package org.genedb.arttb.sqlt_hibernate.rdbms;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.genedb.arttb.sqlt_hibernate.OutputStyle;

public class Database implements ItemRDBMS {
	
	private List<Table> tables = new ArrayList<Table>();
	
	public void addTable(Table t) {
		tables.add(t);
		t.postProcess();
	}
	
	public void outputItem(OutputStyle style, PrintWriter out, File file) throws IOException {
		if (style.equals(OutputStyle.SQLT_XML)) {
			out.println("Database");
		} else {
//			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
//			out.println("<!DOCTYPE hibernate-mapping PUBLIC "
//					+ "\"-//Hibernate/Hibernate Mapping DTD 3.0//EN\" "
//					+ "\"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">");
//			out.println("\n<hibernate-mapping>");
		}
		for (Table table : tables) {
			table.outputItem(style, out, file);
		}
		if (style.equals(OutputStyle.HIBERNATE)) {
//			out.println("</hibernate-mapping>");
//			out.flush();
//			out.close();
		}
	}
}
