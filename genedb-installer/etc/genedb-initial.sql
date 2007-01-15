-- Clear organism table
delete from organism where common_name = 'human';
delete from organism where common_name = 'fruitfly';
delete from organism where common_name = 'mouse';
delete from organism where common_name = 'mosquito';
delete from organism where common_name = 'rat';
delete from organism where common_name = 'mustard weed';
delete from organism where common_name = 'worm';
delete from organism where common_name = 'zebrafish';
delete from organism where common_name = 'rice';


-- Load GeneDB organisms and their properties

#insert into organism (abbreviation, genus, species, common_name)
#       values ('A.fumigatus', 'Aspergillus','fumigatus','afumigatus');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('B.cenocepacia', 'Burkholderia','cenocepacia','bcenocepacia');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('B.fragilis', 'Bacteroide','fragilis','bfragilis');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('B.bronchiseptica', 'Bordetella','bronchiseptica','bbronchiseptica');
      
#insert into organism (abbreviation, genus, species, common_name)
#       values ('B.parapertussis', 'Bordetella','parapertussis','bparapertussis');
#insert into organism (abbreviation, genus, species, common_name)
#       values ('B.pertussis', 'Bordetella','pertussis','bpertussis');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('B.pseudomallei', 'Burkholderia', 'pseudomallei','bpseudomallei');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('C.dubliniensis', 'Candida','dubliniensis','cdubliniensis');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('C.jejuni', 'Campylobacter','jejuni','cjejuni');
       
#insert into organism (abbreviation, genus, species, common_name)
#       values ('C.abortus', 'Chlamydophilia','abortus','cabortus');
     
#insert into organism (abbreviation, genus, species, common_name)
#       values ('C.difficile', 'Clostridium','difficile','cdifficile');
       
#insert into organism (abbreviation, genus, species, common_name)
#       values ('C.diptheriae', 'Corynesbacterium','diptheriae','cdiptheriae');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('D.discoideum', 'Dictyostelium','discoideum','ddiscoideum');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('E.tenella', 'Eimeria','tenella','etenella');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('E.histolytica', 'Entamoeba','histolytica','ehistolytica');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('E.cartaovora', 'Erwinai','cartaovora','ecartaovora');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('G.morsitans', 'Glossina','morsitans','gmorsitans');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('L.major', 'Leishmania','major','lmajor');
#insert into organism (abbreviation, genus, species, common_name)
#       values ('L.infantum', 'Leishmania','infantum','linfantum');
#insert into organism (abbreviation, genus, species, common_name)
#       values ('L.braziliensis', 'Leishmania','braziliensis','lbraziliensis');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('L.longipalpis', 'Lutzomyia','longipalpis','llongipalpis');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('M.leprae', 'Mycobacterium','leprae','mleprae');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('P.falciparum', 'Plasmodium','falciparum','pfalciparum');
#insert into organism (abbreviation, genus, species, common_name)
#       values ('P.chabaudi', 'Plasmodium','chabaudi','pchabaudi');
#insert into organism (abbreviation, genus, species, common_name)
#       values ('P.knowlesi', 'Plasmodium','knowlesi','pknowlesi');
#insert into organism (abbreviation, genus, species, common_name)
#       values ('P.vivax', 'Plasmodium','vivax','pvivax');
#insert into organism (abbreviation, genus, species, common_name)
#       values ('P.berghei', 'Plasmodium','berghei','pberghei');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('R.leguminosarum', 'Rhibosoma','leguminosarum','rleguminosarum');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('S.cerevisiae', 'Saccharomyces','cerevisiae','yeast');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('S.typhi', 'Salmonella','typhi','styphi');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('S.mansoni', 'Schistosoma','mansoni','smansoni');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('S.pombe', 'Schizosaccharomyces','pombe','spombe');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('S.aureusMRSA', 'Staphylococcus','aureus MRSA','saureus');
     
#insert into organism (abbreviation, genus, species, common_name)
#       values ('S.aureusMSSA', 'Staphylococcus','aureus MSSA','saureus');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('S.equi', 'Streptococcus','equi','sequi');  
#insert into organism (abbreviation, genus, species, common_name)
#       values ('S.pyogenes', 'Streptococcus','pyogenes','spyogenes');       
#insert into organism (abbreviation, genus, species, common_name)
#       values ('S.suis', 'Streptococcus','suis','ssuis');
#insert into organism (abbreviation, genus, species, common_name)
#       values ('S.uberis', 'Streptococcus','uberis','suberis');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('S.coelicolor', 'Streptomyces','coelicolor','scoelicolor');
       
