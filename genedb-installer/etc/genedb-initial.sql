-- ----------------------
-- --- Organisms
-- ----------------------
--
--
-- Delete preloaded unused orgs
--
delete from organism where common_name = 'human';
delete from organism where common_name = 'fruitfly';
delete from organism where common_name = 'mouse';
delete from organism where common_name = 'mosquito';
delete from organism where common_name = 'rat';
delete from organism where common_name = 'mustard weed';
delete from organism where common_name = 'worm';
delete from organism where common_name = 'zebrafish';
delete from organism where common_name = 'rice';
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
insert into db (name) values ('PRODUCT');
insert into db (name, description) values (
		'CCGEN',
		'Db where we store db entries corresponding to controlled curation CV terms'
);

insert into db (name, description) values (
		'genedb_internal',
		'Local values for where we need a dbxref eg for new cvterms'
);
--
-- Add 'proper' GeneDB dbs
--


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

-- Do we need a CV for products, or just a db?
insert into cv (name, definition) values (
		'genedb_products',
		'GeneDB-specific cv for products'
);

insert into cv (name, definition) values (
		'genedb_fcvt_prop_keys',
		'Specific values which are used as keys for looking up feature_cvterm_prop'
);

insert into cv (name, definition) values (
		'genedb_synonym_type',
		'GeneDB-specific cv for more specific naming'
);



--
-- ----------------------
-- --- CvTerms
-- ----------------------

insert into dbxref(db_id, accession, description) values (
		(select db_id from db where name='genedb_internal'),
		'genedb_literature:lit_unknown', 
		'dbxref for UNKNOWN literature type'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype) values (
		(select cv_id from cv where name='genedb_literature'), 
		'Unknown',
		'Unknown literature type ',
		(select dbxref_id from dbxref where accession='genedb_literature:lit_unknown'),
		0, 0
); 


insert into dbxref(db_id, accession, description) values (
		(select db_id from db where name='genedb_internal'),
		'genedb_literature:lit_notfetch', 
		'dbxref for NOTFETCHED literature type'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype) values (
		(select cv_id from cv where name='genedb_literature'), 
		'Not Fetched',
		'Not Fetched literature type',
		(select dbxref_id from dbxref where accession='genedb_literature:lit_notfetch'),
		0, 0
); 


insert into dbxref(db_id, accession, description) values (
		(select db_id from db where name='genedb_internal'),
		'genedb_literature:lit_journal',
		'dbxref for Journal literature type'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype)
		values ((select cv_id from cv where name='genedb_literature'), 
		'Journal',
		'journal literature type',
		(select dbxref_id from dbxref where accession='genedb_literature:lit_journal'),
		0, 0
); 


insert into dbxref(db_id, accession) values (
		(select db_id from db where name='null'),
		'genedb_misc:top_level_seq'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype) values (
		(select cv_id from cv where name='genedb_misc'), 
		'top_level_seq',
		'Marker to indicate that a sequence should be considered a top level feature',
		(select dbxref_id from dbxref where accession='genedb_misc:top_level_seq'),
		0, 0
); 


