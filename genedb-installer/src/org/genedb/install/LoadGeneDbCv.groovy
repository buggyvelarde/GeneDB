package org.genedb.install;

import groovy.sql.Sql
import java.util.regex.Matcher

/**
* Simple class for creating CVs and their terms, 
* from the  XML file at the bottom. Note it assumes the CVs 
* already exist
*/
class LoadGeneDbCv {

    def db
    
    int index
    
    def cvDataSet
    def cvId
    
    def termDataSet
    def termId
    
    def relationDataSet
    
    def nodeOrgDataSet
    def nodeOrgId
    
    def dbxrefDataSet
    def dbXRefId
    
    def dbDataSet
    def dbId
    
    boolean writeBack = false;
    
    LoadGeneDbCv() {
		db = Sql.newInstance(
			'jdbc:postgresql://pathdbsrv1a.internal.sanger.ac.uk:10001/chado',
			'chado',
			'chado',
			'org.postgresql.Driver')
			
     	cvDataSet = db.dataSet("cv")
		
		termDataSet = db.dataSet("cvterm")
		
     	dbxrefDataSet = db.dataSet("dbxref")
		
     	dbDataSet = db.dataSet("db")
		
		nodeOrgDataSet = db.dataSet("phylonode_organism")
		
		orgPropDataSet = db.dataSet("organismprop");
	    
	    List relations = db.rows("select * from cv where name='relationship'");
	    genedbMiscCvId = trees[0]."cv_id";
	    
	    dbxref = db.dataSet("dbxref");
	    cvTerm = db.dataSet("cvterm");
	    
	    organismDbXRefSet = db.dataSet("organism_dbxref")

    }


	// Find the maximum current value of the primary key for this table 
	def getMaxIdFromPrimaryKey(String tableName) {
		def col = tableName + "_id";
	    int id = db.rows("SELECT max("+col+") as "+col+" from " + tableName)[0]["${col}"];
	    if (id == null) {
	        return 1
	    }
	    return id;
	}
	
	
	void process(def inp) {
	    def heirachy = new XmlParser().parseText(inp) 
  
	    heirachy.children().each(
	            {createNode(it, null, tree)}
        )		
	}


	
	// Now walk the tree creating db entries  
	void createNode(def node, def parentId, def tree) {
		assert node != null
		switch (node.name()) {
			case 'node':
				def newNodeId = createPhylonode(node, parentId, tree)
				node.children().each(
					{createNode(it, newNodeId, tree)}
				)
				// TODO Pick up page attribute - anything else?
			break
			case 'organism':
				def newNodeId = createPhylonode(node, parentId, tree)
          	  
				Map props = node.attributes()
				String temp = props.remove("fullName")

				List sections = temp.split(" ", 2);
          	  
				createOrganism(node,sections[0],sections[1])
              
				if (writeBack) {
					nodeOrgDataSet.add(
						phylonode_id: newNodeId,
						organism_id:  getMaxIdFromPrimaryKey("organism")
					)
				}
					
				checkAttributeExists(node, "translationTable")
				checkAttributeExists(node,"curatorEmail")
				
				if (!props.containsKey("nickname") && !props.containsKey("newOrg")) {
					println "No nickname for '"+node.'@name'+"'"
					throw new RuntimeException("Found an old organism without a nickname")
				}
				if (props.containsKey("nickname") && props.get("nickname").size()==0) {
					println "No nickname for '"+node.'@name'+"'"
					throw new RuntimeException("Found an old organism without a nickname")
				}
				props.remove("newOrg")
				//props.put("fullName",temp)
          	  
				int orgId = getMaxIdFromPrimaryKey("organism")
				props.each({
					List rows = db.rows("select * from cvterm where name='"+it.key+"'")
					if (rows[0] == null) {
					    throw new RuntimeException("Couldn't get cvterm for '"+it.key+"'");
					}
					int type_id = findCvTerm(it.key)
					String value = it.value;
					if (writeBack) {
						orgPropDataSet.add(
							organism_id: orgId,
							type_id: type_id,
							value: value,
							rank: 0)
					}
				})
				break
			default:
				throw new RuntimeException("Saw a node of '"+node.name()
				        +"' when expecting node or organism");
		}
	}
  
	// Sanity check that an expected property is present 
	void checkAttributeExists(def node, String key) {
	    if (!node.attributes().containsKey(key)) {
	        throw new RuntimeException("No "+key+" for '"+node.'@name'+"'")
		}
	}
  
