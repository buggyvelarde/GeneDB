package org.genedb.install;

import groovy.sql.Sql
import java.util.regex.Matcher

/**
* Simple class for loading organism and related 
* phylogeny tables, from the  XML file at the bottom
*/
class LoadGeneDbPhylogeny {

    def db
    
    int index
    
    def orgDataSet
    def orgId
    
    def nodeDataSet
    def nodeId
    
    def nodeOrgDataSet
    def nodeOrgId
    
    def dbxref
    def dbRefId
    
    def orgPropDataSet
    def orgPropId
    
    def dbId;
    
    def cv;
    
    def cvTerm;
    
    def organismDbXRefSet;
    
    def nodePropDataSet;
    
    LoadGeneDbPhylogeny() {
		db = Sql.newInstance(
			'jdbc:postgresql://pathdbsrv1a.internal.sanger.ac.uk:10001/chado',
			'chado',
			'chado',
			'org.postgresql.Driver')
			
     	orgDataSet = db.dataSet("organism")
		//orgId = getNextId("organism")
		
		orgPropDataSet = db.dataSet("organismprop")
		//orgId = getNextId("organismprop")
		
     	nodeDataSet = db.dataSet("phylonode")
		//nodeId = getNextId("phylonode")
		
     	nodePropDataSet = db.dataSet("phylonodeprop")
		
		nodeOrgDataSet = db.dataSet("phylonode_organism")
		//nodeOrgId = getNextId("phylonode_organism")
		
		orgPropDataSet = db.dataSet("organismprop");
		//orgPropId = getNextId("organismprop");
		
		List trees =   db.rows("SELECT * from db where name='null'");
	    dbId = trees[0]."db_id";
	    
	    trees = db.rows("select * from cv where name='sequence'");
	    cv = trees[0]."cv_id";
	    
	    dbxref = db.dataSet("dbxref");
	    cvTerm = db.dataSet("cvterm");
	    
	    organismDbXRefSet = db.dataSet("organism_dbxref")
	    /*
	    dbxref.add(
	    		db_id: dbId,
	    		accession: "taxonomyid",
	    		version: "1",
	    		description: "dbxref for cvterm taxonId")
	    
	    cvTerm.add(
	    		cv_id: cv,
	    		name: "taxonId",
	    		definition: "taxonomy id",
	    		dbxref_id: getNextId("dbxref"),
	    		is_obsolete: 0,
	    		is_relationshiptype: 0)
	    		
	    dbxref.add(
	    		db_id: dbId,
	    		accession: "fullName",
	    		version: "1",
	    		description: "dbxref for cvterm fullName")
	    
	    cvTerm.add(
	    		cv_id: cv,
	    		name: "fullName",
	    		definition: "full name of organism",
	    		dbxref_id: getNextId("dbxref"),
	    		is_obsolete: 0,
	    		is_relationshiptype: 0)
	    
		dbxref.add(
	    		db_id: dbId,
	    		accession: "nickname",
	    		version: "1",
	    		description: "dbxref for cvterm nickname")
	    
	    cvTerm.add(
	    		cv_id: cv,
	    		name: "nickname",
	    		definition: "nick name of organism",
	    		dbxref_id: getNextId("dbxref"),
	    		is_obsolete: 0,
	    		is_relationshiptype: 0)
	    
	    dbxref.add(
	    		db_id: dbId,
	    		accession: "dbName",
	    		version: "1",
	    		description: "dbxref for cvterm dbName")
	    
	    cvTerm.add(
	    		cv_id: cv,
	    		name: "dbName",
	    		definition: "database name for organism",
	    		dbxref_id: getNextId("dbxref"),
	    		is_obsolete: 0,
	    		is_relationshiptype: 0)
	    		
	    dbxref.add(
	    		db_id: dbId,
	    		accession: "transTable",
	    		version: "1",
	    		description: "dbxref for cvterm transTable")
	    
	    cvTerm.add(
	    		cv_id: cv,
	    		name: "transTable",
	    		definition: "transTable",
	    		dbxref_id: getNextId("dbxref"),
	    		is_obsolete: 0,
	    		is_relationshiptype: 0)
		
		dbxref.add(
	    		db_id: dbId,
	    		accession: "DisplayPage",
	    		version: "1",
	    		description: "dbxref for cvterm DisplayPage")
	    
	    cvTerm.add(
	    		cv_id: cv,
	    		name: "DisplayPage",
	    		definition: "cvterm for attribute value page in taxonomy xml, which decides whether ot not a group should have individual page",
	    		dbxref_id: getNextId("dbxref"),
	    		is_obsolete: 0,
	    )

		dbxref.add(
	    		db_id: dbId,
	    		accession: "curator",
	    		version: "1",
	    		description: "dbxref for cvterm curator")
	    
	    cvTerm.add(
	    		cv_id: cv,
	    		name: "curator",
	    		definition: "curator",
	    		dbxref_id: getNextId("dbxref"),
	    		is_obsolete: 0,
	    		is_relationshiptype: 0)
	    		
	    dbxref.add(
	    		db_id: dbId,
	    		accession: "mitoTransTable",
	    		version: "1",
	    		description: "dbxref for cvterm mitoTransTable")
	    
	    cvTerm.add(
	    		cv_id: cv,
	    		name: "mitoTransTable",
	    		definition: "mitoTransTable",
	    		dbxref_id: getNextId("dbxref"),
	    		is_obsolete: 0,
	    		is_relationshiptype: 0)
		*/
    }