insert into dbxref(db_id, accession) values (
		(select db_id from db where name='null'),
		'genedb_misc:protein_charge'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype) values (
		(select cv_id from cv where name='genedb_misc'), 
		'protein_charge',
		'Marker for protein_charge',
		(select dbxref_id from dbxref where accession='genedb_misc:protein_charge'),
		0, 0
); 

	
insert into dbxref(db_id, accession) 
		values ((select db_id from db where name='null'),
		'genedb_misc:molecular_mass'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype)
		values ((select cv_id from cv where name='genedb_misc'), 
		'molecular_mass',
		'Marker to indicate that a sequence should be considered a top level feature',
		(select dbxref_id from dbxref where accession='genedb_misc:molecular_mass'),
		0, 0
); 

		
insert into dbxref(db_id, accession)
		values ((select db_id from db where name='null'),
		'genedb_misc:isoelectric_point'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype)
		values ((select cv_id from cv where name='genedb_misc'), 
		'isoelectric_point',
		'Marker to indicate that a sequence should be considered a top level feature',
		(select dbxref_id from dbxref where accession='genedb_misc:isoelectric_point'),
		0, 0
); 
		
		
insert into dbxref(db_id, accession) 
		values ((select db_id from db where name='null'),
		'genedb_misc:taxonomy'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype)
		values ((select cv_id from cv where name='genedb_misc'), 
		'taxonomy',
		'Marker to indicate that a sequence should be considered a top level feature',
		(select dbxref_id from dbxref where accession='genedb_misc:taxonomy'),
		0, 0
); 
		
		
insert into dbxref(db_id, accession) 
		values ((select db_id from db where name='null'),
		'autocreated:note'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype)
		values ((select cv_id from cv where name='autocreated'), 
		'note',
		'Free text comment field',
		(select dbxref_id from dbxref where accession='autocreated:note'),
		0, 0
); 

       
insert into dbxref(db_id, accession) 
		values ((select db_id from db where name='null'),
		'autocreated:curation'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype)
		values ((select cv_id from cv where name='autocreated'), 
		'curation',
		'Free text note field for local curation',
		(select dbxref_id from dbxref where accession='autocreated:curation'),
		0, 0
); 

		
insert into dbxref(db_id, accession) 
		values ((select db_id from db where name='null'), 
		'autocreated:colour'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype)
		values ((select cv_id from cv where name='autocreated'), 
		'colour',
		'colour value',
		(select dbxref_id from dbxref where accession='autocreated:colour'),
		0, 0
); 

		
insert into dbxref(db_id, accession) 
		values ((select db_id from db where name='null'),
		'autocreated:private'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype)
		values ((select cv_id from cv where name='autocreated'), 
		'private',
		'Free text note field for comments not to be made publicly visible',
		(select dbxref_id from dbxref where accession='autocreated:private'),
		0, 0
); 
		
		
insert into dbxref(db_id, accession)
		values ((select db_id from db where name='null'),
		'genedb_fcvt_prop_keys:evidence'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype)
		values ((select cv_id from cv where name='genedb_fcvt_prop_keys'), 
		'evidence',
		'Free text note field for supporting evidence',
		(select dbxref_id from dbxref where accession='genedb_fcvt_prop_keys:evidence'),
		0, 0
); 
		
		
insert into dbxref(db_id, accession) 
		values ((select db_id from db where name='null'),
		'genedb_fcvt_prop_keys:qualifier'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype)
		values ((select cv_id from cv where name='genedb_fcvt_prop_keys'), 
		'qualifier',
		'qualifier',
		(select dbxref_id from dbxref where accession='genedb_fcvt_prop_keys:qualifier'),
		0, 0
); 
       
       
insert into dbxref(db_id, accession) 
		values ((select db_id from db where name='null'),
		'genedb_fcvt_prop_keys:date'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype)
		values ((select cv_id from cv where name='genedb_fcvt_prop_keys'), 
		'date',
		'date',
		(select dbxref_id from dbxref where accession='genedb_fcvt_prop_keys:date'),
		0, 0
); 



insert into dbxref(db_id, accession) values (
		(select db_id from db where name='null'),
		'genedb_synonym_type:reserved_name'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype) values (
		(select cv_id from cv where name='genedb_synonym_type'), 
		'reserved_name',
		'A name reserved for future use eg a paper pending',
		(select dbxref_id from dbxref where accession='genedb_synonym_type:reserved_name'),
		0, 0
); 
              

insert into dbxref(db_id, accession) values (
		(select db_id from db where name='null'), 
		'genedb_synonym_type:synonym'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype) values (
		(select cv_id from cv where name='genedb_synonym_type'), 
		'synonym',
		'synonym',
		(select dbxref_id from dbxref where accession='genedb_synonym_type:synonym'),
		0, 0
); 
		
		
insert into dbxref(db_id, accession) values (
		(select db_id from db where name='null'), 
		'genedb_synonym_type:primary_name'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype) values (
		(select cv_id from cv where name='genedb_synonym_type'), 
		'primary_name',
		'eg gene symbol',
		(select dbxref_id from dbxref where accession='genedb_synonym_type:primary_name'),
		0, 0
); 
		
		
insert into dbxref(db_id, accession) values (
		(select db_id from db where name='null'), 
		'genedb_synonym_type:protein_name'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype) values (
		(select cv_id from cv where name='genedb_synonym_type'), 
		'protein_name',
		'Specific name for the protein - may be different from gene symbol',
		(select dbxref_id from dbxref where accession='genedb_synonym_type:protein_name'),
		0, 0
); 
		
		
insert into dbxref(db_id, accession) values (
		(select db_id from db where name='null'), 
		'genedb_synonym_type:systematic_id'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype) values (
		(select cv_id from cv where name='genedb_synonym_type'), 
		'systematic_id',
		'Unique, permanent, accession name for feature',
		(select dbxref_id from dbxref where accession='genedb_synonym_type:systematic_id'),
		0, 0
);

		
insert into dbxref(db_id, accession) values (
		(select db_id from db where name='null'), 
		'genedb_synonym_type:temporary_systematic_id'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype) values (
		(select cv_id from cv where name='genedb_synonym_type'), 
		'temporary_systematic_id',
		'Unique accession name for feature. Unstable - will change in future',
		(select dbxref_id from dbxref where accession='genedb_synonym_type:temporary_systematic_id'),
		0, 0
);