#insert into organism (abbreviation, genus, species, common_name)
#       values ('T.annulata', 'Theileria','annulata','tannulata');
#insert into organism (abbreviation, genus, species, common_name)
#       values ('T.parva', 'Theileria','parva','tparva');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('T.whipplei', 'Tropheryma','whipplei','twhipplei');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('T.congolense', 'Trypanosoma','congolense','tcongolense');
#insert into organism (abbreviation, genus, species, common_name)
#       values ('T.cruzi', 'Trypanosoma','cruzi','tcruzi');
#insert into organism (abbreviation, genus, species, common_name)
#       values ('T.gambiense', 'Trypanosoma','gambiense','tgambiense');
#insert into organism (abbreviation, genus, species, common_name)
#       values ('T.vivax', 'Trypanosoma','vivax','tvivax');
#insert into organism (abbreviation, genus, species, common_name)
#       values ('T.brucei', 'Trypanosoma','brucei','tbrucei');
#insert into organism (abbreviation, genus, species, common_name)
#       values ('T.brucei', 'Trypanosoma','brucei 427','tbrucei427');

#insert into organism (abbreviation, genus, species, common_name)
#       values ('Y.pestis', 'Yersinia','pestis','ypestis');

--insert into organism (abbreviation, genus, species, common_name)
--       values ('', '','','');
--insert into organism (abbreviation, genus, species, common_name)
--       values ('', '','','');
--insert into organism (abbreviation, genus, species, common_name)
--       values ('', '','','');
--insert into organism (abbreviation, genus, species, common_name)
--       values ('', '','','');
--insert into organism (abbreviation, genus, species, common_name)
--       values ('', '','','');
--insert into organism (abbreviation, genus, species, common_name)
--       values ('', '','','');
--insert into organism (abbreviation, genus, species, common_name)
--       values ('', '','','');





-- Clear dbs
delete from db where name like 'DB:%';

-- Add local dbs
-- Add GeneDB dbs

-- Add local cv and cvterms
insert into cv (name, definition)
       values ('genedb_misc', 'Miscellaneous GeneDB-specific terms');



insert into dbxref(db_id, accession) values ((select db_id from db where name='null'), 'genedb-misc:top_level_seq');
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype)
		values ((select cv_id from cv where name='genedb_misc'), 
		'top_level_seq',
		'Marker to indicate that a sequence should be considered a top level feature',
		(select dbxref_id from dbxref where accession='genedb_misc:top_level_seq'),
		0, 0); 

insert into dbxref(db_id, accession) values ((select db_id from db where name='null'), 'genedb-misc:protein_charge');
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype)
		values ((select cv_id from cv where name='genedb_misc'), 
		'protein_charge',
		'Marker for protein_charge',
		(select dbxref_id from dbxref where accession='genedb_misc:protein_charge'),
		0, 0); 
		
insert into dbxref(db_id, accession) values ((select db_id from db where name='null'), 'genedb-misc:molecular_mass');
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype)
		values ((select cv_id from cv where name='genedb_misc'), 
		'molecular_mass',
		'Marker to indicate that a sequence should be considered a top level feature',
		(select dbxref_id from dbxref where accession='genedb_misc:molecular_mass'),
		0, 0); 
		
insert into dbxref(db_id, accession) values ((select db_id from db where name='null'), 'genedb-misc:isoelectric_point');
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype)
		values ((select cv_id from cv where name='genedb_misc'), 
		'isoelectric_point',
		'Marker to indicate that a sequence should be considered a top level feature',
		(select dbxref_id from dbxref where accession='genedb_misc:isolectric_point'),
		0, 0); 
		
#insert into dbxref(db_id, accession) values ((select db_id from db where name='null'), 'genedb-misc:top_level_seq');
#insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype)
#		values ((select cv_id from cv where name='genedb_misc'), 
#		'top_level_seq',
#		'Marker to indicate that a sequence should be considered a top level feature',
#		(select dbxref_id from dbxref where accession='genedb_misc:top_level_seq'),
#		0, 0); 
		
		
insert into dbxref(db_id, accession) values ((select db_id from db where name='null'), 'genedb-misc:taxonomy');
insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype)
		values ((select cv_id from cv where name='genedb_misc'), 
		'taxonomy',
		'Marker to indicate that a sequence should be considered a top level feature',
		(select dbxref_id from dbxref where accession='genedb_misc:taxonomy'),
		0, 0); 
       
       
insert into cv (name, definition)
       values ('genedb_literature', 'GeneDB-specific vocab for types of literature eg paper');
       
insert into cv (name, definition)
       values ('genedb_synonym_type', 'GeneDB-specific cv for more specific naming');

insert into cv (name, definition)
		values ('CC_genedb_controlledcuration', 'GeneDB-specific cv for controlled curation terms');
      
insert into cv (name, definition)
       values ('genedb_fcvt_prop_keys', 'Specific values which are used as keys for looking up feature_cvterm_prop');       

       
-- Load phylogeny relationships
insert into dbxref(db_id, accession) values ((select db_id from db where name='null'), 'genedb-misc:organism_heirachy');
insert into phylotree (dbxref_id, name, type_id, comment)
		values (
		(select dxref_id from dbxref where accession='genedb-misc:organism_heirachy'),
		 'org_heirachy', 
		(select from cvterm where name='taxonomy')
		 , 'GeneDB organism heirachy');
		 
insert into phylonode(parent_phylonode_id, phylotree_id)