	// Create a set of db entries when a phylogeny node is encountered
	int createPhylonode(def node, def parent, def tree) {
		//double dist = node.parent().size();
		int left = node.attributes().left;
		int right = node.attributes().right;
		int depth = node.attributes().depth;
		println "CreatePhylonode: name='"+node.'@name'+"', left='"+left
			+"', right='"+right+"' depth='"+depth+"' taxonids='"
			+node.attributes().taxonIds+"'";
		int ptree = tree;
		//int parentid = parent;
		String nodeName = node.'@name'
		if (writeBack) {
			nodeDataSet.add(
				phylotree_id:        ptree,
        		parent_phylonode_id: parent,
       		 	left_idx:            left, 	
       		 	right_idx:           right, 	
       		 	label:               nodeName,		
       		 	distance:            depth,
      		)
		}
		node.attributes().remove("left");
		node.attributes().remove("right");
		node.attributes().remove("depth")
		
      	for (attr in node.attributes()) {
      		if (attr.key == "page") {
      			println "CreatePhylonodeProp: page='"+attr.value+"'"
      			int type_id = db.rows("select * from cvterm where name='DisplayPage'")[0]["cvterm_id"]
				String value = attr.value;      			                                                                          
      			if (writeBack) {
	      			nodePropDataSet.add(
    	  				phylonode_id: getMaxIdFromPrimaryKey("phylonode"),
      					type_id:      type_id,
      					value:		  value,
      					rank:		  0
      				)
      			}
      		}
      		if (attr.key == "taxonList") {
      			println "CreatePhylonodeProp: taxonList='"+attr.value+"'"
      			int type_id = findCvTerm("taxonList")
				String value = attr.value;
      			if (writeBack) {
	      			nodePropDataSet.add(
    	  				phylonode_id: getMaxIdFromPrimaryKey("phylonode"),
      					type_id:      type_id,
      					value:		  value,
      					rank:		  0
      				)
      			}
      		}
      	}
		node.attributes().remove("taxonList");
		return getMaxIdFromPrimaryKey("phylonode")
	}
  
	int findCvTerm(String termName) {
	    def rows = db.rows("select cvterm_id from cvterm where name='"+termName
	            +"' and cv_id='"+genedbMiscCvId+"'")
	    if (rows[0] == null) {
	        throw new RuntimeException("Couldn't get cvterm for '"+termName+"'");
	    }
	    return rows[0]["cvterm_id"]
	}
	
	// Create a set of db entries when an organism node is encountered
	void createOrganism(def node,def genus,def species) {
		println "CreateOrganism called with '"+node.'@name'+"'"
		String abb = node.'@name'
		String gen = genus;
		String sp = species;
		if (writeBack) {
			orgDataSet.add(		
				abbreviation: abb,
        	    genus:        gen,
				species:      sp,
				common_name:  abb
     	   )
		}
		
        String accession = node.'@name'
        int id = dbId
         //Create dbxref
         if (writeBack) {
//	        dbxref.add(
//    	        db_id:	id,
//        	    accession:		accession,
//				version:		1,
//				description:	"dbxref for organism"
//         	)
        
	         //Create organism dbxref
//			organismDbXRefSet.add(
//			    organism_id: getMaxIdFromPrimaryKey("organism"),
//		 	    dbxref_id:   getMaxIdFromPrimaryKey("dbxref")
//			)
         }
        node.attributes().remove("name");
	}
	
        
	static void main(args) {
    	LoadGeneDbCv lgc = new LoadGeneDbCv()
    	lgc.process(input);
	}
	

	// The XML config file for loading the organism and phylogeny modules
	static String input = '''<?xml version="1.0" encoding="UTF-8"?>
<cv-loading>
    <cv name="Root">
		<term name="Helminths" desription="">
			<node name="Platyhelminths">
				<organism name="Smansoni">
					<property name="taxonId" value="6183" />
					<property name="fullName" value="Schistosoma mansoni" />
					<property name="nickname" value="smansoni" />
	            	<property name="dbName" value="GeneDB_Smansoni" />
					<property name="translationTable" value="1"/>
					<property name="mitochondrialTranslationTable" value="9"/>
					<property name="curatorEmail" value="mb4"/>
				</organism>
			</node>
		</node>
	</cv>
</cv-loading>
''';
  

/*	// Walk the tree for node and organism nodes, storing values for 
// depth and left on the way down, and right and taxonIds on the way back up
List setLeftAndRight(def node, int depth) {
	if (node.name() != 'node' && node.name() != 'organism') {
		return Collections.EMPTY_LIST;
	}
	System.out.println("Processing node on way down, depth='"+depth+"'")
	node.attributes().put('depth', depth);
	node.attributes().put('left', ++index);
	++depth;
	List nestedTaxons = new ArrayList();
	for (child in node.children()) {
		nestedTaxons.addAll(setLeftAndRight(child, depth))
	}
	node.attributes().put('right', ++index)
	
	// Move values from organism props to attributes 
	if (node.name() == 'organism') {
		node.children().each({
			def key
			def value
			for (attr in it.attributes()) {
				if (attr.key == "name") {
					key = attr.value
				} else {
					value = attr.value
				}
			}
			node.attributes().put(key, value)
		}
		)
	    // Extract taxon id and add to nestedTaxons
	   	checkAttributeExists(node, "taxonId")
	    nestedTaxons.add(node.'@taxonId')
	}
	node.attributes().put('taxonList', nestedTaxons.join(" "));
	return nestedTaxons;
}*/

}