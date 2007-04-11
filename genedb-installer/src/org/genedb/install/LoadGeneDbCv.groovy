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
    
    def cvTermDataSet
    def termId
    
    def relationDataSet
   
    def REL_IS_A
    
    def dbXRefDataSet
    def dbXRefId
    
    def dbDataSet
    def dbId
    
    def currentCv
    def currentDb
    
    boolean writeBack = true;
    
    LoadGeneDbCv() {
		db = Sql.newInstance(
			'jdbc:postgresql://pathdbsrv1a.internal.sanger.ac.uk:10001/chado',
			'chado',
			'chado',
			'org.postgresql.Driver')
			
     	cvDataSet = db.dataSet("cv")
		
		cvTermDataSet = db.dataSet("cvterm")
		
     	dbXRefDataSet = db.dataSet("dbxref")
		
     	dbDataSet = db.dataSet("db")
		
     	relationDataSet = db.dataSet("cvterm_relationship")
     	
	    List relations = db.rows("select * from cvterm cvt, cv cv where cv.name='relationship' and cvt.cv_id = cv.cv_id and cvt.name='is_a'");
	    REL_IS_A = relations[0]."cvterm_id";
	    
    }

	
	
	void process(def inp) {
	    def heirachy = new XmlParser().parseText(inp) 
  
	    heirachy.children().each(
	            {createNode(it, null, -1)}
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
				
				rows = db.rows("select * from db where name='"+node.'@db'+"'")
				if (rows[0] == null) {
				    throw new RuntimeException("Couldn't get db for '"+node.'@db'+"'");
				}
				currentDb = rows[0]."db_id"
				
				node.children().each(
					{createNode(it, node, -1)}
				)
				break
			
			
			case 'term':
				checkAttributeExists(node, "description")
			    
				int newDbXRef = createDbXRef(node)
				int newCvTerm = createCvTerm(node, newDbXRef)
          	  
				if (nestedCvTerm > 0) {
				    // Create feature relationship
				    if (writeBack) {
				        try {
						    relationDataSet.add(
						        subject_id: newCvTerm,
						        object_id:  nestedCvTerm,
						        type_id:    REL_IS_A
					  	 	 )
				 	   } catch (Exception exp) {
				  	      // May be a duplicate tree
				  	      System.err.println("Problem storing cvterm - duplicate?")
					   }
				    }
				    
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
		    try {
				dbXRefDataSet.add(
    	   		 	description: description, 	
       			 	accession:   description, 	
       			 	db_id:          currentDb,		
       			 	version:     1
      			)
		    } catch (Exception exp) {
		        // May be a duplicate
		       	System.err.println("Problem storing cvterm - duplicate?");
		    }
		}
		return getMaxIdFromPrimaryKey("dbxref")
	}
	
	
	// Create a set of db entries when an organism node is encountered
	int createCvTerm(def node, def dbXRef) {
		//println "CreateOrganism called with '"+node.'@name'+"'"
		String name = node.'@name'
		String description = node.'@description';

		if (writeBack) {
		    try {
				cvTermDataSet.add(
			        dbxref_id:             dbXRef,
	    	    	cv_id:                 currentCv,
	        		name:               name,
	        		is_obsolete:         0,
	        		is_relationshiptype: 0
     		   )
	 	  	 } catch (Exception exp) {
	    		 // May be a duplicate
	    	  	 System.err.println("Problem storing cvterm - duplicate?");
	   		 }
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
	        throw new AppException("No "+key+" for '"+node.'@name'+"'")
		}
	    String attr = node.attributes().get(key)
	    if (attr.length() == 0) {
	        throw new AppException("The value for '"+key+"' is empty in '"+node.'@name'+"'")
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
    	try {
	    	lgc.process(input);
    	}
    	catch (AppException exp) {
    	    System.err.println(exp.message);
    	}
    	System.err.println("Done (writeBack='"+lgc.writeBack+"')");
	}
	

	// The XML config file for loading the organism and phylogeny modules
	static String input = '''<?xml version="1.0" encoding="UTF-8"?>
<cv-loading>
    <cv name="genedb_misc" db="genedb_misc">
        <term name="application" description="Programs that are run on a genome">
            <term name="clustering_application" description="Programs that cluster related genes/proteins together">
                <term name="Compara" description="Compara clustering method" />
                <term name="OrthoMCL" description="OrthoMCL clustering method" />
            </term>
            <term name="similarity_application" description="Programs that find similarities to another sequence">
                <term name="Blast" description="General blast algorithm">
                    <term name="BlastP" description="Blast P v P" />
                    <term name="BlastN" description="Blast N v N" />
                    <term name="BlastX" description="BlastX" />
                    <term name="TBlastN" description="TBlastX" />
                </term>
                <term name="fasta" description="Similar to blast" />
            </term>
            <term name="gene_prediction_application" description="Programs which identify genes on a sequence">
                <term name="Glimmer" description="Glimmer gene prediction" />
                <term name="genefinder" description="genefinder gene prediction" />
            </term>
            <term name="misc_application" description="Applications we couldn't think of where else to put">
                <term name="RepeatMasker" description="Mask repeats ge for blasting" />
            </term>
        </term>
        
        <term name="top_level_seq" 
              description="Marker to indicate that a sequence should be considered a top level feature" />

        <term name="taxonomy" 
              description="CvTerm to indicate that a phylotree is a type of taxonomic tree" />

        <term name="protein_stat" description="Keys for storing information specifically for proteins">
            <term name="protein_charge" description="Protein charge in Daltons" />
            <term name="molecular_mass" description="Mass of the feature" />
            <term name="isoelectric_point" description="Isoelectric point" />        
        </term>
        
        <term name="feature_cvterm_props" description="Keys for storing information specifically for FeatureCvTermProps">
            <term name="qualifier" description="eg NOT, colocalizes_with" />
            <term name="evidence" description="Typically an evidence code" />
			<term name="residue" description="The residues affected by this" />
			<term name="attribution" description="Who supplied this data" />
        </term>
        
        <term name="feature_props" description="Keys for storing information, for any feature in general">
            <term name="curation" description="Free text note field for local curation" />
            <term name="private" description="Free text note field for comments not to be made publicly visible" />     
 			<term name="EC_number" description="Free text note field for EC_number" />
 			<term name="colour" description="Numeric key for storing a display colour" />
        </term>

        <term name="phylo_organism_prop" description="Parent term for CvTerms that act as keys for organism or phylonode props">
                <term name="taxonList" description="List of taxon ids of this org and all its children recursively" />
                <term name="taxonId" description="NCBI Taxonomy id" />
                <term name="nickname" description="Nickname previously used on genedb website" />
                <term name="curatorName" description="Name of organism curator" />
                <term name="curatorEmail" description="Email of organism curator" />
                <term name="mitochondrialTranslationTable" description="Translation table used for mitochondrial DNA" />
                <term name="translationTable" description="Translation table" />
				<term name="app_www_homePage" description="Style of homepage wanted, if any" />  
				<term name="htmlShortName" description="The short name in HTML format" />
				<term name="htmlFullName" description="The full name in HTML format" />                                                                                      
        </term>
		<term name="artemis_specific" description="cvterm for various *_file qualifiers for artemis ">
			<term name="blast_file"   description="Blast file location" />
			<term name="blastn_file"   description="Blastn file location" />
			<term name="blastpgo_file"   description="Blastp+go file location" />
			<term name="blastp_file"   description="Blastp file location" />
			<term name="blastx_file"   description="Blastx file location" />
			<term name="fasta_file"   description="Fasta file location" />
			<term name="fastax_file"   description="Fastax file location" />
			<term name="tBlastn_file"   description="tBlastn file location" />
			<term name="tBlastx_file"   description="tBlastx file location" />	
		</term>
    </cv>

    
    	
    <cv name="genedb_literature" db="genedb_misc">
        <term name="unknown"   description="unknown literature type" />
        <term name="unfetched" description="unfetched literature type" />
        <term name="journal"   description="journal literature type" />
    </cv>
    
    
    <cv name="genedb_synonym_type" db="genedb_misc">
        <term name="reserved_name"   description="A name reserved for future use eg a paper pending" />
        <term name="synonym" description="synonym" />
        <term name="primary_name"   description="eg gene symbol" />
        <term name="protein_name"   description="Specific name for the protein - may be different from gene symbol" />
        <term name="systematic_id"   description="Unique, permanent, accession name for feature" />
        <term name="temporary_systematic_id"   description="Unique accession name for feature. Unstable - will change in future" />
    </cv>
</cv-loading>
''';

}

class AppException extends RuntimeException {
    AppException(String msg) {
        super(msg)
    }
}