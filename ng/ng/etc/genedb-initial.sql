-- ----------------------
-- --- Structural additions
-- ----------------------
create unique index cvterm_idx_genedb1 on cvterm (lower(name), cv_id, is_obsolete);
create index feature_genedb_idx1 on feature (organism_id, type_id, feature_id);


-- ----------------------
-- --- Functions
-- ----------------------
/* Note: we must have run createlang plpgsql first */
create or replace function reverse(varchar) returns varchar as $$
declare
       _temp varchar;
       _count int;
begin
       _temp := '';
       for _count in reverse length($1)..1 loop
               _temp := _temp || substring($1 from _count for 1);
       end loop;
       return _temp;
end;
$$ language plpgsql immutable;

/**
 *    format_location(chromosome_uniquename, fmin, fmax, strand)
 */
create or replace function format_location(text,integer,integer,smallint)
returns text
as $$
  select case when $4 >= 0
      then $1 || ':'  || ($2 + 1) || '..' || $3
      else $1 || ':(' || ($2 + 1) || '..' || $3 || ')'
  end
$$
language sql
immutable strict
;


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
        'GeneDB',
        'The GeneDB database',
        'http://beta.genedb.org/NamedFeature/');

insert into db (name, description, urlprefix) values (
        'SMART',
        'The SMART database',
        'http://smart.embl-heidelberg.de/smart/do_annotation.pl?&BLAST=DUMMY&DOMAIN=');

insert into db (name, description, urlprefix) values (
        'TDRtargets',
        'The TDRtargets database',
        'http://tdrtargets.org/targets/view?gene_name=');

insert into db (name, description, urlprefix) values (
        'Superfamily',
        'The Superfamily database',
        'http://supfam.cs.bris.ac.uk/SUPERFAMILY/cgi-bin/search.cgi?search_field=');

insert into db (name, description, urlprefix) values (
        'PRINTS',
        'The PRINTS database',
        'http://www.bioinf.manchester.ac.uk/cgi-bin/dbbrowser/PRINTS/DoPRINTS.pl?cmd_a=Display&fun_a=text&qst_a=');

insert into db (name, description, urlprefix) values (
        'ProDom',
        'The ProDom database',
        'http://prodom.prabi.fr/prodom/current/cgi-bin/request.pl?question=DBEN&query=');

insert into db (name, description, urlprefix) values (
        'PANTHER',
        'The PANTHER database',
        'http://www.pantherdb.org/panther/family.do?clsAccession=');

insert into db (name, description, urlprefix) values (
        'FlyBase',
        'The FlyBase database',
        'http://flybase.bio.indiana.edu/.bin/fbidq.html?');

insert into db (name, description, urlprefix) values (
        'PIRSF',
        'Protein Information Resource PIRSF database',
        'http://pir.georgetown.edu/cgi-bin/ipcSF?id=');

insert into db (name, description, urlprefix) values (
        'RF',
        'The RefSeq database',
        'http://www.ncbi.nlm.nih.gov/sites/gquery?term=');

insert into db (name, description, urlprefix) values (
        'GB',
        'The GenBank database',
        'http://www.ncbi.nlm.nih.gov/sites/gquery?term=');

insert into db (name, description, urlprefix) values (
        'CTP',
        'The TriTryp pathway comparison database',
        'http://www.genedb.org/genedb/pathway_comparison_TriTryp/');

insert into db (name, description, urlprefix) values (
        'TrypanoCyc',
        'The TrypanoCyc database',
        'http://www.genedb.org/trypanocyc/TRYPANO/NEW-IMAGE?type=GENE-IN-CHROM-BROWSER&object=');

insert into db (name, description, urlprefix) values (
        'GI',
        'NCBI GI number',
        'http://www.ncbi.nlm.nih.gov/entrez/sutils/girevhist.cgi?val=');

insert into db (name, description, urlprefix) values (
    'VSGDB',
    'A database for trypanosome variant surface glycoproteins',
    'http://leishman.cent.gla.ac.uk/cgi-bin/vsg.pl?id=');

    --
-- Add url prefix to existing dbs
--
update db set urlprefix='http://merops.sanger.ac.uk/cgi-bin/merops.cgi?id='
    where name='MEROPS';

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

update db set urlprefix='http://pfam.sanger.ac.uk/family?type=Family&entry='
    where name='Pfam';

update db set name='Rfam', urlprefix='http://rfam.sanger.ac.uk/family/'
    where name='RFAM';

update db set urlprefix='http://ca.expasy.org/cgi-bin/prosite-search-ac?'
    where name='Prosite';

update db set urlprefix='http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=PubMed&dopt=Abstract&list_uids='
    where name='PMID';

update db set urlprefix='http://supfam.cs.bris.ac.uk/SUPERFAMILY/cgi-bin/search.cgi?search_field='
    where name='Superfamily';

update db set urlprefix='http://cmr.jcvi.org/cgi-bin/CMR/HmmReport.cgi?hmm_acc='
    where name='TIGR_TIGRFAMS';

update db set urlprefix='http://chemlims.com/OPI/MServlet.ChemInfo?module=GeneGo&act=findGenes&Gene_Name_='
    where name='OPI';

-- This is deliberately a relative URL as it redirects via our site
update db set urlprefix='/DbLinkRedirector/EC/'
    where name='EC';

update db set urlprefix='http://www.expasy.org/cgi-bin/sprot-search-de?'
            , description='The SwissProt database'
    where name='SP';

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
--        (select db_id from db where name='null'),
--        'genedb_synonym_type:DisplayPage',
--        'dbxref for cvterm DisplayPage'
--);
--insert into cvterm(cv_id, name, definition, dbxref_id, is_obsolete, is_relationshiptype) values (
--        (select cv_id from cv where name='genedb_misc_...'),
--        'DisplayPage',
--        'cvterm for attribute value page in taxonomy xml, which decides whether ot not a group should have individual page',
--        (select dbxref_id from dbxref where accession='genedb_synonym_type:temporary_systematic_id'),
--        0, 0
--);


-- Load phylogeny relationships
insert into dbxref(db_id, accession) values (
        (select db_id from db where name='null'),
        'genedb_misc:organism_hierarchy'
);
insert into phylotree (dbxref_id, name, type_id, comment) values (
        (select dbxref_id from dbxref where accession='genedb_misc:organism_hirarchy'),
        'org_hierarchy',
        (select cvterm_id from cvterm where name='taxonomy'),
        'GeneDB organism hierarchy'
);

--
-- ----------------------
-- --- Graph tables
-- ----------------------

CREATE SCHEMA graph;

CREATE SEQUENCE graph.graph_graph_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

SELECT pg_catalog.setval('graph.graph_graph_id_seq', 1, false);


CREATE TABLE graph.graph (
        graph_id integer PRIMARY KEY DEFAULT
nextval('graph.graph_graph_id_seq'::regclass),
        feature_id integer NOT NULL,
        name varchar(255) NOT NULL,
        description text,
        data OID
);

ALTER TABLE ONLY graph.graph
  ADD CONSTRAINT feature_id_fkey FOREIGN KEY (feature_id) REFERENCES
feature(feature_id) ON CASCADE DELETE;




