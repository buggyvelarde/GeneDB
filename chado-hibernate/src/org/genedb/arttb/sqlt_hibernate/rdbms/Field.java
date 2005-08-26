package org.genedb.arttb.sqlt_hibernate.rdbms;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.genedb.arttb.sqlt_hibernate.Global;
import org.genedb.arttb.sqlt_hibernate.Munger;
import org.genedb.arttb.sqlt_hibernate.OutputStyle;



public class Field implements ItemRDBMS {

    private String name;
    private String dataType;
    private String size;
    private boolean nullable;
    private boolean autoIncrement;
    private boolean primaryKey;
    private boolean foreignKey;
    private String extra;
    private String comments = "no field description";
    private String defaultValue;
    private String unique;
    private String index;
    
    private Table table;
	private List<Constraint> foreignKeyConstraints = new ArrayList<Constraint>();
    
	public void setTable(Table table) {
		this.table = table;
	}



	public void setName(String name) {
		this.name = name;
	}

//	<set name="properties" table="featureprop" inverse="true">
//	<key column="featureId" />
//	<one-to-many class="org.genedb.arttb.sqlt_hibernate.generated.Featureprop" />
//</set>


	public void outputItem(OutputStyle style, PrintWriter out, File file) {
		if (style.equals(OutputStyle.SQLT_XML)) {
			out.println("\t\tField: name='"+name+"' dataType='" + dataType 
					+ "' size='" + size 
					+ "' nullable='" + nullable
					+ "' autoIncrement='" + autoIncrement
					+ "' primaryKey='" + primaryKey
					+ "' foreignKey='" + foreignKey
					+ "' extra='" + extra 
					+ "' comments='" + comments + "'");
			return;
		}
		Munger munger = Global.getMunger();
		if (primaryKey) {
	        out.println("\t\t<id name=\""+munger.munge(name, table.getName())+"\" type=\""+dataType+"\" column=\""+name+"\"><generator class=\"sequence\"><param name=\"sequence\">"+table.getName()+"_"+name+"_seq</param></generator></id>");	
			return;
		}
		
		if (foreignKeyConstraints.size()>0) {
			assert(foreignKeyConstraints.size()==1);
			Constraint fkc = foreignKeyConstraints.get(0);
			out.print("\t\t<many-to-one name=\""+munger.munge(name, table.getName())
					+"\" class=\""+munger.getForeignTable(fkc.getReferenceTable(), table.getName())
					+"\" column=\""+name+"\" ");
			if (unique!=null) {
				if (SINGLE_UNIQUE.equals(unique)) {
					out.print(" unique=\"true\"");
				} else {
					out.print((" unique-key=\""+unique+"\""));
				}
			}
			if (index!=null) {
				out.print(" index=\""+index+"\"");
			}
			out.println("/>");
			return;
		}
		
		//out.println("\t\t<property name=\""+Munger.munge(name)+"\">");
		out.print("\t\t<property name=\""+munger.munge(name, table.getName())+"\" column=\""+name+"\" type=\"");
		//out.print("\t\t\t<column name=\""+name+"\" sql-type=\"");
		boolean done = false;
		
		if ("varchar".equals(dataType)) {
			out.print("string\" length=\""+size+"\"");
			done=true;
		}
		if (!done && "text".equals(dataType)) {
			out.print("string\"");
			done=true;
		}
		if (!done && "integer".equals(dataType)) {
			out.print("integer\"");
			done=true;
		}
		if (!done && "float".equals(dataType)) {
			out.print("float\"");
			done=true;
		}
		if (!done && "boolean".equals(dataType)) {
			out.print("boolean\"");
			done=true;
		}
		if (!done && "timestamp".equals(dataType)) {
			out.print("timestamp\"");
			done=true;
		}
		if (!done && "date".equals(dataType)) {
			out.print("date\"");
			done=true;
		}
		if (!done && "char".equals(dataType)) {
			out.print("char\"");
			done=true;
		}
		if (!done) {
			System.err.println("Got an unexpected type: '"+dataType+"'");
		}
		
		if ("integer".equals(dataType) || "text".equals(dataType) || "varchar".equals(dataType)) {
			out.print(" not-null=\"");
			if (nullable) {
				out.print("false");
			} else {
				out.print("true");
			}
			out.print("\"");
		}
		
		if (unique!=null) {
			if (SINGLE_UNIQUE.equals(unique)) {
				out.print(" unique=\"true\"");
			} else {
				out.print((" unique-key=\""+unique+"\""));
			}
		}
		
		if (index!=null) {
			out.print(" index=\""+index+"\"");
		}
		
		//if (defaultValue != null) {
		//	out.print(" unsaved-value=\""+defaultValue+"\"");
		//}
		
		//out.println(">");
		//out.println("\t\t\t\t<comment>"+comments+"</comment>");
		//out.println("\t\t\t</column>");
		//out.println("\t\t</property>");
		out.println(" />");
	}

	public static final String SINGLE_UNIQUE="single_column_unique";


	public void setComments(String comments) {
		if (comments != null && comments.length()>0) {
			this.comments = comments;
		}
	}



	public void setDataType(String dataType) {
		this.dataType = dataType;
	}



	public void setExtra(String extra) {
		this.extra = extra;
	}



	public void setSize(String size) {
		this.size = size;
	}



	public void setAutoIncrement(boolean autoIncrement) {
		this.autoIncrement = autoIncrement;
	}



	public void setForeignKey(boolean foreignKey) {
		this.foreignKey = foreignKey;
	}



	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}



	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}



	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}



	public void setUnique(String unique) {
		this.unique = unique;
	}



	public String getName() {
		return name;
	}



	public void addForeignKeyConstraint(Constraint constraint) {
		this.foreignKeyConstraints .add(constraint);
	}



	public void setIndex(String index) {
		this.index = index;
	}
}
