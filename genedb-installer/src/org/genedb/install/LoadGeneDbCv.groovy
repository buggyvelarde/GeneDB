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
            'jdbc:postgresql://pgsrv2:5432/pathdev',
            'rn2@sanger.ac.uk',
            'xxxxxxx',
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
        def hierarchy = new XmlParser().parseText(inp)

        hierarchy.children().each(
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

				System.out.println("encountered term ->"+node.'@name');

                int newDbXRef = createDbXRef(node)
                int newCvTerm=-1;
                if(newDbXRef!=-1){

                newCvTerm = createCvTerm(node, newDbXRef)
                }else{
                	System.out.println("insertion failed at dbxref for "+node.'@name');
                }
                if(newDbXRef!=-1 && newCvTerm!=-1){
				System.out.println("Successfully inserted new cvterm ->"+node.'@name');
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
                   return -1;
            }
        }
        int id = db.rows("SELECT dbxref_id from dbxref where description='"+description+"' and accession='"+description+"' and db_id="+currentDb)[0]["dbxref_id"];
        return id;

    }


    // Create a set of db entries when an organism node is encountered
    int createCvTerm(def node, def dbXRef) {
        //println "CreateOrganism called with '"+node.'@name'+"'"
        String name = node.'@name'
        String description = node.'@description';

        if (writeBack) {
            try {
                cvTermDataSet.add(
                    dbxref_id:          dbXRef,
                    cv_id:              currentCv,
                    name:               name,
                    is_obsolete:         0,
                    is_relationshiptype: 0
                )
                } catch (Exception exp) {
                 // May be a duplicate
                   System.err.println("Problem storing cvterm - duplicate?");
                   return -1;
                }
        }
        int id = db.rows("SELECT cvterm_id from cvterm where cv_id="+currentCv+" and dbxref_id="+dbXRef+" and name='"+name+"' and is_obsolete=0 and is_relationshiptype=0")[0]["cvterm_id"];
        return id;
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
		<term name="SNP_analysis_application" description="Programs which identifies SNPs and indels">
            	<term name="commandline_str" description="Full command line string used to execute the application" />
            	<term name="strain_id" description="unique name of the strain" />
            	<term name="strain_sample_id" description="unique name of the sample for a particular strain" />
                <!-- terms used from samtools alignment format -->
                <term name="chrcontigref" description="Chromosome or Contig reference"/>
                <term name="position" description="position of the SNP base"/>
                <term name="refbase" description="base of the reference chromosome or contig"/>
                <term name="SNPbase" description="base call at SNP position"/>
                <term name="consensus_base" description="base called by maq consensus"/>
                <term name="consensus_quality" description="consensus quality"/>
                <term name="SNP_quality" description="SNP quality"/>
                <term name="max_mapping_quality" description="maximum mapping quality"/>
                <term name="read_coverage" description="read coverage or depth coverage"/>
                <term name="A_Count" description="Number of A"/>
                <term name="C_Count" description="Number of C"/>
                <term name="G_count" description="Number of G"/>
                <term name="T_count" description="Number of T"/>
                <term name="N_count" description="Number of N"/>
                <term name="star_count" description="Number of star symbols"/>
                <!-- terms used by ssaha alignment program -->
                <term name="ssaha" description="tool for very fast matching and alignment of DNA sequences">
                	<term name="ssaha_call" description="call at a particular genome location"/>
                	<term name="ssaha_scalar_result" description="Output that results in a single value for the whole data set">
                		<term name="ssaha_number_of_cell_lines" description="number of cell lines"/>
                	</term>
                 	<term name="ssaha_multivalue_result" description="Output results that has a list of values">
                 		<term name="ssaha_snp_headers" description="Headers related to SNPs">
	                		<term name="ssaha_name_of_chromosome_reference" description="name of chromosome reference"/>
	                		<term name="ssaha_overall_confidence_score" description="Overall SNP confidence score: 0-99"/>
	                		<term name="ssaha_offset" description="SNP offset"/>
	                		<term name="ssaha_read_coverage" description="Read coverage"/>
	                		<term name="ssaha_reference_base" description="Reference base"/>
	                		<term name="ssaha_consensus_base" description="Consensus base"/>
	                		<term name="ssaha_snp_base" description="SNP base"/>
	                		<term name="ssaha_number_of_A" description="Number of A (base quality Q&gt;=0)"/>
	                		<term name="ssaha_number_of_C" description="Number of C (base quality Q&gt;=0)"/>
	                		<term name="ssaha_number_of_G" description="Number of G (base quality Q&gt;=0)"/>
	                		<term name="ssaha_number_of_T" description="Number of T (base quality Q&gt;=0)"/>
	                		<term name="ssaha_number_of_dashes" description="Number of - s"/>
	                		<term name="ssaha_number_of_N" description="Number of Ns"/>
	                		<term name="ssaha_number_of_a_bq_lt_25" description="Number of a (base quality Q&lt;25)"/>
	                		<term name="ssaha_number_of_c_bq_lt_25" description="Number of c (base quality Q&lt;25)"/>
	                		<term name="ssaha_number_of_g_bq_lt_25" description="Number of g (base quality Q&lt;25)"/>
	                		<term name="ssaha_number_of_t_bq_lt_25" description="Number of t (base quality Q&lt;25)"/>
                		</term>
                		<term name="ssaha_indel_headers" description="Headers related to indels">
                			<term name="ssaha_insertion_index" description="insertion index"/>
                			<term name="ssaha_deletion_index" description="deletion index"/>
                			<term name="ssaha_chromosome_name" description="Chromosome name"/>
                			<term name="ssaha_reference_offset" description="Reference offset"/>
                			<term name="ssaha_insertion_length" description="Insertion length"/>
                			<term name="ssaha_deletion_length" description="Deletion length"/>
                			<term name="ssaha_number_of_reads_covering_the_deletion_position" description="Number of reads covering the deletion position"/>
                	        <term name="ssaha_read_mapping_score" description="read mapping score"/>
                			<term name="ssaha_read_name" description="read name"/>
                			<term name="ssaha_read_offset" description="read offset"/>
                			<term name="ssaha_alignment_direction" description="Alignment direction"/>
                			<term name="ssaha_read_coverage_on_the_reference_base" description="Read coverage on the reference base"/>
                			<term name="ssaha_number_of_dashes_from_reads_on_the_reference_base" description="number of dashes from reads on the reference base"/>
                		</term>
                   	</term>
                </term>
                <term name="maq" description="Mapping and Assembly with Quality It builds assembly by mapping short reads to reference sequences.">
                	<term name="maq_output_headers" description="Output headers from maq program">
                		<term name="maq_chromosome" description="chromosome" />
                		<term name="maq_position" description="position" />
                		<term name="maq_reference_base" description="reference base" />
                		<term name="maq_consensus_base" description="consensus base" />
                		<term name="maq_phredlike_consensus_quality" description="Phred-like consensus quality" />
                		<term name="maq_read_depth" description="read depth" />
                		<term name="maq_avg_no_of_hits_of_reads_covering_this_position" description="the average number of hits of reads covering this position" />
                		<term name="maq_highest_mapping_quality_of_the_reads_covering_this_position" description="highest mapping quality of the reads covering this position" />
                		<term name="maq_minimum_consensus_quality_in_the_3bp_flanking_regions_at_each_side_of_the_site" description="minimum consensus quality in the 3bp flanking regions at each side of the site" />
                		<term name="maq_the_second_best_call" description="the second best call" />
                		<term name="maq_log_likelihood_ratio_of_the_second_best_and_the_third_best_call" description="log likelihood ratio of the second best and the third best call" />                		<term name="maq_third_best_call" description="third best call" />
                	</term>
                	<term name="maq_scalar_result" description="Output results that have a single value"/>
                </term>
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

        <term name="protein_property" description="A property of a polypeptide feature">
            <term name="GPI_anchored" description="A flag to indicate that the protein is GPI-anchored, as predicted by dgpi"/>
            <term name="SignalP_prediction" description="The SignalP prediction for this protein. Possible values are 'Signal peptide' and 'Signal anchor'."/>
            <term name="signal_peptide_probability" description="The probability that this protein is a signal peptide, as predicted by SignalP."/>
            <term name="signal_anchor_probability" description="The probability that this protein is a signal anchor, as predicted by SignalP."/>
            <term name="PlasmoAP_score" description="A score denoting how likely this protein is to target the apicoplast, as predicted by PlasmoAP."/>
        </term>

        <term name="GPI_anchor_cleavage_site_property" description="A property of a GPI_anchor_cleavage_site feature">
            <term name="GPI_cleavage_site_score" description="The score assigned to this GPI cleavage site by dgpi"/>
        </term>

        <term name="HTH_property" description="A property of a helix-turn-helix motif">
        	<term name="Maximum_score_at" description="Maximum score in a helix turn helix hit"/> <!-- Get proper description -->
        	<term name="Standard_deviations" description="Standard deviations"/> <!-- Get proper description -->

        </term>

        <term name="feature_cvterm_props" description="Keys for storing information specifically for FeatureCvTermProps">
            <term name="qualifier" description="eg NOT, colocalizes_with" />
            <term name="evidence" description="Typically an evidence code" />
            <term name="residue" description="The residues affected by this" />
            <term name="attribution" description="Who supplied this data" />
        </term>

        <term name="feature_props" description="Keys for storing information, for any feature in general">
            <term name="method" description="Free text note field for method" />
            <term name="status" description="Structured text field for status"/>
            <term name="curation" description="Free text note field for local curation" />
            <term name="private" description="Free text note field for comments not to be made publicly visible" />
            <term name="EC_number" description="Free text note field for EC_number" />
            <term name="colour" description="Numeric key for storing a display colour" />
            <term name="arcturus_id" description="Arcturus contig id" />
            
            <term name="archived_metadata"
                    description="Metadata about a feature that comes from some external source, typically a data file, and is archived in its source form">
                <term name="EMBL_qualifier" description="An unparsed EMBL qualifier, for archival purposes"/>
            </term>
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
            <term name="app_www_homePage_mainDescription" description="Main section of homepage text" />
            <term name="htmlShortName" description="The short name in HTML format" />
            <term name="htmlFullName" description="The full name in HTML format" />
            <term name="populated" description="This property is set on organisms that are populated with data"/>
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
            <term name="clustalx_file"   description="Clustalx file location" />
            <term name="sigcleave_file"   description="Sigcleave file location" />
            <term name="pepstats_file"   description="Peptide statistics file location" />
        </term>

        <term name="application_params" description="These seems to be used for /similarity qualifiers">
            <term name="ungapped id" description="The ungapped identity percentage"/>
            <term name="overlap" description="Number of amino acids of overlap"/>
        </term>
    </cv>

    <!-- Feature types used by GeneDB that are not SO terms.
         These are slated for addition to SO; when they're added,
         we should switch to using the SO term instead. -->
    <cv name="genedb_feature_type" db="genedb_misc">
        <term name="GPI_anchor_cleavage_site" description="the site on the C-terminus of the protein which is cleaved off prior to addition of the glycolipid"/>
    </cv>

    <cv name="genedb_literature" db="genedb_misc">
        <term name="unknown"   description="unknown literature type" />
        <term name="unfetched" description="unfetched literature type" />
        <term name="journal"   description="journal literature type" />
    </cv>

    <cv name="genedb_synonym_type" db="genedb_misc">
        <term name="synonym" description="synonym" />
        <term name="product_synonym"   description="A synonym for the gene product" />
        <term name="systematic_id"   description="Unique, permanent, accession name for feature" />
        <term name="temporary_systematic_id"   description="Unique accession name for feature. Unstable - will change in future" />
    </cv>

    <cv name="RILEY" db="RILEY">
        <term name="0.0.0" description="Unknown function, no known homologs"/>
        <term name="0.0.1" description="Conserved in Escherichia coli"/>
        <term name="0.0.2" description="Conserved in other organisms"/>
        <term name="1.0.0" description="Cell processes"/>
        <term name="1.1.1" description="Chemotaxis and mobility"/>
        <term name="1.2.1" description="Chromosome replication"/>
        <term name="1.3.1" description="Chaperones"/>
        <term name="1.4.0" description="Protection responses"/>
        <term name="1.4.1" description="Cell killing"/>
        <term name="1.4.2" description="Detoxification"/>
        <term name="1.4.3" description="Drug/analog sensitivity"/>
        <term name="1.4.4" description="Radiation sensitivity"/>
        <term name="1.4.5" description="DNA-binding"/>
        <term name="1.5.0" description="Transport/binding proteins"/>
        <term name="1.5.1" description="Amino acids and amines"/>
        <term name="1.5.2" description="Cations"/>
        <term name="1.5.3" description="Carbohydrates, organic acids and alcohols"/>
        <term name="1.5.4" description="Anions"/>
        <term name="1.5.5" description="Other Transport/binding proteins"/>
        <term name="1.6.0" description="Adaptation"/>
        <term name="1.6.1" description="Adaptations, atypical conditions"/>
        <term name="1.6.2" description="Osmotic adaptation"/>
        <term name="1.6.3" description="Fe storage"/>
        <term name="1.6.4" description="Nodulation related"/>
        <term name="1.7.1" description="Cell division"/>
        <term name="1.8.1" description="Sporulation, differentiation and germination"/>
        <term name="2.0.0" description="Macromolecule metabolism"/>
        <term name="2.1.0" description="Macromolecule degradation"/>
        <term name="2.1.1" description="Degradation of DNA"/>
        <term name="2.1.2" description="Degradation of RNA"/>
        <term name="2.1.3" description="Degradation of polysaccharides"/>
        <term name="2.1.4" description="Degradation of proteins, peptides, glyco"/>
        <term name="2.2.0" description="Macromolecule synthesis, modification"/>
        <term name="2.2.1" description="Amino acyl tRNA synthesis; tRNA modification"/>
        <term name="2.2.2" description="Basic proteins - synthesis, modification"/>
        <term name="2.2.3" description="DNA - replication, repair, restriction/modification"/>
        <term name="2.2.4" description="Glycoprotein"/>
        <term name="2.2.5" description="Lipopolysaccharide"/>
        <term name="2.2.6" description="Lipoprotein"/>
        <term name="2.2.7" description="Phospholipids"/>
        <term name="2.2.8" description="Polysaccharides - (cytoplasmic)"/>
        <term name="2.2.9" description="Protein modification"/>
        <term name="2.2.10" description="Proteins - translation and modification"/>
        <term name="2.2.11" description="RNA synthesis, modification, DNA transcript'n"/>
        <term name="2.2.12" description="tRNA"/>
        <term name="3.0.0" description="Metabolism of small molecules"/>
        <term name="3.1.0" description="Amino acid biosynthesis"/>
        <term name="3.1.1" description="Alanine"/>
        <term name="3.1.2" description="Arginine"/>
        <term name="3.1.3" description="Asparagine"/>
        <term name="3.1.4" description="Aspartate"/>
        <term name="3.1.5" description="Chorismate"/>
        <term name="3.1.6" description="Cysteine"/>
        <term name="3.1.7" description="Glutamate"/>
        <term name="3.1.8" description="Glutamine"/>
        <term name="3.1.9" description="Glycine"/>
        <term name="3.1.10" description="Histidine"/>
        <term name="3.1.11" description="Isoleucine"/>
        <term name="3.1.12" description="Leucine"/>
        <term name="3.1.13" description="Lysine"/>
        <term name="3.1.14" description="Methionine"/>
        <term name="3.1.15" description="Phenylalanine"/>
        <term name="3.1.16" description="Proline"/>
        <term name="3.1.17" description="Serine"/>
        <term name="3.1.18" description="Threonine"/>
        <term name="3.1.19" description="Tryptophan"/>
        <term name="3.1.20" description="Tyrosine"/>
        <term name="3.1.21" description="Valine"/>
        <term name="3.2.0" description="Biosynthesis of cofactors, carriers"/>
        <term name="3.2.1" description="Acyl carrier protein (ACP)"/>
        <term name="3.2.2" description="Biotin"/>
        <term name="3.2.3" description="Cobalamin"/>
        <term name="3.2.4" description="Enterochelin"/>
        <term name="3.2.5" description="Folic acid"/>
        <term name="3.2.6" description="Heme, porphyrin"/>
        <term name="3.2.7" description="Lipoate"/>
        <term name="3.2.8" description="Menaquinone, ubiquinone"/>
        <term name="3.2.9" description="Molybdopterin"/>
        <term name="3.2.10" description="Pantothenate"/>
        <term name="3.2.11" description="Pyridine nucleotide"/>
        <term name="3.2.12" description="Pyridoxine"/>
        <term name="3.2.13" description="Riboflavin"/>
        <term name="3.2.14" description="Thiamin"/>
        <term name="3.2.15" description="Thioredoxin, glutaredoxin, glutathione"/>
        <term name="3.2.16" description="biotin carboxyl carrier protein (BCCP)"/>
        <term name="3.2.17" description="Ferredoxin"/>
        <term name="3.2.18" description="Isoprenoid"/>
        <term name="3.3.0" description="Central intermediary metabolism"/>
        <term name="3.3.1" description="2'-Deoxyribonucleotide metabolism"/>
        <term name="3.3.2" description="Amino sugars"/>
        <term name="3.3.3" description="Entner-Douderoff"/>
        <term name="3.3.4" description="Gluconeogenesis"/>
        <term name="3.3.5" description="Glyoxylate bypass"/>
        <term name="3.3.6" description="Incorporation metal ions"/>
        <term name="3.3.7" description="Misc. glucose metabolism"/>
        <term name="3.3.8" description="Misc. glycerol metabolism"/>
        <term name="3.3.9" description="Non-oxidative branch, pentose pwy"/>
        <term name="3.3.10" description="Nucleotide hydrolysis"/>
        <term name="3.3.11" description="Nucleotide interconversions"/>
        <term name="3.3.12" description="Oligosaccharides"/>
        <term name="3.3.13" description="Phosphorus compounds"/>
        <term name="3.3.14" description="Polyamine biosynthesis"/>
        <term name="3.3.15" description="Pool, multipurpose conversions of intermed. met'm"/>
        <term name="3.3.16" description="S-adenosyl methionine"/>
        <term name="3.3.17" description="Salvage of nucleosides and nucleotides"/>
        <term name="3.3.18" description="Sugar-nucleotide biosynthesis, conversions"/>
        <term name="3.3.19" description="Sulfur metabolism"/>
        <term name="3.3.20" description="amino acids"/>
        <term name="3.3.21" description="other"/>
        <term name="3.3.22" description="Nitrogen metabolism (urease)"/>
        <term name="3.4.0" description="Degradation of small molecules"/>
        <term name="3.4.1" description="Amines"/>
        <term name="3.4.2" description="Amino acids"/>
        <term name="3.4.3" description="Carbon compounds"/>
        <term name="3.4.4" description="Fatty acids"/>
        <term name="3.4.5" description="Other Degradation of small molecules"/>
        <term name="3.5.0" description="Energy metabolism, carbon"/>
        <term name="3.5.1" description="Aerobic respiration"/>
        <term name="3.5.2" description="Anaerobic respiration"/>
        <term name="3.5.3" description="Electron transport"/>
        <term name="3.5.4" description="Fermentation"/>
        <term name="3.5.5" description="Glycolysis"/>
        <term name="3.5.6" description="Oxidative branch, pentose pwy"/>
        <term name="3.5.7" description="Pyruvate dehydrogenase"/>
        <term name="3.5.8" description="TCA cycle"/>
        <term name="3.5.9" description="ATP-proton motive force"/>
        <term name="3.6.0" description="Fatty acid biosynthesis"/>
        <term name="3.6.1" description="Fatty acid and phosphatidic acid biosynth"/>
        <term name="3.7.0" description="Nucleotide biosynthesis"/>
        <term name="3.7.1" description="Purine ribonucleotide biosynthesis"/>
        <term name="3.7.2" description="Pyrimidine ribonucleotide biosynthesis"/>
        <term name="3.8.0" description="related to secondary metabolism"/>
        <term name="3.8.1" description="polyketide synthases (PKSs)"/>
        <term name="3.8.2" description="non-ribosomal peptide synthases (NRPSs)"/>
        <term name="4.0.0" description="Cell envelope"/>
        <term name="4.1.0" description="Periplasmic/exported/lipoproteins"/>
        <term name="4.1.1" description="Inner membrane"/>
        <term name="4.1.2" description="Murein sacculus, peptidoglycan"/>
        <term name="4.1.3" description="Outer membrane constituents"/>
        <term name="4.1.4" description="Surface polysaccharides and antigens"/>
        <term name="4.1.5" description="Surface structures"/>
        <term name="4.1.6" description="G+ membrane"/>
        <term name="4.1.7" description="G+ exported/lipoprotein"/>
        <term name="4.1.8" description="G+ surface anchored"/>
        <term name="4.1.9" description="G+ peptidoglycan, teichoic acid"/>
        <term name="4.2.0" description="Ribosome constituents"/>
        <term name="4.2.1" description="Ribosomal and stable RNAs"/>
        <term name="4.2.2" description="Ribosomal proteins - synthesis, modification"/>
        <term name="4.2.3" description="Ribosomes - maturation and modification"/>
        <term name="5.0.0" description="Extrachromosomal"/>
        <term name="5.1.0" description="Laterally acquired elements"/>
        <term name="5.1.1" description="Colicin-related functions"/>
        <term name="5.1.2" description="Phage-related functions and prophages"/>
        <term name="5.1.3" description="Plasmid-related functions"/>
        <term name="5.1.4" description="Transposon-related functions"/>
        <term name="5.1.5" description="Pathogenicity Islands/determinants"/>
        <term name="6.0.0" description="Global functions"/>
        <term name="6.1.1" description="Global regulatory functions"/>
        <term name="6.1.2" description="Response regulator"/>
        <term name="6.1.3" description="two-component fusion"/>
        <term name="6.2.0" description="RNA polymerase core enzyme binding"/>
        <term name="6.2.1" description="sigma factor"/>
        <term name="6.2.2" description="anti sigma factor"/>
        <term name="6.2.3" description="anti sigma factor antagonist"/>
        <term name="6.3.1" description="AsnC"/>
        <term name="6.3.2" description="AraC"/>
        <term name="6.3.3" description="GntR"/>
        <term name="6.3.4" description="IclR"/>
        <term name="6.3.5" description="LacI"/>
        <term name="6.3.6" description="LysR"/>
        <term name="6.3.7" description="MarR"/>
        <term name="6.3.8" description="TetR"/>
        <term name="6.3.9" description="ROK"/>
        <term name="6.3.10" description="DeoR"/>
        <term name="6.3.11" description="LuxR (GerR)"/>
        <term name="6.3.12" description="MerR"/>
        <term name="6.3.13" description="ArsR"/>
        <term name="6.3.14" description="PadR"/>
        <term name="6.4.0" description="Protein kinases"/>
        <term name="6.4.1" description="Serine/threonine"/>
        <term name="6.4.2" description="Tyrosine"/>
        <term name="6.5.0" description="Others"/>
        <term name="6.5.1" description="GGDEF/EAL domain regulatory protein"/>
        <term name="6.6.0" description="LPS regulated regulatory functions"/>
        <term name="7.0.0" description="Not classified (included putative assignments)"/>
        <term name="7.1.1" description="DNA sites, no gene product"/>
        <term name="7.2.1" description="Cryptic genes"/>
    </cv>
</cv-loading>
''';

}

class AppException extends RuntimeException {
    AppException(String msg) {
        super(msg)
    }
}
