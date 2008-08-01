select cvterm.cvterm_id, cvterm.name, dbxref.accession, count(*)
from feature
join cvterm on feature.type_id = cvterm.cvterm_id
join dbxref on cvterm.dbxref_id = dbxref.dbxref_id
where cvterm.cv_id = 10 /* sequence ontology CV */
group by cvterm.cvterm_id, cvterm.name, dbxref.accession
;

select cvterm.name, dbxref.accession, count(*)
from feature_relationship
join cvterm on feature_relationship.type_id = cvterm.cvterm_id
join dbxref on cvterm.dbxref_id = dbxref.dbxref_id
where cvterm.cv_id = 10 /* sequence ontology CV */
group by cvterm.name, dbxref.accession
;
