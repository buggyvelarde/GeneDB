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
		*/

    }
        
    
	static void main(args) {
    	LoadGeneDbPhylogeny lgp = new LoadGeneDbPhylogeny()
    	lgp.process(input);
	}

	def getNextId(def table) {
		def col = table + "_id";
	    int id = db.rows("SELECT max("+col+") as "+col+" from " + table)[0]["${col}"];
	    if (id == null) {
	        return 1
	    }
	    return id;
		
		//def col = table + "_" + table + "_id_seq";
	    //def id = db.rows("SELECT nextval("+col+") as "+col)[0]["${col}"];
	    //if (id == null) {
	    //    return 1
	    //}
	    //return id;
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
              createPhylonode(node, parent, tree)
              def newNode = getNextId("phylonode")
              node.children().each(
                      {createNode(it, newNode, tree)}
              )
              break
          case 'organism':
          	  def newNode = createPhylonode(node, parent, tree)
          	  
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
          	  
          	  List sections
          	  String temp = props.remove("fullName")
          	  
          	  if(temp.contains(",")) {
          		sections = temp.split(",")
          	  } else {
          		sections = temp.split(" ")
          	  }
          	  //Matcher matcher = (temp =~ /([a-zA-Z]+)\s+([a-zA-Z]+)/) 
          	  //println (matcher[0])
          	  //println (matcher[0][1]+","+matcher[0][2])
          	  
          	  createOrganism(node,sections[0],sections[-1])
              
          	  nodeOrgDataSet.add(
            	phylonode_id: getNextId("phylonode"),
            	organism_id:  getNextId("organism")
            )
			
          	  
      
          	  checkPropExists(props, node, "taxonId")
          	  checkPropExists(props, node, "transTable")
          	  //checkPropExists(props, "")
          	  if (!props.containsKey("nickname") && !props.containsKey("newOrg")) {
     	   		println "No nickname for '"+node.'@name'+"'"
              }
          	  props.remove("newOrg")
          	  props.put("fullName",temp)
          	  
          	  int orgId = getNextId("organism")
          	  props.each(
          			  {
          				List rows = db.rows("select * from cvterm where name='"+it.key+"'")
          				int type_id = rows[0]."cvterm_id"
          				String value = it.value;
        				orgPropDataSet.add(
          	        		  organism_id: orgId,
          	        		  type_id: type_id,
          	        		  value: value,
          	        		  rank: 0)}
          	  )
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
		double dist = node.parent().size();
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
      	)
  }
  
	void createOrganism(def node,def genus,def species) {
		println "CreateOrganism called with '"+node.'@name'+"'"
		String abb = node.'@name'
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
		)
		
	}
  
	static String input = '''<?xml version="1.0" encoding="UTF-8"?>
<org-heirachy>
    <node name="Root">
		<node name="Helminths">
			<node name="platyhelminths">
				<organism name="Smansoni">
					<property name="taxonId" value="" />
					<property name="fullName" value="" />
					<property name="nickname" value="smansoni" />
	            	<property name="dbName" value="GeneDB_Smansoni" />
					<property name="transTable" value=""/>
					<property name="mitoTransTable" value=""/>
				</organism>
			</node>
		</node>
        <node name="Kinetoplastids">
            <node name="Leishmania">
				<organism name="Lbraziliensis">
					<property name="taxonId" value="5660" />
					<property name="fullName" value="Leishmania braziliensis" />
					<property name="nickname" value="lbraziliensis" />
	                <property name="dbName" value="GeneDB_Lbraziliensis" />
					<property name="transTable" value="1"/>
					<property name="mitoTransTable" value="4"/>
				</organism>
                <organism name="Lmajor">
                    <property name="taxonId" value="5664" />
                    <property name="fullName" value="Leishmania major" />
                    <property name="nickname" value="leish" />
                    <property name="dbName" value="GeneDB_Lmajor" />
					<property name="transTable" value="1"/>
					<property name="mitoTransTable" value="4"/>
                </organism>
                <organism name="Linfantum">
                    <property name="taxonId" value="5761" />
                    <property name="fullName" value="Leishmania infantum" />
                    <property name="nickname" value="linfantum" />
                    <property name="dbName" value="GeneDB_Linfantum" />
					<property name="transTable" value="1"/>
					<property name="mitoTransTable" value="4"/>
                </organism>
            </node>
            <node name="Trypanasoma">
                <organism name="Tcongolense">
					<property name="taxonId" value="5692" />
					<property name="fullName" value="Trypanosoma congolense" />
					<property name="nickname" value="tcongolense" />
	                <property name="dbName" value="GeneDB_Tcongolense" />
					<property name="transTable" value="1"/>
					<property name="mitoTransTable" value="4"/>
				</organism>
				<organism name="Tbruceibrucei427">
                    <property name="taxonId" value="5761" />
					<property name="fullName" value="Trypanasoma brucei brucei, strain 427" />
					<property name="nickname" value="tbrucei427" />
					<property name="dbName" value="GeneDB_Tbrucei427" />
					<property name="transTable" value=""/>
					<property name="mitoTransTable" value=""/>
                </organism>
                <organism name="Tbruceibrucei927">
                    <property name="taxonId" value="185431" />
					<property name="fullName" value="Trypanasoma brucei brucei, strain 927" />
					<property name="nickname" value="tryp" />
					<property name="dbName" value="GeneDB_Tbrucei927" />
					<property name="transTable" value="1"/>
					<property name="mitoTransTable" value="4"/>
                </organism>
                <organism name="Tbruceigambiense">
                    <property name="taxonId" value="31285" />
					<property name="fullName" value="Trypanasoma brucei gambiense" />
                    <property name="nickname" value="tgambiense" />
					<property name="dbName" value="GeneDB_Tgambiense" />
					<property name="transTable" value="1"/>
					<property name="mitoTransTable" value="4"/>
                </organism>
                <organism name="Tvivax">
                    <property name="taxonId" value="5699" />
                    <property name="fullName" value="Trypanasoma vivax" />
                    <property name="nickname" value="tvivax" />
                    <property name="dbName" value="GeneDB_Tvivax" />
					<property name="transTable" value="1"/>
					<property name="mitoTransTable" value="4"/>
                </organism>
                <organism name="Tcruzi">
                    <property name="taxonId" value="5693" />
                    <property name="fullName" value="Trypanasoma cruzi" />
                    <property name="nickname" value="tcruzi" />
                    <property name="dbName" value="GeneDB_Tcruzi" />
					<property name="transTable" value="1"/>
					<property name="mitoTransTable" value="4"/>
                </organism>
            </node>
            <node name="Protozoa">
	            <organism name="Ddiscoideum">
					<property name="taxonId" value="44689" />
					<property name="fullName" value="Dictyostelium discoideum" />
					<property name="nickname" value="dicty" />
	                <property name="dbName" value="GeneDB_Ddiscoideum" />
					<property name="transTable" value="1"/>
					<property name="mitoTransTable" value="1"/>
				</organism>
				<organism name="Ehistolytica">
					<property name="taxonId" value="5759" />
					<property name="fullName" value="Entamoeba histolytica" />
					<property name="nickname" value="ehistolytica" />
	                <property name="dbName" value="GeneDB_Ehistolytica" />
					<property name="transTable" value="1"/>
					<property name="mitoTransTable" value="1"/>
				</organism>
				<node name="Apicomplexa">
					<organism name="Etenella">
						<property name="taxonId" value="5802" />
						<property name="fullName" value="Eimeria tenella" />
						<property name="nickname" value="etenella" />
		                <property name="dbName" value="GeneDB_Etenella" />
						<property name="transTable" value="1"/>
						<property name="mitoTransTable" value="4"/>
				  	</organism>
					<organism name="Tannulata">
						<property name="taxonId" value="5874" />				
						<property name="fullName" value="Theileria annulata" />
						<property name="nickname" value="annulata" />
		                <property name="dbName" value="GeneDB_Tannulata" />
						<property name="transTable" value="1"/>
						<property name="mitoTransTable" value="4"/>
					</organism>
	                <node name="Plasmodia">
	                    <organism name="Pfalciparum">
	                        <property name="taxonId" value="5833" />
	                        <property name="fullName" value="Plasmodium falciparum" />
	                        <property name="nickname" value="malaria" />
	                        <property name="dbName" value="GeneDB_Pfalciparum" />
							<property name="transTable" value="1"/>
						    <property name="mitoTransTable" value="4"/>
	                    </organism>
	                    <organism name="Pknowlesi">
	                        <property name="taxonId" value="5850" />
	                        <property name="fullName" value="Plasmodium knowlesi" />
	                        <property name="nickname" value="pknowlesi" />
	                        <property name="dbName" value="GeneDB_Pknowlesi" />
							<property name="transTable" value="1"/>
							<property name="mitoTransTable" value="4"/>
	                    </organism>
	                    <organism name="Pberghei">
	                        <property name="taxonId" value="5821" />
	                        <property name="fullName" value="Plasmodium berghei" />
	                        <property name="nickname" value="pberghei" />
	                        <property name="dbName" value="GeneDB_Pberghei" />
							<property name="transTable" value="1"/>
							<property name="mitoTransTable" value="4"/>
	                    </organism>
	                    <organism name="Pchabaudi">
	                        <property name="taxonId" value="5825" />
	                        <property name="fullName" value="Plasmodium chabaudi" />
	                        <property name="nickname" value="pchabaudi" />
	                        <property name="dbName" value="GeneDB_Pchabaudi" />
							<property name="transTable" value="1"/>
							<property name="mitoTransTable" value="4"/>
	                    </organism>
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
                </organism>
                <organism name="Spombe">
                    <property name="taxonId" value="4896" />
                    <property name="fullName" value="Schizosaccharomyces pombe" />
                    <property name="nickname" value="pombe" />
                    <property name="dbName" value="GeneDB_Spombe" />
					<property name="transTable" value="1"/>
					<property name="mitoTransTable" value="4"/>
                </organism>
                <organism name="Afumigatus">
                    <property name="taxonId" value="5085" />
                    <property name="fullName" value="Aspergillus fumigatus" />
                    <property name="nickname" value="asp" />
                    <property name="dbName" value="GeneDB_Afumigatus" />
					<property name="transTable" value="1"/>
					<property name="mitoTransTable" value="4"/>
                </organism>
				<organism name="Cdubliniensis">
					<property name="taxonId" value="42374" />
					<property name="fullName" value="Candida dubliniensis" />
					<property name="nickname" value="cdubliniensis" />
	                <property name="dbName" value="GeneDB_Cdubliniensis" />
					<property name="transTable" value="12"/>
					<property name="mitoTransTable" value="3"/>
				</organism>
            </node>
            
        </node>
		<node name="Eukaryotes">
			<organism name="Bmarinus">
				<property name="taxonId" value="" />
				<property name="fullName" value="" />
				<property name="nickname" value="bmarinus" />
            	<property name="dbName" value="GeneDB_Bmarinus" />
				<property name="transTable" value=""/>
				<property name="mitoTransTable" value=""/>
			</organism>
			<!--<organism name="">
			</organism>
			<organism name="">
			</organism>
			<organism name="">
			</organism>
			<organism name="">
			</organism>
			<organism name="">
			</organism> -->

		</node>
		<node name="bacteria">
			<node name="Bordetella">
				<organism name="">
					<property name="taxonId" value="" />
					<property name="fullName" value="" />
					<property name="nickname" value="" />
	            	<property name="dbName" value="" />
					<property name="transTable" value=""/>
					<property name="mitoTransTable" value=""/>
				</organism>
				<organism name="">
					<property name="taxonId" value="" />
					<property name="fullName" value="" />
					<property name="nickname" value="" />
	            	<property name="dbName" value="" />
					<property name="transTable" value=""/>
					<property name="mitoTransTable" value=""/>
				</organism>
				<organism name="">
					<property name="taxonId" value="" />
					<property name="fullName" value="" />
					<property name="nickname" value="" />
	            	<property name="dbName" value="" />
					<property name="transTable" value=""/>
					<property name="mitoTransTable" value=""/>
				</organism>
				<organism name="">
					<property name="taxonId" value="" />
					<property name="fullName" value="" />
					<property name="nickname" value="" />
	            	<property name="dbName" value="" />
					<property name="transTable" value=""/>
					<property name="mitoTransTable" value=""/>
				</organism>
			</node>
			<node name="Burkholderia">
				<organism name="">
					<property name="taxonId" value="" />
					<property name="fullName" value="" />
					<property name="nickname" value="" />
	            	<property name="dbName" value="" />
					<property name="transTable" value=""/>
					<property name="mitoTransTable" value=""/>
				</organism>
				<organism name="">
					<property name="taxonId" value="" />
					<property name="fullName" value="" />
					<property name="nickname" value="" />
	            	<property name="dbName" value="" />
					<property name="transTable" value=""/>
					<property name="mitoTransTable" value=""/>
				</organism>
			</node>
			<node name="Clostridium">
				<organism name="">
					<property name="taxonId" value="" />
					<property name="fullName" value="" />
					<property name="nickname" value="" />
	            	<property name="dbName" value="" />
					<property name="transTable" value=""/>
					<property name="mitoTransTable" value=""/>
				</organism>
				<organism name="">
					<property name="taxonId" value="" />
					<property name="fullName" value="" />
					<property name="nickname" value="" />
	            	<property name="dbName" value="" />
					<property name="transTable" value=""/>
					<property name="mitoTransTable" value=""/>
				</organism>
			</node>
			<node name="Mycobacterium">
				<organism name="">
					<property name="taxonId" value="" />
					<property name="fullName" value="" />
					<property name="nickname" value="" />
	            	<property name="dbName" value="" />
					<property name="transTable" value=""/>
					<property name="mitoTransTable" value=""/>
				</organism>
				<organism name="">
					<property name="taxonId" value="" />
					<property name="fullName" value="" />
					<property name="nickname" value="" />
	            	<property name="dbName" value="" />
					<property name="transTable" value=""/>
					<property name="mitoTransTable" value=""/>
				</organism>
			</node>
			<node name="Neisseria">
			</node>
			<node name="Salmonella">
			</node>
			<node name="Staphylococcus">
			</node>
			<node name="Streptococcus">
			</node>
			<node name="Yersinia">
			</node>
		</node>
    </node>
</org-heirachy>
''';
  
}