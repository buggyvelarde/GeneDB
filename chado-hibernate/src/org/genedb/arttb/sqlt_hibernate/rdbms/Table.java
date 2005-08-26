package org.genedb.arttb.sqlt_hibernate.rdbms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.genedb.arttb.sqlt_hibernate.Global;
import org.genedb.arttb.sqlt_hibernate.Munger;
import org.genedb.arttb.sqlt_hibernate.OutputStyle;

public class Table implements ItemRDBMS {
	private String name;
	private List<Field> fields = new ArrayList<Field>();
	private List<Index> indices = new ArrayList<Index>();
	private List<Constraint> constraints = new ArrayList<Constraint>();
	private String comments = "no table description"; 
	
	public void postProcess() {
		for (Constraint constraint : constraints) {
			if (constraint.isUnique()) {
				processUniqueConstraints(constraint);
				continue;
			}
			if (constraint.isForeignKey()) {
				processForeignKeyConstraints(constraint);
				continue;
			}
		}
		processIndices();
	}

	private void processForeignKeyConstraints(Constraint constraint) {
		String[] fields = constraint.getSplitFields();
		assert(fields != null);
		assert(fields.length == 1);
		// single-field unique
		Field field = findFieldByName(fields[0]);
		field.addForeignKeyConstraint(constraint);
	}
	
	private void processUniqueConstraints(Constraint constraint) {
		String[] fields = constraint.getSplitFields();
		if (fields != null) {
			if (fields.length == 1) {
				// single-field unique
				Field field = findFieldByName(fields[0]);
				field.setUnique(Field.SINGLE_UNIQUE);
			} else {
				for (int i = 0; i < fields.length; i++) {
					Field field = findFieldByName(fields[i]);
					field.setUnique("wibble"+name);
				}
			}
		} else {
			System.err.println("Warning got null for constraint '"+constraint+"' in table '"+name+"'");
		}
	}
	
	private void processIndices() {
		for (Index index : indices) {
			String[] fields = index.getSplitFields();
			if (fields != null) {
				if (fields.length == 1) {
					// single-field unique
					Field field = findFieldByName(fields[0]);
					field.setIndex(index.getName());
				} else {
					System.err.println("Skipping multi-column index in table '"+name+"'");
//					for (int i = 0; i < fields.length; i++) {
//						Field field = findFieldByName(fields[i]);
//						field.setIndex("wibble"+name);
//					}
				}
			} else {
				System.err.println("Warning got null for index '"+index.getName()+"' in table '"+name+"'");
			}
		}
	}
	
	private Field findFieldByName(String fieldName) {
		for (Field field: fields) {
			if (fieldName.equals(field.getName())) {
				return field;
			}
		}
		throw new RuntimeException("No field named '"+fieldName+"' in table '"+name+"'");
	}

	public void addField(Field f) {
		fields.add(f);
		f.setTable(this);
	}
	
	public void addIndex(Index i) {
		indices.add(i);
	}
	
	public void addConstraint(Constraint c) {
		constraints.add(c);
	}
	
	public void outputItem(OutputStyle style, PrintWriter out, File file) throws IOException {
		if (style.equals(OutputStyle.SQLT_XML)) {
			out.println("\tTable: name='"+name+"'");
		} else {
			// Define out, start new file
			Munger munger = Global.getMunger();
			File hbmFile = new File(file, munger.tableMunge(name)+".hbm.xml");
			out = new PrintWriter(new FileWriter(hbmFile));
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			out.println("<!DOCTYPE hibernate-mapping PUBLIC "
					+ "\"-//Hibernate/Hibernate Mapping DTD 3.0//EN\" "
					+ "\"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">");
			out.println("\n<hibernate-mapping package=\""+munger.getPackage(name)+"\">");
			out.println("\t<class name=\""+munger.tableMunge(name)+"\" table=\""+name+"\">");
			out.println("\t\t<comment>"+comments+"</comment>");
		}
		
		for (Field field: fields) {
			field.outputItem(style, out, null);
		}
		for (Index index : indices) {
			index.outputItem(style, out, null);
		}
		for (Constraint constraint : constraints) {
			constraint.outputItem(style, out, null);
		}
		
		if (style.equals(OutputStyle.HIBERNATE)) {
			// Neede to close XML and file
			out.println("\t</class>");
			out.println("</hibernate-mapping>");
			out.flush();
			out.close();
		}
	}



	public List<Field> getFields() {
		return fields;
	}



	public void setFields(List<Field> fields) {
		this.fields = fields;
	}



	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
