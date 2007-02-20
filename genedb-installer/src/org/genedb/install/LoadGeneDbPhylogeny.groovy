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
    
    def genedbMiscCvId;
    
    def cvTerm;
    
    def organismDbXRefSet;
    
    def nodePropDataSet;
    
    boolean writeBack = true;
    
    LoadGeneDbPhylogeny() {
		db = Sql.newInstance(
			'jdbc:postgresql://pathdbsrv1a.internal.sanger.ac.uk:10001/chado',
			'chado',
			'chado',
			'org.postgresql.Driver')
			
     	orgDataSet = db.dataSet("organism")
		
		orgPropDataSet = db.dataSet("organismprop")
		
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
    	LoadGeneDbPhylogeny lgp = new LoadGeneDbPhylogeny()
    	lgp.process(input);
	}
	

	// The XML config file for loading the organism and phylogeny modules
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
					<property name="translationTable" value="1"/>
					<property name="mitochondrialTranslationTable" value="9"/>
					<property name="curatorEmail" value="mb4"/>
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
						<property name="translationTable" value="1"/>
						<property name="mitochondrialTranslationTable" value="4"/>
						<property name="curatorEmail" value="csp"/>
					</organism>
		            <organism name="Lmajor">
		                <property name="taxonId" value="5664" />
		                <property name="fullName" value="Leishmania major" />
		                <property name="nickname" value="leish" />
		                <property name="dbName" value="GeneDB_Lmajor" />
						<property name="translationTable" value="1"/>
						<property name="mitochondrialTranslationTable" value="4"/>
						<property name="curatorEmail" value="csp"/>
		            </organism>
	                <organism name="Linfantum">
	                    <property name="taxonId" value="5761" />
	                    <property name="fullName" value="Leishmania infantum" />
	                    <property name="nickname" value="linfantum" />
	                    <property name="dbName" value="GeneDB_Linfantum" />
						<property name="translationTable" value="1"/>
						<property name="mitochondrialTranslationTable" value="4"/>
						<property name="curatorEmail" value="csp"/>
	                </organism>
	            </node>
	            <node name="Trypanosoma" page="true">
	                <organism name="Tcongolense">
						<property name="taxonId" value="5692" />
						<property name="fullName" value="Trypanosoma congolense" />
						<property name="nickname" value="tcongolense" />
		                <property name="dbName" value="GeneDB_Tcongolense" />
						<property name="translationTable" value="1"/>
						<property name="mitochondrialTranslationTable" value="4"/>
						<property name="curatorEmail" value="chf"/>
					</organism>
					<organism name="Tbruceibrucei427">
	                    <property name="taxonId" value="5761" />
						<property name="fullName" value="Trypanosoma brucei brucei, strain 427" />
						<property name="nickname" value="tbrucei427" />
						<property name="dbName" value="GeneDB_Tbrucei427" />
						<property name="translationTable" value=""/>
						<property name="mitochondrialTranslationTable" value=""/>
						<property name="curatorEmail" value="chf"/>
	                </organism>
	                <organism name="Tbruceibrucei927">
	                    <property name="taxonId" value="185431" />
						<property name="fullName" value="Trypanosoma brucei brucei, strain 927" />
						<property name="nickname" value="tryp" />
						<property name="dbName" value="GeneDB_Tbrucei927" />
						<property name="translationTable" value="1"/>
						<property name="mitochondrialTranslationTable" value="4"/>
						<property name="curatorEmail" value="chf"/>
	                </organism>
	                <organism name="Tbruceigambiense">
	                    <property name="taxonId" value="31285" />
						<property name="fullName" value="Trypanosoma brucei gambiense" />
	                    <property name="nickname" value="tgambiense" />
						<property name="dbName" value="GeneDB_Tgambiense" />
						<property name="translationTable" value="1"/>
						<property name="mitochondrialTranslationTable" value="4"/>
						<property name="curatorEmail" value="chf"/>
	                </organism>
	                <organism name="Tvivax">
	                    <property name="taxonId" value="5699" />
	                    <property name="fullName" value="Trypanosoma vivax" />
	                    <property name="nickname" value="tvivax" />
	                    <property name="dbName" value="GeneDB_Tvivax" />
						<property name="translationTable" value="1"/>
						<property name="mitochondrialTranslationTable" value="4"/>
						<property name="curatorEmail" value="chf"/>
	                </organism>
	                <organism name="Tcruzi">
	                    <property name="taxonId" value="5693" />
	                    <property name="fullName" value="Trypanosoma cruzi" />
	                    <property name="nickname" value="tcruzi" />
	                    <property name="dbName" value="GeneDB_Tcruzi" />
						<property name="translationTable" value="1"/>
						<property name="mitochondrialTranslationTable" value="4"/>
						<property name="curatorEmail" value="chf"/>
	                </organism>
	            </node>
	            <organism name="Ddiscoideum">
					<property name="taxonId" value="44689" />
					<property name="fullName" value="Dictyostelium discoideum" />
					<property name="nickname" value="dicty" />
	                <property name="dbName" value="GeneDB_Ddiscoideum" />
					<property name="translationTable" value="1"/>
					<property name="mitochondrialTranslationTable" value="1"/>
					<property name="curatorEmail" value="mar"/>
				</organism>
				<organism name="Ehistolytica">
					<property name="taxonId" value="5759" />
					<property name="fullName" value="Entamoeba histolytica" />
					<property name="nickname" value="ehistolytica" />
	                <property name="dbName" value="GeneDB_Ehistolytica" />
					<property name="translationTable" value="1"/>
					<property name="mitochondrialTranslationTable" value="1"/>
					<property name="curatorEmail" value="mb4"/>
				</organism>
				<node name="Apicomplexa">
					<organism name="Etenella">
						<property name="taxonId" value="5802" />
						<property name="fullName" value="Eimeria tenella" />
						<property name="nickname" value="etenella" />
		                <property name="dbName" value="GeneDB_Etenella" />
						<property name="translationTable" value="1"/>
						<property name="mitochondrialTranslationTable" value="4"/>
						<property name="curatorEmail" value="mar"/>
				  	</organism>
					<organism name="Tannulata">
						<property name="taxonId" value="5874" />				
						<property name="fullName" value="Theileria annulata" />
						<property name="nickname" value="annulata" />
		                <property name="dbName" value="GeneDB_Tannulata" />
						<property name="translationTable" value="1"/>
						<property name="mitochondrialTranslationTable" value="4"/>
						<property name="curatorEmail" value="ap2"/>
					</organism>
	                <node name="Plasmodia" page="true">
	                    <organism name="Pfalciparum">
	                        <property name="taxonId" value="5833" />
	                        <property name="fullName" value="Plasmodium falciparum" />
	                        <property name="nickname" value="malaria" />
	                        <property name="dbName" value="GeneDB_Pfalciparum" />
							<property name="translationTable" value="1"/>
						    <property name="mitochondrialTranslationTable" value="4"/>
							<property name="curatorEmail" value="aeb"/>
	                    </organism>
	                    <organism name="Pknowlesi">
	                        <property name="taxonId" value="5850" />
	                        <property name="fullName" value="Plasmodium knowlesi" />
	                        <property name="nickname" value="pknowlesi" />
	                        <property name="dbName" value="GeneDB_Pknowlesi" />
							<property name="translationTable" value="1"/>
							<property name="mitochondrialTranslationTable" value="4"/>
							<property name="curatorEmail" value="aeb"/>
	                    </organism>
	                    <organism name="Pberghei">
	                        <property name="taxonId" value="5821" />
	                        <property name="fullName" value="Plasmodium berghei" />
	                        <property name="nickname" value="pberghei" />
	                        <property name="dbName" value="GeneDB_Pberghei" />
							<property name="translationTable" value="1"/>
							<property name="mitochondrialTranslationTable" value="4"/>
							<property name="curatorEmail" value="aeb"/>
	                    </organism>
	                    <organism name="Pchabaudi">
	                        <property name="taxonId" value="5825" />
	                        <property name="fullName" value="Plasmodium chabaudi" />
	                        <property name="nickname" value="pchabaudi" />
	                        <property name="dbName" value="GeneDB_Pchabaudi" />
							<property name="translationTable" value="1"/>
							<property name="mitochondrialTranslationTable" value="4"/>
							<property name="curatorEmail" value="aeb"/>
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
				<property name="translationTable" value="1"/>
				<property name="mitochondrialTranslationTable" value="3"/>
				<property name="curatorEmail" value="val"/>
            </organism>
            <organism name="Spombe">
                <property name="taxonId" value="4896" />
                <property name="fullName" value="Schizosaccharomyces pombe" />
                <property name="nickname" value="pombe" />
                <property name="dbName" value="GeneDB_Spombe" />
				<property name="translationTable" value="1"/>
				<property name="mitochondrialTranslationTable" value="4"/>
				<property name="curatorEmail" value="val"/>
            </organism>
            <organism name="Afumigatus">
                <property name="taxonId" value="5085" />
                <property name="fullName" value="Aspergillus fumigatus" />
                <property name="nickname" value="asp" />
                <property name="dbName" value="GeneDB_Afumigatus" />
				<property name="translationTable" value="1"/>
				<property name="mitochondrialTranslationTable" value="4"/>
				<property name="curatorEmail" value="mb4"/>
            </organism>
			<organism name="Cdubliniensis">
				<property name="taxonId" value="42374" />
				<property name="fullName" value="Candida dubliniensis" />
				<property name="nickname" value="cdubliniensis" />
                <property name="dbName" value="GeneDB_Cdubliniensis" />
				<property name="translationTable" value="12"/>
				<property name="mitochondrialTranslationTable" value="3"/>
				<property name="curatorEmail" value="mb4"/>
			</organism>
        </node>
		<node name="bacteria">
			<organism name="Bmarinus">
				<property name="taxonId" value="97084" />
				<property name="fullName" value="Bacteriovorax marinus" />
				<property name="newOrg" value="true" />
            	<property name="dbName" value="GeneDB_Bmarinus" />
				<property name="translationTable" value="11"/>
				<property name="curatorEmail" value="sdb"/>
			</organism>
			<organism name="Bfragilis_NCTC9343">
				<property name="taxonId" value="272559" />
				<property name="fullName" value="Bacteroides fragilis NCTC 9343" />
				<property name="nickname" value="bfragilis" />
            	<property name="dbName" value="GeneDB_Bfragilis" />
				<property name="translationTable" value="11"/>
				<property name="curatorEmail" value="amct"/>
			</organism>
			<organism name="Cjejuni">
				<property name="taxonId" value="197" />
				<property name="fullName" value="Campylobacter jejuni" />
				<property name="nickname" value="cjejuni" />
            	<property name="dbName" value="GeneDB_Cjejuni" />
				<property name="translationTable" value="11"/>
				<property name="curatorEmail" value="sdb"/>
			</organism>
			<organism name="Ctrachomatis">
				<property name="taxonId" value="813" />
				<property name="fullName" value="Chlamydia trachomatis" />
				<property name="nickname" value="testing" />
            	<property name="dbName" value="GeneDB_Ctrachomatis" />
				<property name="translationTable" value="11"/>
				<property name="curatorEmail" value="nrt"/>
			</organism>
			<organism name="Cabortus">
				<property name="taxonId" value="83555" />
				<property name="fullName" value="Chlamydophila abortus" />
				<property name="nickname" value="cabortus" />
            	<property name="dbName" value="GeneDB_Cabortus" />
				<property name="translationTable" value="11"/>
				<property name="curatorEmail" value="nrt"/>
			</organism>
			<organism name="Cmichiganensis">
				<property name="taxonId" value="28447" />
				<property name="fullName" value="Clavibacter michiganensis" />
				<property name="newOrg" value="true" />
            	<property name="dbName" value="GeneDB_Cmichiganensis" />
				<property name="translationTable" value="11"/>
				<property name="curatorEmail" value="sdb"/>
			</organism>
			<organism name="Cdiphtheriae">
				<property name="taxonId" value="1717" />
				<property name="fullName" value="Corynebacterium diphtheriae" />
				<property name="nickname" value="diphtheria" />
            	<property name="dbName" value="GeneDB_Cdiphtheriae" />
				<property name="translationTable" value="11"/>
				<property name="curatorEmail" value="amct"/>
			</organism>
			<organism name="Ecarotovora">
				<property name="taxonId" value="554" />
				<property name="fullName" value="Erwinia carotovora" />
				<property name="nickname" value="ecarot" />
            	<property name="dbName" value="GeneDB_Ecarotovora" />
				<property name="translationTable" value="11"/>
				<property name="curatorEmail" value="ms5"/>
			</organism>
			<organism name="Ecoli_042">
				<property name="taxonId" value="216592" />
				<property name="fullName" value="Escherichia coli 042" />
				<property name="nickname" value="ecoli" />
            	<property name="dbName" value="GeneDB_Ecoli" />
				<property name="translationTable" value="11"/>
				<property name="curatorEmail" value="net"/>
			</organism>
			<organism name="Pfluorescens">
				<property name="taxonId" value="294" />
				<property name="fullName" value="Pseudomonas fluorescens" />
				<property name="newOrg" value="true" />
            	<property name="dbName" value="GeneDB_Pfluorescens" />
				<property name="translationTable" value="11"/>
				<property name="curatorEmail" value="amct"/>
			</organism>
			<organism name="Rleguminosarum">
				<property name="taxonId" value="384" />
				<property name="fullName" value="Rhizobium leguminosarum" />
				<property name="nickname" value="rleguminosarum" />
            	<property name="dbName" value="GeneDB_Rleguminosarum" />
				<property name="translationTable" value="11"/>
				<property name="curatorEmail" value="lcc"/>
			</organism>
			<node name="Bordetella">
				<organism name="Bavium_197N">
					<property name="taxonId" value="360910" />
					<property name="fullName" value="Bordetella avium 197N" />
					<property name="nickname" value="bavium" />
	            	<property name="dbName" value="GeneDB_Bavium" />
					<property name="translationTable" value="11"/>
					<property name="curatorEmail" value="ms5"/>
				</organism>
				<organism name="Bbronchiseptica">
					<property name="taxonId" value="518" />
					<property name="fullName" value="Bordetella bronchiseptica" />
					<property name="nickname" value="bronchi" />
	            	<property name="dbName" value="GeneDB_Bbronchiseptica" />
					<property name="translationTable" value="11"/>
					<property name="curatorEmail" value="ms5"/>
				</organism>
				<organism name="Bparapertussis">
					<property name="taxonId" value="519" />
					<property name="fullName" value="Bordetella parapertussis" />
					<property name="nickname" value="parapert" />
	            	<property name="dbName" value="GeneDB_Bparapertussis" />
					<property name="translationTable" value="11"/>
					<property name="curatorEmail" value="ms5"/>
				</organism>
				<organism name="Bpertussis">
					<property name="taxonId" value="520" />
					<property name="fullName" value="Bordetella pertussis" />
					<property name="nickname" value="pert" />
	            	<property name="dbName" value="GeneDB_Bpertussis" />
					<property name="translationTable" value="11"/>
					<property name="curatorEmail" value="ms5"/>
				</organism>
			</node>
			<node name="Burkholderia">
				<organism name="Bcenocepacia">
					<property name="taxonId" value="95486" />
					<property name="fullName" value="Burkholderia cenocepacia" />
					<property name="nickname" value="bcenocepacia" />
	            	<property name="dbName" value="GeneDB_Bcenocepacia" />
					<property name="translationTable" value="11"/>
					<property name="curatorEmail" value="mh3"/>
				</organism>
				<organism name="Bpseudomallei">
					<property name="taxonId" value="28450" />
					<property name="fullName" value="Burkholderia pseudomallei" />
					<property name="nickname" value="bpseudomallei" />
	            	<property name="dbName" value="GeneDB_Bpseudomallei" />
					<property name="translationTable" value="11"/>
					<property name="curatorEmail" value="mh3"/>
				</organism>
			</node>
			<node name="Clostridium">
				<organism name="Cbotulinum">
					<property name="taxonId" value="1491" />
					<property name="fullName" value="Clostridium botulinum" />
					<property name="nickname" value="cbotulinum" />
	            	<property name="dbName" value="GeneDB_Cbotulinum" />
					<property name="translationTable" value="11"/>
					<property name="curatorEmail" value="ms5"/>
				</organism>
				<organism name="Cdifficile">
					<property name="taxonId" value="1496" />
					<property name="fullName" value="Clostridium difficile" />
					<property name="nickname" value="cdifficile" />
	            	<property name="dbName" value="GeneDB_Cdifficile" />
					<property name="translationTable" value="11"/>
					<property name="curatorEmail" value="ms5"/>
				</organism>
			</node>
			<node name="Mycobacterium">
				<organism name="Mbovis">
					<property name="taxonId" value="1765" />
					<property name="fullName" value="Mycobacterium bovis" />
					<property name="nickname" value="mbovis" />
	            	<property name="dbName" value="GeneDB_Mbovis" />
					<property name="translationTable" value="11"/>
					<property name="curatorEmail" value="parkhill"/>
				</organism>
				<organism name="Mleprae">
					<property name="taxonId" value="1769" />
					<property name="fullName" value="Mycobacterium leprae" />
					<property name="nickname" value="mleprae" />
	            	<property name="dbName" value="GeneDB_Mleprae" />
					<property name="translationTable" value="11"/>
					<property name="curatorEmail" value="parkhill"/>
				</organism>
				<organism name="Mmarinum">
					<property name="taxonId" value="1781" />
					<property name="fullName" value="Mycobacterium marinum" />
					<property name="nickname" value="mmarinum" />
	            	<property name="dbName" value="GeneDB_Mmarinum" />
					<property name="translationTable" value="11"/>
					<property name="curatorEmail" value="parkhill"/>
				</organism>
				<organism name="Mtuberculosis">
					<property name="taxonId" value="1773" />
					<property name="fullName" value="Mycobacterium tuberculosis" />
					<property name="nickname" value="mtuberculosis" />
	            	<property name="dbName" value="GeneDB_Mtuberculosis" />
					<property name="translationTable" value="11"/>
					<property name="curatorEmail" value="nrt"/>
				</organism>
			</node>
			<node name="Neisseria">
				<organism name="Nmeningitidis">
					<property name="taxonId" value="487" />
					<property name="fullName" value="Neisseria meningitidis" />
					<property name="nickname" value="nmeningitidis" />
	            	<property name="dbName" value="GeneDB_Nmeningitidis" />
					<property name="translationTable" value="11"/>
					<property name="curatorEmail" value="sdb"/>
				</organism>
				<organism name="Nmeningitidis_C">
					<property name="taxonId" value="272831" />
					<property name="fullName" value="Neisseria meningitidis C" />
					<property name="nickname" value="nmeningitidisC" />
	            	<property name="dbName" value="GeneDB_NmeningitidisC" />
					<property name="translationTable" value="11"/>
					<property name="curatorEmail" value="sdb"/>
				</organism>
			</node>
			<node name="Salmonella" page="true">
				<organism name="Sbongori">
					<property name="taxonId" value="54736" />
					<property name="fullName" value="Salmonella bongori" />
					<property name="newOrg" value="true" />
	            	<property name="dbName" value="GeneDB_Sbongori" />
					<property name="translationTable" value="11"/>
					<property name="curatorEmail" value="nrt"/>
				</organism>	
				<organism name="Senteritidis_PT4">
					<property name="taxonId" value="592" />
					<property name="fullName" value="Salmonella enteritidis PT4" />
					<property name="newOrg" value="true" />
	            	<property name="dbName" value="GeneDB_Senteritidis_PT4" />
					<property name="translationTable" value="11"/>
					<property name="curatorEmail" value="nrt"/>
				</organism>
			</node>
			<organism name="Smarcescens">
					<property name="taxonId" value="615" />
					<property name="fullName" value="Serratia marcescens" />
					<property name="newOrg" value="true" />
	            	<property name="dbName" value="GeneDB_Smarcescens" />
					<property name="translationTable" value="11"/>
					<property name="curatorEmail" value="nrt"/>
			</organism>
			<node name="Staphylococcus">
				<organism name="Saureus_MRSA252">
					<property name="taxonId" value="282458" />
					<property name="fullName" value="Staphylococcus aureus (MRSA252)" />
					<property name="nickname" value="saureusMRSA" />
	            	<property name="dbName" value="GeneDB_SaureusMRSA" />
					<property name="translationTable" value="11"/>
					<property name="curatorEmail" value="mh3"/>
				</organism>	
				<organism name="Saureus_MSSA476">
					<property name="taxonId" value="282459" />
					<property name="fullName" value="Staphylococcus aureus (MSSA476)" />
					<property name="nickname" value="saureusMSSA476" />
	            	<property name="dbName" value="GeneDB_SaureusMSSA" />
					<property name="translationTable" value="11"/>
					<property name="curatorEmail" value="mh3"/>
				</organism>
			</node>
			<organism name="Smaltophilia">
				<property name="taxonId" value="40324" />
				<property name="fullName" value="Stenotrophomonas maltophilia" />
				<property name="newOrg" value="true" />
            	<property name="dbName" value="GeneDB_Smaltophilia" />
				<property name="translationTable" value="11"/>
				<property name="curatorEmail" value="lcc"/>
			</organism>
			<node name="Streptococcus">
				<organism name="Spyogenes">
					<property name="taxonId" value="1314" />
					<property name="fullName" value="Streptococcus pyogenes" />
					<property name="nickname" value="spyogenes" />
	            	<property name="dbName" value="GeneDB_Spyogenes" />
					<property name="translationTable" value="11"/>
					<property name="curatorEmail" value="mh3"/>
				</organism>	
				<organism name="Suberis">
					<property name="taxonId" value="1349" />
					<property name="fullName" value="Streptococcus uberis" />
					<property name="nickname" value="suberis" />
	            	<property name="dbName" value="GeneDB_Suberis" />
					<property name="translationTable" value="11"/>
					<property name="curatorEmail" value="mh3"/>
				</organism>
			</node>
			<organism name="Twhipplei">
				<property name="taxonId" value="2039" />
				<property name="fullName" value="Tropheryma whipplei" />
				<property name="nickname" value="twhipplei" />
            	<property name="dbName" value="GeneDB_Twhipplei" />
				<property name="translationTable" value="11"/>
				<property name="curatorEmail" value="sdb"/>
			</organism>	
			<node name="Yersinia">
				<organism name="Ypestis">
					<property name="taxonId" value="632" />
					<property name="fullName" value="Yersinia pestis" />
					<property name="nickname" value="ypsetis" />
	            	<property name="dbName" value="GeneDB_Ypestis" />
					<property name="translationTable" value="11"/>
					<property name="curatorEmail" value="nrt"/>
				</organism>	
				<organism name="Yenterocolitica">
					<property name="taxonId" value="630" />
					<property name="fullName" value="Yersinia enterocolitica" />
					<property name="newOrg" value="true" />
	            	<property name="dbName" value="GeneDB_Yenterocolitica" />
					<property name="translationTable" value="11"/>
					<property name="curatorEmail" value="nrt"/>
				</organism>
			</node>
		</node>
    </node>
</org-heirachy>
''';
  
}