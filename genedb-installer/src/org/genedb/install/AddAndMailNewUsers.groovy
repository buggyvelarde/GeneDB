package org.genedb.install;

import groovy.sql.Sql
import java.util.regex.Matcher

/**
* Simple class for loading organism and related 
* phylogeny tables, from the  XML file at the bottom
*/
class AddAndMailNewUsers {

	def templateMail = ''' This is an email, announcing your account details to the malaria reannotation ''';
	
	def temmplateSql = ''' ''';
	
    def db
    
    int index
    
    def orgDataSet
    def orgId
    
    def nodeDataSet
    def nodeId
    
    def dbDataSet
    
    def nodeOrgDataSet
    def nodeOrgId
    
    def dbxref
    def dbRefId
    
    def orgPropDataSet
    def orgPropId
    
    def dbId;
    
    def genedbMiscCvId;
    
    def cvTerm;
    
    def organismDbXRefSet;
    
    def nodePropDataSet;
    
    boolean writeBack = true;
    
    LoadGeneDbPhylogeny() {
		db = Sql.newInstance(
			'jdbc:postgresql://pathdbsrv1a.internal.sanger.ac.uk:10103/malaria_workshop',
			'pathdb',
			'pathdb',
			'org.postgresql.Driver')
			
     	orgDataSet = db.dataSet("organism")
		
		orgPropDataSet = db.dataSet("organismprop")
		
		dbDataSet = db.dataSet("db")
		
     	nodeDataSet = db.dataSet("phylonode")
		
     	nodePropDataSet = db.dataSet("phylonodeprop")
		
		nodeOrgDataSet = db.dataSet("phylonode_organism")
		
		orgPropDataSet = db.dataSet("organismprop");
		
		List trees =   db.rows("SELECT * from db where name='null'");
	    dbId = trees[0]."db_id";
	    
	    trees = db.rows("select * from cv where name='genedb_misc'");
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
	    List trees = db.rows("SELECT * from phylotree where name='org_heirachy'");
	    def tree = trees[0]."phylotree_id";
	    def heirachy = new XmlParser().parseText(inp) 
  
	    index = getMaxIdFromPrimaryKey("phylonode")
	    heirachy.children().each(
	            {setLeftAndRight(it, 1)}
        )
//def rootNode = createPhylonode(, null, );
//      setLeftAndRight(heirachy.node, 1)
//        )
	    //createNode(hierachy.node, null, tree)
	    heirachy.children().each(
	            {createNode(it, null, tree)}
        )		
	}

	// Walk the tree for node and organism nodes, storing values for 
	// depth and left on the way down, and right and taxonIds on the way back up
	List setLeftAndRight(def node, int depth) {
		if (node.name() != 'node' && node.name() != 'organism') {
			return Collections.EMPTY_LIST;
		}
		//System.out.println("Processing node on way down, depth='"+depth+"'")
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
			break
			
			case 'organism':
				def newNodeId = createPhylonode(node, parentId, tree)
          	  
				Map props = node.attributes()
				String temp = props.remove("fullName")

				List sections = temp.split(" ", 2);
          	  
				createOrganism(node,sections[0],sections[1])
              
				if (writeBack) {
				    try {
						nodeOrgDataSet.add(
							phylonode_id: newNodeId,
							organism_id:  getMaxIdFromPrimaryKey("organism")
						)
		 		  	 } catch (Exception exp) {
		    			 // May be a duplicate
		    		  	 System.err.println("Problem storing phylonode_organism - duplicate?");
		   			 }
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
			 			try {
							orgPropDataSet.add(
								organism_id: orgId,
								type_id: type_id,
								value: value,
								rank: 0)
		 	  			 } catch (Exception exp) {
		    			 // May be a duplicate
		    		  	 System.err.println("Problem storing organism property - duplicate?");
		   				 }
					}
				})
				
				
/*				if (writeBack) {
					try {
					    orgDbXRefDataSet.add(
							organism_id: dbId,
					        dbxref_id: description
					    )
    	  			 } catch (Exception exp) {
        				 //May be a duplicate
        		  		 System.err.println("Problem storing db - duplicate?");
       			 	}
				}*/
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
		//println "CreatePhylonode: name='"+node.'@name'+"', left='"+left
		//	+"', right='"+right+"' depth='"+depth+"' taxonids='"
		//	+node.attributes().taxonIds+"'";
		int ptree = tree;
		//int parentid = parent;
		String nodeName = node.'@name'
		if (writeBack) {
		    try {
				nodeDataSet.add(
					phylotree_id:        ptree,
        			parent_phylonode_id: parent,
       			 	left_idx:            left, 	
       		 		right_idx:           right, 	
       			 	label:               nodeName,		
       			 	distance:            depth,
      			)
	 	 	 } catch (Exception exp) {
    			 // May be a duplicate
    	 	 	 System.err.println("Problem storing phylonode - duplicate?");
   			 }
		}
		node.attributes().remove("left");
		node.attributes().remove("right");
		node.attributes().remove("depth")
		node.attributes().remove("shortName") // TODO Is this right
		
		Set skipAttributeNames = new HashSet();
		skipAttributeNames.add("fullName");
		skipAttributeNames.add("newOrg");
		skipAttributeNames.add("name")
		
      	for (attr in node.attributes()) {
      		// println "CreatePhylonodeProp: '"+attr.key+"'='"+attr.value+"'"
      		if (skipAttributeNames.contains(attr.key)) {
      		    continue
      		}
      		int type_id = findCvTerm(attr.key)
			String value = attr.value;
  			if (writeBack) {
				try {
		      		nodePropDataSet.add(
   		 	  			phylonode_id: getMaxIdFromPrimaryKey("phylonode"),
      					type_id:      type_id,
      					value:		  value,
      					rank:		  0
      				)
   	 	  		} catch (Exception exp) {
	    			// May be a duplicate
	    		  	System.err.println("Problem storing phylonodeprop - duplicate?");
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
		//println "CreateOrganism called with '"+node.'@name'+"'"
		String abb = node.'@name'
		String gen = genus;
		String sp = species;
		if (writeBack) {
		    try {
				orgDataSet.add(		
					abbreviation: abb,
        		    genus:        gen,
					species:      sp,
					common_name:  abb
     	 		 )
			} catch (Exception exp) {
				// May be a duplicate
				System.err.println("Problem storing org - duplicate?");
			}
		}
		
		String accession = node.'@name'
		//Create dbxref
//		if (writeBack) {
//             try {
//		        dbxref.add(
//    		        db_id:	dbId,
//        		    accession:		accession,
//					version:		1,
//					description:	"dbxref for organism"
//   	     	)
//    	  	 } catch (Exception exp) {
//        		  May be a duplicate
//        	  	 System.err.println("Problem storing cvterm - duplicate?");
//       		 }
        
//	         Create organism dbxref
//	         try {
//				organismDbXRefSet.add(
//				    organism_id: getMaxIdFromPrimaryKey("organism"),
//		 		    dbxref_id:   getMaxIdFromPrimaryKey("dbxref")
//				)
//		  	 } catch (Exception exp) {
//   			  May be a duplicate
//    	  		 System.err.println("Problem storing cvterm - duplicate?");
//   			 }
//		}
        node.attributes().remove("name");
	}
	
        
	static void main(args) {
    	AddAndMailNewUsers app = new AddAndMailNewUsers()
    	app.process(input);
    	System.err.println("Done");
	}
	

	// The XML config file for loading the organism and phylogeny modules

}