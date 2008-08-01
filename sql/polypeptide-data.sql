select polypeptide.uniquename
     , subfeature.uniquename
     , subfeature_type.name
     , subfeatureloc.fmin
     , subfeatureloc.fmax
     , primarydb.name||':'||primarydbxref.accession as primary
     , secondarydb.name||':'||secondarydbxref.accession as secondary
     , subfeatureproptype.name
     , subfeatureprop.value
from feature polypeptide
join featureloc polypeptideloc using (feature_id)
join featureloc subfeatureloc
    on subfeatureloc.srcfeature_id = polypeptideloc.feature_id
join feature subfeature
    on subfeature.feature_id = subfeatureloc.feature_id
join cvterm subfeature_type
    on subfeature_type.cvterm_id = subfeature.type_id
left join featureprop subfeatureprop
    on subfeature.feature_id = subfeatureprop.feature_id
left join cvterm subfeatureproptype
    on subfeatureproptype.cvterm_id = subfeatureprop.type_id
left join dbxref primarydbxref
    on subfeature.dbxref_id = primarydbxref.dbxref_id
left join db primarydb using (db_id)
left join feature_dbxref subfeature_dbxref
    on subfeature.feature_id = subfeature_dbxref.feature_id
left join dbxref secondarydbxref
    on subfeature_dbxref.dbxref_id = secondarydbxref.dbxref_id
left join db secondarydb
    on secondarydbxref.db_id = secondarydb.db_id
where subfeatureloc.rank = 0
and polypeptide.type_id = 191
order by polypeptide.feature_id
       , subfeature.feature_id
;

select polypeptide.uniquename
     , polypeptide_prop_type.name
     , polypeptide_prop.value
from feature polypeptide
join featureprop polypeptide_prop using (feature_id)
join cvterm polypeptide_prop_type on polypeptide_prop.type_id = polypeptide_prop_type.cvterm_id
where polypeptide.type_id = 191
;
