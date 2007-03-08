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
   
    def REL_IS_A
    
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
	    
	    List relations = db.rows("select * from cvterm cvt, cv cv where cv.name='relationship' and cvt.cv_id = cv_id and cvt.name='is_a'");
	    REL_IS_A = relations[0]."cv_id";
	    
    }

	
	
	void process(def inp) {
	    def heirachy = new XmlParser().parseText(inp) 
  
	    heirachy.children().each(
	            {createNode(it, null, null)}
        )		
	}

	
	// Now walk the tree creating db entries  
	void createNode(def node, def parent, int nestedCvTerm) {
		assert node != null
		switch (node.name()) {
		    
			case 'cv':
				List rows = db.rows("select * from cv where name='"+node.'@name'+"'")
				if (rows[0] == null) {
				    throw new RuntimeException("Couldn't get cv for '"+node.'@name'+"'");
				}
				currentCv = rows[0]."cv_id"
				
				node.children().each(
					{createNode(it, node, null)}
				)
				break
			
			
			case 'term':
				checkAttributeExists(node, "description")
			    
				int newDbXRef = createDbXRef(node, parentId, tree)
				int newCvTerm = createCvTerm(node, parentId, tree)
          	  
				if (nestedCvTerm != null) {
				    // Create feature relationship
				    featureRelDataSet.add(
				        featureBySubjectId: newCvTerm,
				        featureByObjectId:  nestedCvTerm,
				        cvTerm:             REL_IS_A,
				        rank:               1
				    )
				}
					
				node.children().each(
						{createNode(it, node, newCvTerm)}
				)
				break
				
				
			default:
				throw new RuntimeException("Saw a node of '"+node.name()
				        +"' when expecting node or organism");
		}
	}
 
  
	// Create a dbxref entry when a cvterm node is encountered
	int createDbXRef(def node) {
		String name = node.'@name';
		String description = node.'@description';

		if (writeBack) {
			dbXRefDataSet.add(
       		 	description: description, 	
       		 	accession:   description, 	
       		 	db:          currentDb,		
       		 	version:     1
//		       this.features = features;
//		       this.cvTerms = cvTerms
      		)
		}
		return getMaxIdFromPrimaryKey("dbxref")
	}
	
	
	// Create a set of db entries when an organism node is encountered
	int createCvTerm(def node,def genus,def species) {
		//println "CreateOrganism called with '"+node.'@name'+"'"
		String name = node.'@name'
		String description = node.'@description';

		if (writeBack) {
			cvTermDataSet.add(
		        dbXRef:             dbXRef,
	        	cv:                 currentCv,
	        	name:               name,
	        	isObsolete:         false,
	        	isRelationshipType: false
     	   )
		}
		return getMaxIdFromPrimaryKey("cvterm")
	}
        
	
	// Find a cvterm by name
	int findCvTerm(String termName) {
	    def rows = db.rows("select cvterm_id from cvterm where name='"+termName
	            +"' and cv_id='"+genedbMiscCvId+"'")
	    if (rows[0] == null) {
	        throw new RuntimeException("Couldn't get cvterm for '"+termName+"'");
	    }
	    return rows[0]["cvterm_id"]
	}
	
	
	 
	// Sanity check that an expected property is present 
	void checkAttributeExists(def node, String key) {
	    if (!node.attributes().containsKey(key)) {
	        throw new RuntimeException("No "+key+" for '"+node.'@name'+"'")
		}
	    String attr = node.attributes().get(key)
	    if (attr.length == 0) {
	        throw new RuntimeException("The value for '"+key+"' is empty")
	    }
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
	
	
	static void main(args) {
    	LoadGeneDbCv lgc = new LoadGeneDbCv()
    	lgc.process(input);
	}
	

	// The XML config file for loading the organism and phylogeny modules
	static String input = '''<?xml version="1.0" encoding="UTF-8"?>
<cv-loading>
    <cv name="genedb-misc">
		<term name="applications" desription="">
			<term name="clustering">
				<term name="Compara" />
				<term name="OrthoMCL" />
			</term>
			<term name="similarity_application">
				<term name="Blast">
					<term name="BlastP" />
					<term name="BlastN" />
					<term name="BlastX" />
					<term name="TBlastN" />
				</term>
				<term name="fasta" />
			</term>
			<term name="gene_prediction_application">
				<term name="Glimmer" />
				<term name="genefinder" />
			</term>
			<term name="misc_applications">
				<term name="RepeatMasker" />
			</term>
		</term> <!-- algorithms -->
		<term name="top_level_seq" 
              description="Marker to indicate that a sequence should be considered a top level feature" />
		<term name="protein_stats" description="">
			<term name="protein_charge" description="" />
			<term name="molecular_mass" description="" />
			<term name="isoelectric_point" description="" />		
		</term>
		<term name="feature_props" description="">
			<term name="note" description="" />
			<term name="curation" description="" />
			<term name="private" description="" />		
		</term>
	</cv> <!-- genedb-misc -->
    <cv name="genedb-literature" db="genedb-misc">
		<term name="lit_unknown"   description="unknown literature type" />
		<term name="lit_unfetched" description="unfetched literature type" />
		<term name="lit_journal"   description="journal literature type" />
	</cv> <!-- genedb-literature -->
    <cv name="genedb_synonym_type" db="genedb-misc">
		<term name="reserved_name"   description="" />
		<term name="synonym" description="" />
		<term name="primary_name"   description="" />
		<term name="protein_name"   description="" />
		<term name="systematic_id"   description="" />
		<term name="primary_name"   description="" />
		<term name="temporary_systematic_id"   description="" />
	</cv> <!-- genedb_synonym_type -->
</cv-loading>
''';

}