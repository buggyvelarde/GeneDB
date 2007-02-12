package org.genedb.install;

import groovy.sql.Sql

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
    
    def dbXRefDataSet
    def dbXRefId
    
    LoadGeneDbPhylogeny() {
		db = Sql.newInstance(
			'jdbc:postgresql://holly.internal.sanger.ac.uk:9050/chado',
			'chado',
			'chado',
			'org.postgresql.Driver')
			
     	orgDataSet = db.dataSet("organism")
		orgId = getNextId("organism")
		
     	nodeDataSet = db.dataSet("phylonode")
		nodeId = getNextId("phylonode")
		
     	nodeOrgDataSet = db.dataSet("phylonode_organism")
		nodeOrgId = getNextId("phylonode_organism")
    }
        
    
	static void main(args) {
    	LoadGeneDbPhylogeny lgp = new LoadGeneDbPhylogeny()
    	lgp.process(input);
	}

	def getNextId(def table) {
	    def col = table + "_id";
	    def id = db.rows("SELECT max("+col+") as "+col+" from " + table)[0]["${col}"];
	    if (id == null) {
	        return 1
	    }
	    return id++;
	}
	
	void process(def inp) {
	    List trees = db.rows("SELECT * from phylotree where name='org_heirachy'");
	    def tree = trees[0]."phylotree_id";
	    def heirachy = new XmlSlurper().parseText(inp) 
  
	    index = 0
	    heirachy.node.children().each(
	            {setLeftAndRight(it, 1)}
        )	
	    
	    heirachy.node.children().each(
	            {createNode(it, null, tree)}
        )		
	}

	  
	  void setLeftAndRight(def node, def depth) {
	      if (node.name() != 'node' && node.name() != 'organism') {
	          return
	      }
	      node.attributes().put('depth', depth);
	      node.attributes().put('left', ++index);
	      ++depth;
	      node.children().each(
	              {setLeftAndRight(it, depth)}
	      )
	      node.attributes().put('right', ++index)     
	  }
	
	  
  void createNode(def node, def parent, def tree) {
      assert node != null
      switch (node.name()) {
          case 'node':
              def newNode = createPhylonode(node, parent, tree)
              node.children().each(
                      {createNode(it, newNode, tree)}
              )
              break
          case 'organism':
          	  def newNode = createPhylonode(node, parent, tree)
          	  def org = createOrganism(node)
              //nodeDataSet.add() {
          	  //    phylonode_id:    ,
//            phylotree_id:        tree,
//            phylonode_id: newNode,
//            organism_id: 	org
//            }
	
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
          	  })
      
          	  checkPropExists(props, node, "taxonId")
          	  checkPropExists(props, node, "transTable")
          	  //checkPropExists(props, "")
          	  if (!map.containsKey("nickname") && !map.containsKey("newOrg")) {
     	   		println "No nickname for '"+node.'@name'+"'"
              }
          	  map.removeKey("newOrg")
          	  
