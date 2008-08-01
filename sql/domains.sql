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