	// Select a value for the primary key based on the maximum current value
	def getNextId(def table) {
		def col = table + "_id";
	    int id = db.rows("SELECT max("+col+") as "+col+" from " + table)[0]["${col}"];
	    if (id == null) {
	        return 1
	    }
	    return id;
	}
	
	List tids;
	List prev;
	
	void process(def inp) {
	    List trees = db.rows("SELECT * from phylotree where name='org_heirachy'");
	    def tree = trees[0]."phylotree_id";
	    def heirachy = new XmlSlurper().parseText(inp) 
  
	    index = 0
	    heirachy.node.children().each(
	            {
		            tids = new ArrayList() 
		            prev = new ArrayList()
		            //println "tids is ->> " + tids.size()
	            	setLeftAndRight(it, 1,tids) 
				}
        )	
	    
	    heirachy.node.children().each(
	            {createNode(it, null, tree)}
        )		
	}

	// Walk the tree for node and organism nodes, storing values for 
	// depth and left on the way down, and right on the way back up
	
	void setLeftAndRight(def node, def depth,List tids) {
		if (node.name() != 'node' && node.name() != 'organism') {
		    //println "node name is ---> " + node.name()
		    Map props = node.attributes();
		    if (props.get("name") == "taxonId") {
		    	//println props.get("value")
		    	tids.add(props.get("value"))
		    }
			return
		}
		node.attributes().put('depth', depth);
		node.attributes().put('left', ++index);
		++depth;
		node.children().each(
		        {
		        	if (it.name() == "node") {
		        		tids = new ArrayList()
		        	}
		        	setLeftAndRight(it, depth,tids)
		        }
		        )
		node.attributes().put('right', ++index) 
		if (node.name() == "node") {
			node.attributes().put('tanonIds',tids)
		}
	}
	
	// Now walk the tree creating db entries  
	void createNode(def node, def parent, def tree) {
		assert node != null
		switch (node.name()) {
			case 'node':
				createPhylonode(node, parent, tree)
				def newNode = getNextId("phylonode")
				node.children().each(
					{createNode(it, newNode, tree)}
					)
				// TODO Pick up page attribute - anything else?
			break
			case 'organism':
				createPhylonode(node, parent, tree)
          	  
				Map props = new HashMap()
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
					props.put(key, value)
				}
				)
          	  
				List sections
				String temp = props.remove("fullName")
          	  
				if (temp.contains(",")) {
					sections = temp.split(",")
				} else {
					sections = temp.split(" ")
				}
				//Matcher matcher = (temp =~ /([a-zA-Z]+)\s+([a-zA-Z]+)/) 
				//println (matcher[0])
				//println (matcher[0][1]+","+matcher[0][2])
          	  
				createOrganism(node,sections[0],sections[-1])
              
				/*nodeOrgDataSet.add(
					phylonode_id: getNextId("phylonode"),
					organism_id:  getNextId("organism")
					)*/
					
				checkPropExists(props, node, "taxonId")
				checkPropExists(props, node, "transTable")
				checkPropExists(props, node,"curator")
				
				if (!props.containsKey("nickname") && !props.containsKey("newOrg")) {
					println "No nickname for '"+node.'@name'+"'"
					throw new RuntimeException("Found an old organism without a nickname")
				}
				if (props.containsKey("nickname") && props.get("nickname").size()==0) {
					println "No nickname for '"+node.'@name'+"'"
					throw new RuntimeException("Found an old organism without a nickname")
				}
				props.remove("newOrg")
				props.put("fullName",temp)
          	  
