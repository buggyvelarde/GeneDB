select polypeptide.uniquename as polypeptide
     , domain_db.name as database
     , domain_dbxref.accession
     , domainloc.fmin
     , domainloc.fmax
from feature domain
join featureloc domainloc using (feature_id)
join feature polypeptide on domainloc.srcfeature_id = polypeptide.feature_id
join dbxref domain_dbxref on domain.dbxref_id = domain_dbxref.dbxref_id
join db domain_db using (db_id)
where polypeptide.type_id = 191 /*polypeptide*/
and   domain.type_id = 504 /*polypeptide_domain*/
order by polypeptide.uniquename
;


/* Shows how many different domain databases apply to each polypeptide */
select polypeptide.uniquename as polypeptide
     , count(distinct domain_db.name) as num_databases
from feature domain
join featureloc domainloc using (feature_id)
join feature polypeptide on domainloc.srcfeature_id = polypeptide.feature_id
join dbxref domain_dbxref on domain.dbxref_id = domain_dbxref.dbxref_id
join db domain_db using (db_id)
where polypeptide.type_id = 191 /*polypeptide*/
and   domain.type_id = 504 /*polypeptide_domain*/
group by polypeptide.uniquename
;

/* Finds domains that begin near the end of the polypeptide
   (a potentially problematic case for the protein map) */
select protein.uniquename as protein
     , domain.uniquename  as domain
     , domainloc.fmin
     , domainloc.fmax
     , protein.seqlen
from feature domain
join featureloc domainloc using (feature_id)
join feature protein on domainloc.srcfeature_id = protein.feature_id
where domain.type_id = 504 /*polypeptide_domain*/
and protein.seqlen - domainloc.fmin < 100
order by protein.seqlen - domainloc.fmin
;
