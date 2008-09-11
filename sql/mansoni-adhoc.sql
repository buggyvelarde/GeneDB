select cvterm.cvterm_id, cvterm.name, dbxref.accession, count(*)
from feature
join cvterm on feature.type_id = cvterm.cvterm_id
join dbxref on cvterm.dbxref_id = dbxref.dbxref_id
where cvterm.cv_id = 10 /* sequence ontology CV */
and feature.organism_id = 13 /* Schistosoma mansoni */
group by cvterm.cvterm_id, cvterm.name, dbxref.accession
;

select feature.feature_id
     , feature.uniquename
     , srcfeature.uniquename as src_feature
     , featureloc.locgroup
     , featureloc.fmin
     , featureloc.fmax
     , featureloc.strand
     , featureloc.phase
from feature
join featureloc using (feature_id)
join feature srcfeature on featureloc.srcfeature_id = srcfeature.feature_id
where feature.uniquename like E'Smp\\_000710%'
;

select feature.feature_id
     , feature.uniquename
     , proptypecv.name || ':' || proptype.name as type
     , featureprop.value
from feature
join featureprop using (feature_id)
join cvterm proptype on featureprop.type_id = proptype.cvterm_id
join cv proptypecv on proptype.cv_id = proptypecv.cv_id
where feature.organism_id = 13
order by feature.feature_id, proptype.cvterm_id, featureprop.rank
;

select cvterm.cvterm_id
     , cvterm.name
     , cvtermpath.pathdistance
from cvtermpath
join cvterm on cvtermpath.subject_id = cvterm.cvterm_id
where cvtermpath.object_id = 427 /*chromosome*/
and cvtermpath.type_id = 35 /*is_a*/
;

select s.name
     , rt.name
     , o.name
from cvterm s
join cvterm_relationship r on s.cvterm_id = r.subject_id
join cvterm o on o.cvterm_id = r.object_id
join cvterm rt on r.type_id = rt.cvterm_id
where s.cv_id = 6
and   o.cv_id = 6
;
