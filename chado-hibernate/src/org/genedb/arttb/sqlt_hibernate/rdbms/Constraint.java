package org.genedb.arttb.sqlt_hibernate.rdbms;

import java.io.File;
import java.io.PrintWriter;

import org.genedb.arttb.sqlt_hibernate.OutputStyle;



public class Constraint implements ItemRDBMS {


    private String referenceTable;
    private String referenceFields;
    	private String onDelete;
    private String onUpdate;
    	private String matchType;
    	private String expression;
    	private boolean deferrable;
    	private boolean unique;
    	
    	private String type;
    private String name;
    private String fields;
    private String[] splitFields;
    private String options;
	private boolean foreignKey;
	private boolean notNull;
	private boolean primaryKey;
	
    
    
	public void setName(String name) {
		this.name = name;
	}



	public void outputItem(OutputStyle style, PrintWriter out, File file) {
		if (style.equals(OutputStyle.SQLT_XML)) {
			out.println("\t\tConstraint: name='"+name
					+ "' type='" + type
					+ "' fields='" + fields + "' options='" + options 
					+ "' referenceTable='" + referenceTable 
					+ "' referenceFields='" + referenceFields 
					+ "' onDelete='" + onDelete 
					+ "' onUpdate='" + onUpdate
					+ "' matchType='" + matchType
					+ "' expression='" + expression
					+ "' deferrable='" + deferrable +"'");	
		}
	}



	public void setType(String type) {
		this.type=type;
		if ("UNIQUE".equals(type)) {
			this.unique = true;
			return;
		}
		if ("NOT NULL".equals(type)) {
			this.notNull = true;
			return;
		}
		if ("PRIMARY KEY".equals(type)) {
			this.primaryKey = true;
			return;
		}
		if ("FOREIGN KEY".equals(type)) {
			this.foreignKey = true;
			return;
		}
		System.err.println(type);
	}

	public void setFields(String fields) {
		this.fields = fields;
		this.splitFields = fields.split(",");
	}



	public void setOptions(String options) {
		this.options = options;
	}



	public void setDeferrable(boolean deferrable) {
		this.deferrable = deferrable;
	}



	public void setExpression(String expression) {
		this.expression = expression;
	}



	public void setMatchType(String matchType) {
		this.matchType = matchType;
	}



	public void setOnDelete(String onDelete) {
		this.onDelete = onDelete;
	}



	public void setOnUpdate(String onUpdate) {
		this.onUpdate = onUpdate;
	}



	public void setReferenceFields(String referenceFields) {
		this.referenceFields = referenceFields;
	}



	public void setReferenceTable(String referenceTable) {
		this.referenceTable = referenceTable;
	}



	public boolean isUnique() {
		return unique;
	}



	public String[] getSplitFields() {
		return splitFields;
	}



	public boolean isForeignKey() {
		return foreignKey;
	}



	public String getReferenceTable() {
		return referenceTable;
	}



	public String getReferenceFields() {
		return referenceFields;
	}



	public String getName() {
		return name;
	}

}
