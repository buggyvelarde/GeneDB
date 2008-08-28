update phylotree
set name = 'org_hierarchy'
  , comment = 'GeneDB organism hierarchy'
where name = 'org_heirachy'
;

update dbxref
set accession = 'genedb_misc:organism_hierarchy'
where accession = 'genedb_misc:organism_heirachy'
;

