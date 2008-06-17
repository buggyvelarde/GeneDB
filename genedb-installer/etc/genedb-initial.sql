-- ----------------------
-- --- Organisms
-- ----------------------
--
--
-- Delete preloaded unused orgs
--
delete from organism;
-- GeneDB organisms and their properties and loaded by the phylogeny script
insert into organism (abbreviation, genus, species, common_name, comment) values (
          'dummy',
          'None',
          'really',
          'dummy',
          'Dummy value for representing eg organisms for db hits');


--
-- ----------------------
-- --- Dbs
-- ----------------------
--
--
-- Clear dbs
--
delete from db where name like 'DB:%';
delete from db where name = 'UNIPROT';

--
-- Add local dbs and update existing dbs
--

insert into db (name, description) values (
		'PRODUCT',
		'Db where we store db entries corresponding to products');

insert into db (name, description) values (
		'RILEY',
		'Db where we store db entries corresponding to RILEY terms');

insert into db (name, description) values (
		'CCGEN',
		'Db where we store db entries corresponding to controlled curation CV terms');

insert into db (name, description) values (
		'genedb_misc',
		'Local values for where we need a dbxref eg for new cvterms');

insert into db (name, description, urlprefix) values (
        'SMART',
        'Db where we store db entries corresponding to SMART terms',
        'http://smart.embl-heidelberg.de/smart/do_annotation.pl?&BLAST=DUMMY&DOMAIN=');

insert into db (name, description, urlprefix) values (
        'TDRtargets',
        'Db where we store db entries corresponding to TDRtargets terms',
        'http://tdrtargets.org/targets/view?gene_name=');

insert into db (name, description, urlprefix) values (
        'Superfamily',
        'Db where we store db entries corresponding to Superfamily terms',
        'http://supfam.cs.bris.ac.uk/SUPERFAMILY/cgi-bin/model.cgi?model=');

insert into db (name, description, urlprefix) values (
        'PRINTS',
        'Db where we store db entries corresponding to PRINTS terms',
        'http://www.bioinf.manchester.ac.uk/cgi-bin/dbbrowser/PRINTS/DoPRINTS.pl?cmd_a=Display&fun_a=text&qst_a=');

--
-- Add url prefix to existing dbs
--
update db set urlprefix='http://merops.sanger.ac.uk/cgi-bin/merops.cgi?id='
	where name='MEROPS';

update db set urlprefix='http://www.ebi.ac.uk/interpro/IEntry?ac='
	where name='InterPro';

update db set urlprefix='http://www.ebi.ac.uk/interpro/IEntry?ac='
	where name='InterPro';

update db set urlprefix='http://genome-www4.stanford.edu/cgi-bin/SGD/locus.pl?locus='
	where name='SGD';

update db set urlprefix='http://smart.embl-heidelberg.de/smart/do_annotation.pl?&BLAST=DUMMY&DOMAIN='
	where name='SMART';

update db set urlprefix='http://www.ebi.uniprot.org/entry/'
	where name='UniProt';

update db set urlprefix='http://plasmodb.org/plasmodb/servlet/sv?page=gene&source_id='
	where name='PlasmoDB';

update db set urlprefix='http://www.brenda.uni-koeln.de/php/result_flat.php3?ecno='
	where name='BRENDA';

update db set urlprefix='http://www.ebi.ac.uk/cgi-bin/expasyfetch?'
	where name='EMBL';

update db set urlprefix='http://www.rcsb.org/pdb/cgi/explore.cgi?pdbId='
	where name='PDB';

update db set urlprefix='http://www.rcsb.org/pdb/cgi/explore.cgi?pdbId='
	where name='Pfam';

update db set urlprefix='http://ca.expasy.org/cgi-bin/prosite-search-ac?'
	where name='Prosite';

update db set urlprefix='http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=PubMed&dopt=Abstract&list_uids='
	where name='PMID';



--
-- ----------------------
-- --- CVs
-- ----------------------
--
--
-- Add local cv's
--
insert into cv (name, definition) values (
		'genedb_misc',
		'Miscellaneous GeneDB-specific terms'
);

insert into cv (name, definition) values (
		'genedb_literature',
		'GeneDB-specific terms for literature'
);

insert into cv (name, definition) values (
		'RILEY',
		'CV for storing Monica RILEY classifications'
);

insert into cv (name, definition) values (
		'CC_genedb_controlledcuration',
		'GeneDB-specific cv for controlled curation terms'
);


insert into cv (name, definition) values (
		'CC_protein_family',
		'GeneDB-specific cv for controlled curation terms - protein family'
);


insert into cv (name, definition) values (
		'CC_name_derivation',
		'GeneDB-specific cv for controlled curation terms - name derivation'
);


insert into cv (name, definition) values (
		'CC_species_dist',
		'GeneDB-specific cv for controlled curation terms - species_dist'
);


insert into cv (name, definition) values (
		'CC_pt_mod',
		'GeneDB-specific cv for controlled curation terms - post translation modification'
);

-- Do we need a CV for products, or just a db?
insert into cv (name, definition) values (
		'genedb_products',
		'GeneDB-specific cv for products'
);

insert into cv (name, definition) values (
		'genedb_synonym_type',
		'GeneDB-specific cv for more specific naming'
);



--
-- ----------------------
-- --- CvTerms
-- ----------------------

--insert into dbxref(db_id, accession, description) values (
--		(select db_id from db where name='null'),
--		'genedb_synonym_type:DisplayPage',
--		'dbxref for cvterm DisplayPage'
--);
--insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype) values (
--		(select cv_id from cv where name='genedb_misc_...'),
--		'DisplayPage',
--		'cvterm for attribute value page in taxonomy xml, which decides whether ot not a group should have individual page',
--		(select dbxref_id from dbxref where accession='genedb_synonym_type:temporary_systematic_id'),
--		0, 0
--);


-- Load phylogeny relationships
insert into dbxref(db_id, accession) values (
		(select db_id from db where name='null'),
		'genedb_misc:organism_heirachy'
);
insert into phylotree (dbxref_id, name, type_id, comment) values (
		(select dbxref_id from dbxref where accession='genedb_misc:organism_heirachy'),
		'org_heirachy',
		(select cvterm_id from cvterm where name='taxonomy'),
		'GeneDB organism heirachy'
);