--dbxref_id: getNextId("dbxref"),

insert into dbxref(db_id, accession, description) values (
		(select db_id from db where name='null'), 
		'genedb_synonym_type:fullName',
		'dbxref for cvterm fullName'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype) values (
		(select cv_id from cv where name='genedb_misc_...'), 
		'fullName',
		'full name of organism',
		(select dbxref_id from dbxref where accession='genedb_synonym_type:temporary_systematic_id'),
		0, 0
);


insert into dbxref(db_id, accession, description) values (
		(select db_id from db where name='null'), 
		'genedb_synonym_type:transTable',
		'dbxref for cvterm transTable'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype) values (
		(select cv_id from cv where name='genedb_misc_...'), 
		'transTable',
		'transTable',
		(select dbxref_id from dbxref where accession='genedb_synonym_type:temporary_systematic_id'),
		0, 0
);
	
	
insert into dbxref(db_id, accession, description) values (
		(select db_id from db where name='null'), 
		'genedb_synonym_type:DisplayPage',
		'dbxref for cvterm DisplayPage'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype) values (
		(select cv_id from cv where name='genedb_misc_...'), 
		'DisplayPage',
		'cvterm for attribute value page in taxonomy xml, which decides whether ot not a group should have individual page',
		(select dbxref_id from dbxref where accession='genedb_synonym_type:temporary_systematic_id'),
		0, 0
);


insert into dbxref(db_id, accession, description) values (
		(select db_id from db where name='null'), 
		'genedb_synonym_type:curator',
		'dbxref for cvterm curator'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype) values (
		(select cv_id from cv where name='genedb_misc_...'), 
		'curator',
		'curator',
		(select dbxref_id from dbxref where accession='genedb_synonym_type:temporary_systematic_id'),
		0, 0
);


insert into dbxref(db_id, accession, description) values (
		(select db_id from db where name='genedb_internal'), 
		'genedb_internal:taxonList',
		'dbxref for cvterm taxonList'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype) values (
		(select cv_id from cv where name='genedb_misc'), 
		'taxonList',
		'List of taxon ids of this org and all its children recursively',
		(select dbxref_id from dbxref where accession='genedb_internal:taxonList'),
		0, 0
);


insert into dbxref(db_id, accession, description) values (
		(select db_id from db where name='genedb_internal'), 
		'genedb_internal:taxonId',
		'dbxref for cvterm taxonId'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype) values (
		(select cv_id from cv where name='genedb_misc'), 
		'taxonId',
		'NCBI Taxonomy id',
		(select dbxref_id from dbxref where accession='genedb_internal:taxonId'),
		0, 0
);


insert into dbxref(db_id, accession, description) values (
		(select db_id from db where name='genedb_internal'), 
		'genedb_internal:nickname',
		'dbxref for cvterm nickname'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype) values (
		(select cv_id from cv where name='genedb_misc'), 
		'nickname',
		'nickname of organism',
		(select dbxref_id from dbxref where accession='genedb_internal:nickname'),
		0, 0
);


insert into dbxref(db_id, accession, description) values (
		(select db_id from db where name='genedb_internal'), 
		'genedb_internal:curatorName',
		'dbxref for cvterm curator name'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype) values (
		(select cv_id from cv where name='genedb_misc'), 
		'curatorName',
		'curatorName',
		(select dbxref_id from dbxref where accession='genedb_internal:curatorName'),
		0, 0
);


insert into dbxref(db_id, accession, description) values (
		(select db_id from db where name='genedb_internal'), 
		'genedb_internal:curatorEmail',
		'dbxref for cvterm curator email'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype) values (
		(select cv_id from cv where name='genedb_misc'), 
		'curatorEmail',
		'curatorEmail',
		(select dbxref_id from dbxref where accession='genedb_internal:curatorEmail'),
		0, 0
);


insert into dbxref(db_id, accession, description) values (
		(select db_id from db where name='genedb_internal'), 
		'genedb_internal:mitochondrialTranslationTable',
		'dbxref for cvterm mitochondrialTranslationTable'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype) values (
		(select cv_id from cv where name='genedb_misc'), 
		'mitochondrialTranslationTable',
		'mitochondrialTranslationTable',
		(select dbxref_id from dbxref where accession='genedb_internal:mitochondrialTranslationTable'),
		0, 0
);


insert into dbxref(db_id, accession, description) values (
		(select db_id from db where name='genedb_internal'), 
		'genedb_internal:dbName',
		'dbxref for cvterm dbName'
);
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype) values (
		(select cv_id from cv where name='genedb_misc'), 
		'dbName',
		'database name for organism',
		(select dbxref_id from dbxref where accession='genedb_internal:dbName'),
		0, 0
);
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
