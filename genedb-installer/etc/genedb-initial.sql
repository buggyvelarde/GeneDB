-- ----------------------
-- --- Organisms
-- ----------------------
--
--
-- Delete preloaded unused orgs
--
delete from organism;
-- GeneDB organisms and their properties and loaded by the phylogeny script


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
		'Db where we store db entries corresponding to controlled curation CV terms'
);

insert into db (name, description) values (
		'genedb_misc',
		'Local values for where we need a dbxref eg for new cvterms'
);


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