//          	  props.each(
//          	          {println it.key+"="+ it.value}
//          	  )
	          break
          default:
              throw new RuntimeException("Saw a node of '"+node.name()+"' when expecting node or organism");
      }
  }
  
  void checkPropExists(def map, def node, String key) {
	  if (!map.containsKey(key)) {
     	   println "No "+key+" for '"+node.'@name'+"'"
      }
  }
  
	void createPhylonode(def node, def parent, def tree) {
		println "CreatePhylonode: name='"+node.'@name'+"', left='"+node.attributes().get('left')+"', right='"+node.attributes().right+"' depth='"+node.attributes().depth+"'";
//		nodeDataSet.add() (
//			phylotree_id:        tree,
//        	parent_phylonode_id: parent,
//        	left_idx:            node.left, 	
//        	right_idx:           node.right, 	
//        	type_id:             'thingy',
//        	label:               node.'@name',		
//        	distance:            node.parents().size()
//      	)
  }
  
	def createOrganism(def node) {
		println "CreateOrganism called with '"+node.'@name'+"'"
//		orgDataSet.add() (
//			abbreviation: abbreviation,
//            genus:        genus,
//            species:      species,
//            common_name:  commonName
//        )		
		
        // Create dbxref
//        dbXRefSet.add() (
//            
//        )
        
        // Create organism dbxref
//		organismDbXRefSet.add() (
//		    organism: organism,
//		    dbxref:   dbxref
//		)
		//return organismId;
	}
  
	static String input = '''<?xml version="1.0" encoding="UTF-8"?>
<org-heirachy>
    <node name="Root">
        <node name="Kinetoplastids">
            <node name="Protozoa"></node>
            <node name="Leishmania">
                <organism name="Lmajor">
                    <property name="taxonId" value="5664" />
                    <property name="fullName" value="Leishmania major" />
                    <property name="nickname" value="leish" />
                    <property name="dbName" value="GeneDB_Lmajor" />
                </organism>
                <organism name="Linfantum">
                    <property name="taxonId" value="5761" />
                    <property name="fullName" value="Leishmania infantum" />
                    <property name="nickname" value="linfantum" />
                    <property name="dbName" value="GeneDB_Linfantum" />
                </organism>
            </node>
            <node name="Trypanasoma">
                <organism name="Tbruceibrucei427">
                    <property name="fullName" value="Trypanasoma brucei brucei, strain 427" />
                </organism>
                <organism name="Tbruceibrucei927">
                    <property name="fullName" value="Trypanasoma brucei brucei, strain 927" />
                </organism>
                <organism name="Tbruceigambiense">
                    <property name="fullName" value="Trypanasoma brucei gambiense" />
                    <property name="nickname" value="tgambiense" />
                </organism>
                <organism name="Tvivax">
                    <property name="taxonId" value="5693" />
                    <property name="fullName" value="Trypanasoma vivax" />
                    <property name="nickname" value="tvivax" />
                    <property name="dbName" value="GeneDB_Tvivax" />
                </organism>
                <organism name="Tcruzi">
                    <property name="taxonId" value="5693" />
                    <property name="fullName" value="Trypanasoma cruzi" />
                    <property name="nickname" value="tcruzi" />
                    <property name="dbName" value="GeneDB_Tcruzi" />
                </organism>
            </node>
            <node name="Apicomplexa">
                <node name="Plasmodia">
                    <organism name="Pfalciparum">
                        <property name="taxonId" value="5833" />
                        <property name="fullName" value="Plasmodium falciparum" />
                        <property name="nickname" value="malaria" />
                        <property name="dbName" value="GeneDB_Pfalciparum" />
                    </organism>
                    <organism name="Pknowlesi">
                        <property name="taxonId" value="5850" />
                        <property name="fullName" value="Plasmodium knowlesi" />
                        <property name="nickname" value="pknowlesi" />
                        <property name="dbName" value="GeneDB_Pknowlesi" />
                    </organism>
                    <organism name="Pberghei">
                        <property name="taxonId" value="5821" />
                        <property name="fullName" value="Plasmodium berghei" />
                        <property name="nickname" value="pberghei" />
                        <property name="dbName" value="GeneDB_Pberghei" />
                    </organism>
                    <organism name="Pchabaudi">
                        <property name="taxonId" value="5825" />
                        <property name="fullName" value="Plasmodium chabaudi" />
                        <property name="nickname" value="pchabaudi" />
                        <property name="dbName" value="GeneDB_Pchabaudi" />
                    </organism>
                </node>
            </node>
            <node name="Fungi">
                <organism name="Scerevisiae">
                    <property name="taxonId" value="4932" />
                    <property name="fullName" value="S... cerevisiae" />
                    <property name="nickname" value="cerevisiae" />
                    <property name="dbName" value="GeneDB_Scerevisiae" />
                </organism>
                <organism name="Spombe">
                    <property name="taxonId" value="4896" />
                    <property name="fullName" value="S... pombe" />
                    <property name="nickname" value="pombe" />
                    <property name="dbName" value="GeneDB_Spombe" />
                </organism>
                <organism name="Afumigatus">
                    <property name="taxonId" value="5085" />
                    <property name="fullName" value="Aspergillus fumigatus" />
                    <property name="nickname" value="asp" />
                    <property name="dbName" value="GeneDB_Afumigatus" />
                </organism>
            </node>
            <node name="Eubacteria">
                <node name="Protobacteria">
                    <node name="Gammaprotbacteria"></node>
                    <node name="Alphaprotobacteria"></node>
                    <node name="Betaprotobacteria"></node>
                    <node name="Delta-Epsilon-subdivisions"></node>
                </node>
                <node name="Actinobacteria"></node>
                <node name="Bacterideter"></node>
                <node name="Chlamydiae"></node>
                <node name="Firmiates">
                    <node name="Bacilli"></node>
                    <node name="Clostridia"></node>
                    <node name="Midliates"></node>
                </node>
            </node>
        </node>
		<node name="Eukaryotes">
			<organism name="Cdubliniensis">
			</organism>
			<organism name="Ddiscoideum">
			</organism>
			<organism name="Ehistolytica">
			</organism>
 			<organism name="Etenella">
			</organism>
			<organism name="Lbraziliensis">
			</organism>
			<organism name="Tannulata">
			</organism>
			<organism name="Tcongolense">
			</organism>
			<organism name="Smansoni">
			</organism>
			<organism name="Bmarinus">
			</organism>
			<organism name="">
			</organism>
			<organism name="">
			</organism>
			<organism name="">
			</organism>
			<organism name="">
			</organism>
			<organism name="">
			</organism>

		</node>
    </node>
</org-heirachy>
''';
  
}