				int orgId = getNextId("organism")
				props.each({
					List rows = db.rows("select * from cvterm where name='"+it.key+"'")
					int type_id = rows[0]."cvterm_id"
					String value = it.value;
					/*orgPropDataSet.add(
						organism_id: orgId,
						type_id: type_id,
						value: value,
						rank: 0)*/
						}
				)
				break
			default:
				throw new RuntimeException("Saw a node of '"+node.name()+"' when expecting node or organism");
		}
	}
  
	// Sanity check that an expected property is present 
	void checkPropExists(def map, def node, String key) {
	    if (!map.containsKey(key)) {
	        throw new RuntimeException("No "+key+" for '"+node.'@name'+"'")
		}
	}
  
	// Create a set of db entries when a phylogeny node is encountered
	void createPhylonode(def node, def parent, def tree) {
		println "CreatePhylonode: name='"+node.'@name'+"', left='"+node.attributes().get('left')+"', right='"+node.attributes().right+"' depth='"+node.attributes().depth+"' taxonids='"+node.attributes().taxonIds+"'" ;
		/*double dist = node.parent().size();
		int left = node.attributes().left;
		int right = node.attributes().right;
		int ptree = tree;
		//int parentid = parent;
		String nodeName = node.'@name'
		nodeDataSet.add(
			phylotree_id:        ptree,
        	parent_phylonode_id: parent,
        	left_idx:            left, 	
        	right_idx:           right, 	
        	label:               nodeName,		
        	distance:            dist,
      	)*/
      	
      	for (attr in node.attributes()) {
      		if (attr.key == "page") {
      			println "CreatePhylonodeProp: page='"+attr.value+"'"
      			/*int type_id = db.rows("select * from cvterm where name='DisplayPage'")[0]["cvterm_id"]
				String value = attr.value;      			                                                                          
      			nodePropDataSet.add(
      				phylonode_id: getNextId("phylonode"),
      				type_id:      type_id,
      				value:		  value,
      				rank:		  0
      			)*/
      		}
      		if (attr.key == "tanonIds") {
      			println "CreatePhylonodeProp: taxonid='"+attr.value+"'"
      			/*int type_id = db.rows("select * from cvterm where name='taxonid'")[0]["cvterm_id"]
				String value = attr.value;      			                                                                          
      			nodePropDataSet.add(
      				phylonode_id: getNextId("phylonode"),
      				type_id:      type_id,
      				value:		  value,
      				rank:		  0
      			)*/
      		}
      	}
	}
  
	// Create a set of db entries when an organism node is encountered
	void createOrganism(def node,def genus,def species) {
		println "CreateOrganism called with '"+node.'@name'+"'"
		/*String abb = node.'@name'
		String gen = genus;
		String sp = species;
		orgDataSet.add(		
			abbreviation: abb,
            genus:        gen,
            species:      sp,
            common_name:  abb
        )		
		
        String accession = node.'@name'
        int id = dbId
         //Create dbxref
        dbxref.add(
            db_id:	id,
            accession:	accession,
            version:	1,
            description:	"dbxref for organism"
        )
        
         //Create organism dbxref
		organismDbXRefSet.add(
		    organism_id: getNextId("organism"),
		    dbxref_id:   getNextId("dbxref")
		)*/
		
	}
	
        
	static void main(args) {
    	LoadGeneDbPhylogeny lgp = new LoadGeneDbPhylogeny()
    	lgp.process(input);
	}
	

	// The XML config file for loading the organism and phylogeny modules
	/*static String input = '''<?xml version="1.0" encoding="UTF-8"?>
<org-heirachy>
    <node name="Root">
		<node name="Protozoa">
		    <node name="Kinetoplastids">
		        <node name="Leishmania">
					<organism name="Lbraziliensis">
						<property name="taxonId" value="5660" />
						<property name="fullName" value="Leishmania braziliensis" />
						<property name="nickname" value="lbraziliensis" />
		                <property name="dbName" value="GeneDB_Lbraziliensis" />
						<property name="transTable" value="1"/>
						<property name="mitoTransTable" value="4"/>
						<property name="curator" value="csp"/>
					</organism>
		            <organism name="Lmajor">
		                <property name="taxonId" value="5664" />
		                <property name="fullName" value="Leishmania major" />
		                <property name="nickname" value="leish" />
		                <property name="dbName" value="GeneDB_Lmajor" />
						<property name="transTable" value="1"/>
						<property name="mitoTransTable" value="4"/>
						<property name="curator" value="csp"/>
		            </organism>
	                <organism name="Linfantum">
	                    <property name="taxonId" value="5761" />
	                    <property name="fullName" value="Leishmania infantum" />
	                    <property name="nickname" value="linfantum" />
	                    <property name="dbName" value="GeneDB_Linfantum" />
						<property name="transTable" value="1"/>
						<property name="mitoTransTable" value="4"/>
						<property name="curator" value="csp"/>
	                </organism>
	            </node>
			</node>
		</node>
	</node>
</org-heirachy>
''';*/
	static String input = '''<?xml version="1.0" encoding="UTF-8"?>
<org-heirachy>
    <node name="Root">
		<node name="Helminths">
			<node name="Platyhelminths">
				<organism name="Smansoni">
					<property name="taxonId" value="6183" />
					<property name="fullName" value="Schistosoma mansoni" />
					<property name="nickname" value="smansoni" />
	            	<property name="dbName" value="GeneDB_Smansoni" />
					<property name="transTable" value="1"/>
					<property name="mitoTransTable" value="9"/>
					<property name="curator" value="mb4"/>
				</organism>
			</node>
		</node>
		<node name="Protozoa">
		    <node name="Kinetoplastids">
		        <node name="Leishmania">
					<organism name="Lbraziliensis">
						<property name="taxonId" value="5660" />
						<property name="fullName" value="Leishmania braziliensis" />
						<property name="nickname" value="lbraziliensis" />
		                <property name="dbName" value="GeneDB_Lbraziliensis" />
						<property name="transTable" value="1"/>
						<property name="mitoTransTable" value="4"/>
						<property name="curator" value="csp"/>
					</organism>
		            <organism name="Lmajor">
		                <property name="taxonId" value="5664" />
		                <property name="fullName" value="Leishmania major" />
		                <property name="nickname" value="leish" />
		                <property name="dbName" value="GeneDB_Lmajor" />
						<property name="transTable" value="1"/>
						<property name="mitoTransTable" value="4"/>
						<property name="curator" value="csp"/>
		            </organism>
	                <organism name="Linfantum">
	                    <property name="taxonId" value="5761" />
	                    <property name="fullName" value="Leishmania infantum" />
	                    <property name="nickname" value="linfantum" />
	                    <property name="dbName" value="GeneDB_Linfantum" />
						<property name="transTable" value="1"/>
						<property name="mitoTransTable" value="4"/>
						<property name="curator" value="csp"/>
	                </organism>
	            </node>
	            <node name="Trypanosoma" page="true">
	                <organism name="Tcongolense">
						<property name="taxonId" value="5692" />
						<property name="fullName" value="Trypanosoma congolense" />
						<property name="nickname" value="tcongolense" />
		                <property name="dbName" value="GeneDB_Tcongolense" />
						<property name="transTable" value="1"/>
						<property name="mitoTransTable" value="4"/>
						<property name="curator" value="chf"/>
					</organism>
					<organism name="Tbruceibrucei427">
	                    <property name="taxonId" value="5761" />
						<property name="fullName" value="Trypanosoma brucei brucei, strain 427" />
						<property name="nickname" value="tbrucei427" />
						<property name="dbName" value="GeneDB_Tbrucei427" />
						<property name="transTable" value=""/>
						<property name="mitoTransTable" value=""/>
						<property name="curator" value="chf"/>
	                </organism>
	                <organism name="Tbruceibrucei927">
	                    <property name="taxonId" value="185431" />
						<property name="fullName" value="Trypanosoma brucei brucei, strain 927" />
						<property name="nickname" value="tryp" />
						<property name="dbName" value="GeneDB_Tbrucei927" />
						<property name="transTable" value="1"/>
						<property name="mitoTransTable" value="4"/>
						<property name="curator" value="chf"/>
	                </organism>
	                <organism name="Tbruceigambiense">
	                    <property name="taxonId" value="31285" />
						<property name="fullName" value="Trypanosoma brucei gambiense" />
	                    <property name="nickname" value="tgambiense" />
						<property name="dbName" value="GeneDB_Tgambiense" />
						<property name="transTable" value="1"/>
						<property name="mitoTransTable" value="4"/>
						<property name="curator" value="chf"/>
	                </organism>
	                <organism name="Tvivax">
	                    <property name="taxonId" value="5699" />
	                    <property name="fullName" value="Trypanosoma vivax" />
	                    <property name="nickname" value="tvivax" />
	                    <property name="dbName" value="GeneDB_Tvivax" />
						<property name="transTable" value="1"/>
						<property name="mitoTransTable" value="4"/>
						<property name="curator" value="chf"/>
	                </organism>
	                <organism name="Tcruzi">
	                    <property name="taxonId" value="5693" />
	                    <property name="fullName" value="Trypanosoma cruzi" />
	                    <property name="nickname" value="tcruzi" />
	                    <property name="dbName" value="GeneDB_Tcruzi" />
						<property name="transTable" value="1"/>
						<property name="mitoTransTable" value="4"/>
						<property name="curator" value="chf"/>
	                </organism>
	            </node>
	            <organism name="Ddiscoideum">
					<property name="taxonId" value="44689" />
					<property name="fullName" value="Dictyostelium discoideum" />
					<property name="nickname" value="dicty" />
	                <property name="dbName" value="GeneDB_Ddiscoideum" />
					<property name="transTable" value="1"/>
					<property name="mitoTransTable" value="1"/>
					<property name="curator" value="mar"/>
				</organism>
				<organism name="Ehistolytica">
					<property name="taxonId" value="5759" />
					<property name="fullName" value="Entamoeba histolytica" />
					<property name="nickname" value="ehistolytica" />
	                <property name="dbName" value="GeneDB_Ehistolytica" />
					<property name="transTable" value="1"/>
					<property name="mitoTransTable" value="1"/>
					<property name="curator" value="mb4"/>
				</organism>
				<node name="Apicomplexa">
					<organism name="Etenella">
						<property name="taxonId" value="5802" />
						<property name="fullName" value="Eimeria tenella" />
						<property name="nickname" value="etenella" />
		                <property name="dbName" value="GeneDB_Etenella" />
						<property name="transTable" value="1"/>
						<property name="mitoTransTable" value="4"/>
						<property name="curator" value="mar"/>
				  	</organism>
					<organism name="Tannulata">
						<property name="taxonId" value="5874" />				
						<property name="fullName" value="Theileria annulata" />
						<property name="nickname" value="annulata" />
		                <property name="dbName" value="GeneDB_Tannulata" />
						<property name="transTable" value="1"/>
						<property name="mitoTransTable" value="4"/>
						<property name="curator" value="ap2"/>
					</organism>
	                <node name="Plasmodia" page="true">
	                    <organism name="Pfalciparum">
	                        <property name="taxonId" value="5833" />
	                        <property name="fullName" value="Plasmodium falciparum" />
	                        <property name="nickname" value="malaria" />
	                        <property name="dbName" value="GeneDB_Pfalciparum" />
							<property name="transTable" value="1"/>
						    <property name="mitoTransTable" value="4"/>
							<property name="curator" value="aeb"/>
	                    </organism>
	                    <organism name="Pknowlesi">
	                        <property name="taxonId" value="5850" />
	                        <property name="fullName" value="Plasmodium knowlesi" />
	                        <property name="nickname" value="pknowlesi" />
	                        <property name="dbName" value="GeneDB_Pknowlesi" />
							<property name="transTable" value="1"/>
							<property name="mitoTransTable" value="4"/>
							<property name="curator" value="aeb"/>
	                    </organism>
	                    <organism name="Pberghei">
	                        <property name="taxonId" value="5821" />
	                        <property name="fullName" value="Plasmodium berghei" />
	                        <property name="nickname" value="pberghei" />
	                        <property name="dbName" value="GeneDB_Pberghei" />
							<property name="transTable" value="1"/>
							<property name="mitoTransTable" value="4"/>
							<property name="curator" value="aeb"/>
	                    </organism>
	                    <organism name="Pchabaudi">
	                        <property name="taxonId" value="5825" />
	                        <property name="fullName" value="Plasmodium chabaudi" />
	                        <property name="nickname" value="pchabaudi" />
	                        <property name="dbName" value="GeneDB_Pchabaudi" />
							<property name="transTable" value="1"/>
							<property name="mitoTransTable" value="4"/>
							<property name="curator" value="aeb"/>
	                    </organism>
	                </node>
	            </node>
			</node>
		</node>
        <node name="Fungi">
            <organism name="Scerevisiae">
                <property name="taxonId" value="4932" />
                <property name="fullName" value="Saccharomyces cerevisiae" />
                <property name="nickname" value="cerevisiae" />
                <property name="dbName" value="GeneDB_Scerevisiae" />
				<property name="transTable" value="1"/>
				<property name="mitoTransTable" value="3"/>
				<property name="curator" value="val"/>
            </organism>
            <organism name="Spombe">
                <property name="taxonId" value="4896" />
                <property name="fullName" value="Schizosaccharomyces pombe" />
                <property name="nickname" value="pombe" />
                <property name="dbName" value="GeneDB_Spombe" />
				<property name="transTable" value="1"/>
				<property name="mitoTransTable" value="4"/>
				<property name="curator" value="val"/>
            </organism>
            <organism name="Afumigatus">
                <property name="taxonId" value="5085" />
                <property name="fullName" value="Aspergillus fumigatus" />
                <property name="nickname" value="asp" />
                <property name="dbName" value="GeneDB_Afumigatus" />
				<property name="transTable" value="1"/>
				<property name="mitoTransTable" value="4"/>
				<property name="curator" value="mb4"/>
            </organism>
			<organism name="Cdubliniensis">
				<property name="taxonId" value="42374" />
				<property name="fullName" value="Candida dubliniensis" />
				<property name="nickname" value="cdubliniensis" />
                <property name="dbName" value="GeneDB_Cdubliniensis" />
				<property name="transTable" value="12"/>
				<property name="mitoTransTable" value="3"/>
				<property name="curator" value="mb4"/>
			</organism>
        </node>
		<node name="bacteria">
			<organism name="Bmarinus">
				<property name="taxonId" value="97084" />
				<property name="fullName" value="Bacteriovorax marinus" />
				<property name="newOrg" value="true" />
            	<property name="dbName" value="GeneDB_Bmarinus" />
				<property name="transTable" value="11"/>
				<property name="curator" value="sdb"/>
			</organism>
			<organism name="Bfragilis_NCTC9343">
				<property name="taxonId" value="272559" />
				<property name="fullName" value="Bacteroides fragilis NCTC 9343" />
				<property name="nickname" value="bfragilis" />
            	<property name="dbName" value="GeneDB_Bfragilis" />
				<property name="transTable" value="11"/>
				<property name="curator" value="amct"/>
			</organism>
			<organism name="Cjejuni">
				<property name="taxonId" value="197" />
				<property name="fullName" value="Campylobacter jejuni" />
				<property name="nickname" value="cjejuni" />
            	<property name="dbName" value="GeneDB_Cjejuni" />
				<property name="transTable" value="11"/>
				<property name="curator" value="sdb"/>
			</organism>
			<organism name="Ctrachomatis">
				<property name="taxonId" value="813" />
				<property name="fullName" value="Chlamydia trachomatis" />
				<property name="nickname" value="testing" />
            	<property name="dbName" value="GeneDB_Ctrachomatis" />
				<property name="transTable" value="11"/>
				<property name="curator" value="nrt"/>
			</organism>
			<organism name="Cabortus">
				<property name="taxonId" value="83555" />
				<property name="fullName" value="Chlamydophila abortus" />
				<property name="nickname" value="cabortus" />
            	<property name="dbName" value="GeneDB_Cabortus" />
				<property name="transTable" value="11"/>
				<property name="curator" value="nrt"/>
			</organism>
			<organism name="Cmichiganensis">
				<property name="taxonId" value="28447" />
				<property name="fullName" value="Clavibacter michiganensis" />
				<property name="newOrg" value="true" />
            	<property name="dbName" value="GeneDB_Cmichiganensis" />
				<property name="transTable" value="11"/>
				<property name="curator" value="sdb"/>
			</organism>
			<organism name="Cdiphtheriae">
				<property name="taxonId" value="1717" />
				<property name="fullName" value="Corynebacterium diphtheriae" />
				<property name="nickname" value="diphtheria" />
            	<property name="dbName" value="GeneDB_Cdiphtheriae" />
				<property name="transTable" value="11"/>
				<property name="curator" value="amct"/>
			</organism>
			<organism name="Ecarotovora">
				<property name="taxonId" value="554" />
				<property name="fullName" value="Erwinia carotovora" />
				<property name="nickname" value="ecarot" />
            	<property name="dbName" value="GeneDB_Ecarotovora" />
				<property name="transTable" value="11"/>
				<property name="curator" value="ms5"/>
			</organism>
			<organism name="Ecoli_042">
				<property name="taxonId" value="216592" />
				<property name="fullName" value="Escherichia coli 042" />
				<property name="nickname" value="ecoli" />
            	<property name="dbName" value="GeneDB_Ecoli" />
				<property name="transTable" value="11"/>
				<property name="curator" value="net"/>
			</organism>
			<organism name="Pfluorescens">
				<property name="taxonId" value="294" />
				<property name="fullName" value="Pseudomonas fluorescens" />
				<property name="newOrg" value="true" />
            	<property name="dbName" value="GeneDB_Pfluorescens" />
				<property name="transTable" value="11"/>
				<property name="curator" value="amct"/>
			</organism>
			<organism name="Rleguminosarum">
				<property name="taxonId" value="384" />
				<property name="fullName" value="Rhizobium leguminosarum" />
				<property name="nickname" value="rleguminosarum" />
            	<property name="dbName" value="GeneDB_Rleguminosarum" />
				<property name="transTable" value="11"/>
				<property name="curator" value="lcc"/>
			</organism>
			<node name="Bordetella">
				<organism name="Bavium_197N">
					<property name="taxonId" value="360910" />
					<property name="fullName" value="Bordetella avium 197N" />
					<property name="nickname" value="bavium" />
	            	<property name="dbName" value="GeneDB_Bavium" />
					<property name="transTable" value="11"/>
					<property name="curator" value="ms5"/>
				</organism>
				<organism name="Bbronchiseptica">
					<property name="taxonId" value="518" />
					<property name="fullName" value="Bordetella bronchiseptica" />
					<property name="nickname" value="bronchi" />
	            	<property name="dbName" value="GeneDB_Bbronchiseptica" />
					<property name="transTable" value="11"/>
					<property name="curator" value="ms5"/>
				</organism>
				<organism name="Bparapertussis">
					<property name="taxonId" value="519" />
					<property name="fullName" value="Bordetella parapertussis" />
					<property name="nickname" value="parapert" />
	            	<property name="dbName" value="GeneDB_Bparapertussis" />
					<property name="transTable" value="11"/>
					<property name="curator" value="ms5"/>
				</organism>
				<organism name="Bpertussis">
					<property name="taxonId" value="520" />
					<property name="fullName" value="Bordetella pertussis" />
					<property name="nickname" value="pert" />
	            	<property name="dbName" value="GeneDB_Bpertussis" />
					<property name="transTable" value="11"/>
					<property name="curator" value="ms5"/>
				</organism>
			</node>
			<node name="Burkholderia">
				<organism name="Bcenocepacia">
					<property name="taxonId" value="95486" />
					<property name="fullName" value="Burkholderia cenocepacia" />
					<property name="nickname" value="bcenocepacia" />
	            	<property name="dbName" value="GeneDB_Bcenocepacia" />
					<property name="transTable" value="11"/>
					<property name="curator" value="mh3"/>
				</organism>
				<organism name="Bpseudomallei">
					<property name="taxonId" value="28450" />
					<property name="fullName" value="Burkholderia pseudomallei" />
					<property name="nickname" value="bpseudomallei" />
	            	<property name="dbName" value="GeneDB_Bpseudomallei" />
					<property name="transTable" value="11"/>
					<property name="curator" value="mh3"/>
				</organism>
			</node>
			<node name="Clostridium">
				<organism name="Cbotulinum">
					<property name="taxonId" value="1491" />
					<property name="fullName" value="Clostridium botulinum" />
					<property name="nickname" value="cbotulinum" />
	            	<property name="dbName" value="GeneDB_Cbotulinum" />
					<property name="transTable" value="11"/>
					<property name="curator" value="ms5"/>
				</organism>
				<organism name="Cdifficile">
					<property name="taxonId" value="1496" />
					<property name="fullName" value="Clostridium difficile" />
					<property name="nickname" value="cdifficile" />
	            	<property name="dbName" value="GeneDB_Cdifficile" />
					<property name="transTable" value="11"/>
					<property name="curator" value="ms5"/>
				</organism>
			</node>
			<node name="Mycobacterium">
				<organism name="Mbovis">
					<property name="taxonId" value="1765" />
					<property name="fullName" value="Mycobacterium bovis" />
					<property name="nickname" value="mbovis" />
	            	<property name="dbName" value="GeneDB_Mbovis" />
					<property name="transTable" value="11"/>
					<property name="curator" value="parkhill"/>
				</organism>
				<organism name="Mleprae">
					<property name="taxonId" value="1769" />
					<property name="fullName" value="Mycobacterium leprae" />
					<property name="nickname" value="mleprae" />
	            	<property name="dbName" value="GeneDB_Mleprae" />
					<property name="transTable" value="11"/>
					<property name="curator" value="parkhill"/>
				</organism>
				<organism name="Mmarinum">
					<property name="taxonId" value="1781" />
					<property name="fullName" value="Mycobacterium marinum" />
					<property name="nickname" value="mmarinum" />
	            	<property name="dbName" value="GeneDB_Mmarinum" />
					<property name="transTable" value="11"/>
					<property name="curator" value="parkhill"/>
				</organism>
				<organism name="Mtuberculosis">
					<property name="taxonId" value="1773" />
					<property name="fullName" value="Mycobacterium tuberculosis" />
					<property name="nickname" value="mtuberculosis" />
	            	<property name="dbName" value="GeneDB_Mtuberculosis" />
					<property name="transTable" value="11"/>
					<property name="curator" value="nrt"/>
				</organism>
			</node>
			<node name="Neisseria">
				<organism name="Nmeningitidis">
					<property name="taxonId" value="487" />
					<property name="fullName" value="Neisseria meningitidis" />
					<property name="nickname" value="nmeningitidis" />
	            	<property name="dbName" value="GeneDB_Nmeningitidis" />
					<property name="transTable" value="11"/>
					<property name="curator" value="sdb"/>
				</organism>
				<organism name="Nmeningitidis_C">
					<property name="taxonId" value="272831" />
					<property name="fullName" value="Neisseria meningitidis C" />
					<property name="nickname" value="nmeningitidisC" />
	            	<property name="dbName" value="GeneDB_NmeningitidisC" />
					<property name="transTable" value="11"/>
					<property name="curator" value="sdb"/>
				</organism>
			</node>
			<node name="Salmonella" page="true">
				<organism name="Sbongori">
					<property name="taxonId" value="54736" />
					<property name="fullName" value="Salmonella bongori" />
					<property name="newOrg" value="true" />
	            	<property name="dbName" value="GeneDB_Sbongori" />
					<property name="transTable" value="11"/>
					<property name="curator" value="nrt"/>
				</organism>	
				<organism name="Senteritidis_PT4">
					<property name="taxonId" value="592" />
					<property name="fullName" value="Salmonella enteritidis PT4" />
					<property name="newOrg" value="true" />
	            	<property name="dbName" value="GeneDB_Senteritidis_PT4" />
					<property name="transTable" value="11"/>
					<property name="curator" value="nrt"/>
				</organism>
			</node>
			<organism name="Smarcescens">
					<property name="taxonId" value="615" />
					<property name="fullName" value="Serratia marcescens" />
					<property name="newOrg" value="true" />
	            	<property name="dbName" value="GeneDB_Smarcescens" />
					<property name="transTable" value="11"/>
					<property name="curator" value="nrt"/>
			</organism>
			<node name="Staphylococcus">
				<organism name="Saureus_MRSA252">
					<property name="taxonId" value="282458" />
					<property name="fullName" value="Staphylococcus aureus (MRSA252)" />
					<property name="nickname" value="saureusMRSA" />
	            	<property name="dbName" value="GeneDB_SaureusMRSA" />
					<property name="transTable" value="11"/>
					<property name="curator" value="mh3"/>
				</organism>	
				<organism name="Saureus_MSSA476">
					<property name="taxonId" value="282459" />
					<property name="fullName" value="Staphylococcus aureus (MSSA476)" />
					<property name="nickname" value="saureusMSSA476" />
	            	<property name="dbName" value="GeneDB_SaureusMSSA" />
					<property name="transTable" value="11"/>
					<property name="curator" value="mh3"/>
				</organism>
			</node>
			<organism name="Smaltophilia">
				<property name="taxonId" value="40324" />
				<property name="fullName" value="Stenotrophomonas maltophilia" />
				<property name="newOrg" value="true" />
            	<property name="dbName" value="GeneDB_Smaltophilia" />
				<property name="transTable" value="11"/>
				<property name="curator" value="lcc"/>
			</organism>
			<node name="Streptococcus">
				<organism name="Spyogenes">
					<property name="taxonId" value="1314" />
					<property name="fullName" value="Streptococcus pyogenes" />
					<property name="nickname" value="spyogenes" />
	            	<property name="dbName" value="GeneDB_Spyogenes" />
					<property name="transTable" value="11"/>
					<property name="curator" value="mh3"/>
				</organism>	
				<organism name="Suberis">
					<property name="taxonId" value="1349" />
					<property name="fullName" value="Streptococcus uberis" />
					<property name="nickname" value="suberis" />
	            	<property name="dbName" value="GeneDB_Suberis" />
					<property name="transTable" value="11"/>
					<property name="curator" value="mh3"/>
				</organism>
			</node>
			<organism name="Twhipplei">
				<property name="taxonId" value="2039" />
				<property name="fullName" value="Tropheryma whipplei" />
				<property name="nickname" value="twhipplei" />
            	<property name="dbName" value="GeneDB_Twhipplei" />
				<property name="transTable" value="11"/>
				<property name="curator" value="sdb"/>
			</organism>	
			<node name="Yersinia">
				<organism name="Ypestis">
					<property name="taxonId" value="632" />
					<property name="fullName" value="Yersinia pestis" />
					<property name="nickname" value="ypsetis" />
	            	<property name="dbName" value="GeneDB_Ypestis" />
					<property name="transTable" value="11"/>
					<property name="curator" value="nrt"/>
				</organism>	
				<organism name="Yenterocolitica">
					<property name="taxonId" value="630" />
					<property name="fullName" value="Yersinia enterocolitica" />
					<property name="newOrg" value="true" />
	            	<property name="dbName" value="GeneDB_Yenterocolitica" />
					<property name="transTable" value="11"/>
					<property name="curator" value="nrt"/>
				</organism>
			</node>
		</node>
    </node>
</org-heirachy>
''';
  
}